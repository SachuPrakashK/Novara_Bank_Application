package com.novara.banking;

import com.novara.banking.ledger.*;
import com.novara.banking.account.AccountHoldService;
import com.novara.banking.beneficiary.BeneficiaryService;
import com.novara.banking.beneficiary.CreateBeneficiaryRequest;
import com.novara.banking.reconciliation.ReconciliationService;
import com.novara.banking.shared.DomainException;
import com.novara.banking.transfer.InternalTransferRequest;
import com.novara.banking.transfer.InternalTransferService;
import com.novara.banking.payment.*;
import com.novara.banking.identity.CurrentIdentity.FederatedIdentity;
import com.novara.banking.card.CardCommands;
import com.novara.banking.card.CardService;
import com.novara.banking.deposit.DepositInstallmentService;
import com.novara.banking.deposit.DepositModels.ReviewRequest;
import com.novara.banking.deposit.DepositService;
import com.novara.banking.loan.LoanModels.ApplicationRequest;
import com.novara.banking.loan.LoanModels.QuoteRequest;
import com.novara.banking.loan.LoanService;
import com.novara.banking.statement.StatementExportService;
import com.novara.banking.statement.StatementService;
import com.novara.banking.profile.ProfileService;
import com.novara.banking.servicerequest.ServiceRequestService;
import com.novara.banking.notification.NotificationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.dao.DataAccessException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.util.List;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.*;
import org.springframework.http.MediaType;

@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class BankingIntegrationTest {
    @Container static final PostgreSQLContainer<?> POSTGRES=new PostgreSQLContainer<>("postgres:17-alpine").withDatabaseName("novara_test").withUsername("novara").withPassword("test-only");
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry){registry.add("spring.datasource.url",POSTGRES::getJdbcUrl);registry.add("spring.datasource.username",POSTGRES::getUsername);registry.add("spring.datasource.password",POSTGRES::getPassword);registry.add("novara.deposits.installment-worker-initial-delay",()->"PT24H");registry.add("novara.statements.generation-limit",()->"2");}
    @MockBean JwtDecoder jwtDecoder;
    @Autowired JdbcClient jdbc; @Autowired LedgerPostingService ledger; @Autowired InternalTransferService transfers; @Autowired ReconciliationService reconciliation; @Autowired AccountHoldService holds; @Autowired BeneficiaryService beneficiaries; @Autowired PaymentService payments; @Autowired PaymentScheduleService schedules; @Autowired CardService cards; @Autowired DepositService deposits; @Autowired DepositInstallmentService installments; @Autowired LoanService loans; @Autowired StatementService statements; @Autowired StatementExportService statementExports; @Autowired ProfileService profiles; @Autowired ServiceRequestService serviceRequests; @Autowired NotificationService notifications; @Autowired MockMvc mockMvc;
    UUID sourceId,destinationId; long sourceLedger,destinationLedger;

    @BeforeEach void seed(){
        jdbc.sql("TRUNCATE payment_schedule_occurrences,payment_schedules,payment_status_events,payment_intents,payment_limit_policies,beneficiary_verifications,beneficiaries,reconciliation_exceptions,reconciliation_runs,outbox_events,audit_events,idempotency_records,transfers,account_holds,journal_lines,journals,posting_batches,account_owners,bank_accounts,ledger_balances,ledger_accounts,customers RESTART IDENTITY CASCADE").update();
        long customer=jdbc.sql("INSERT INTO customers(identity_issuer,identity_subject,display_name) VALUES('http://localhost:8081/realms/novara','oidc-customer-1','Fictional Customer') RETURNING id").query(Long.class).single();
        var first=createAccount("SAVINGS-1","hash-1","•••• 1001",100_000);sourceId=first.publicId;sourceLedger=first.ledgerId;
        var second=createAccount("SAVINGS-2","hash-2","•••• 1002",50_000);destinationId=second.publicId;destinationLedger=second.ledgerId;
        jdbc.sql("INSERT INTO account_owners(account_id,customer_id,ownership_role) VALUES(:account,:customer,'PRIMARY')").param("account",first.accountId).param("customer",customer).update();
        jdbc.sql("INSERT INTO account_owners(account_id,customer_id,ownership_role) VALUES(:account,:customer,'PRIMARY')").param("account",second.accountId).param("customer",customer).update();
    }

    @Test void postsBalancedJournalAndUpdatesBalances(){
        transfers.execute("oidc-customer-1","valid-balanced-key-0001",request(10_000),"corr-1");
        assertThat(balance(sourceLedger)).isEqualTo(90_000);assertThat(balance(destinationLedger)).isEqualTo(60_000);
        assertThat(jdbc.sql("SELECT sum(amount_minor) FILTER(WHERE direction='DEBIT')=sum(amount_minor) FILTER(WHERE direction='CREDIT') FROM journal_lines").query(Boolean.class).single()).isTrue();
    }
    @Test void rejectsUnbalancedAndNonPositivePosting(){
        assertThatThrownBy(()->ledger.post(new JournalCommand("bad-batch","bad-journal","INR","bad",List.of(new JournalLineCommand(sourceLedger,JournalLineCommand.Direction.DEBIT,100,"INR"),new JournalLineCommand(destinationLedger,JournalLineCommand.Direction.CREDIT,99,"INR")),null))).isInstanceOf(DomainException.class);
        assertThatThrownBy(()->transfers.execute("oidc-customer-1","invalid-amount-key-01",request(0),"corr")).isInstanceOf(DomainException.class);
    }
    @Test void rejectsInsufficientFundsUnauthorizedAndRestrictedAccounts(){
        assertThatThrownBy(()->transfers.execute("oidc-customer-1","insufficient-key-001",request(100_001),"corr")).isInstanceOf(DomainException.class).hasMessageContaining("sufficient");
        assertThatThrownBy(()->transfers.execute("different-subject","unauthorized-key-001",request(100),"corr")).isInstanceOf(DomainException.class).hasMessageContaining("not found");
        jdbc.sql("UPDATE bank_accounts SET debit_restricted=true WHERE public_id=:id").param("id",sourceId).update();
        assertThatThrownBy(()->transfers.execute("oidc-customer-1","restricted-key-0001",request(100),"corr")).isInstanceOf(DomainException.class).hasMessageContaining("eligible");
    }
    @Test void idempotentRetryReturnsOriginalAndPayloadChangeIsRejected(){
        var first=transfers.execute("oidc-customer-1","idempotency-key-0001",request(500),"corr");
        var retry=transfers.execute("oidc-customer-1","idempotency-key-0001",request(500),"corr");
        assertThat(retry.id()).isEqualTo(first.id());assertThat(jdbc.sql("SELECT count(*) FROM journals").query(Long.class).single()).isEqualTo(1);
        assertThatThrownBy(()->transfers.execute("oidc-customer-1","idempotency-key-0001",request(501),"corr")).isInstanceOf(DomainException.class).hasMessageContaining("different");
    }
    @Test void concurrentDebitsCannotOverspend() throws Exception {
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()){
            var start=new CountDownLatch(1);
            var tasks=List.of((Callable<Boolean>)()->attempt(start,"concurrent-key-001",80_000),(Callable<Boolean>)()->attempt(start,"concurrent-key-002",80_000));
            var futures=tasks.stream().map(executor::submit).toList();start.countDown();
            assertThat(futures.stream().filter(f->{try{return f.get();}catch(Exception e){return false;}}).count()).isEqualTo(1);
            assertThat(balance(sourceLedger)).isEqualTo(20_000);
        }
    }
    @Test void opposingTransfersUseDeterministicLocksWithoutDeadlock() throws Exception {
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()){
            var start=new CountDownLatch(1);
            var one=executor.submit(()->{start.await();return transfers.execute("oidc-customer-1","opposing-lock-key-01",request(1_000),"corr");});
            var two=executor.submit(()->{start.await();return transfers.execute("oidc-customer-1","opposing-lock-key-02",new InternalTransferRequest(destinationId,sourceId,1_000,"INR",null),"corr");});
            start.countDown();assertThat(one.get(10,TimeUnit.SECONDS)).isNotNull();assertThat(two.get(10,TimeUnit.SECONDS)).isNotNull();
            assertThat(balance(sourceLedger)).isEqualTo(100_000);assertThat(balance(destinationLedger)).isEqualTo(50_000);
        }
    }
    @Test void reversalPostsOppositeImmutableJournal(){
        var original=ledger.post(new JournalCommand("original-batch","original-journal","INR","Test",List.of(new JournalLineCommand(sourceLedger,JournalLineCommand.Direction.DEBIT,200,"INR"),new JournalLineCommand(destinationLedger,JournalLineCommand.Direction.CREDIT,200,"INR")),null));
        ledger.reverse(original.journalId(),"reversal-batch","reversal-journal");
        assertThat(balance(sourceLedger)).isEqualTo(100_000);assertThat(balance(destinationLedger)).isEqualTo(50_000);
        assertThat(jdbc.sql("SELECT count(*) FROM journals WHERE reversal_of_journal_id=:id").param("id",original.journalId()).query(Long.class).single()).isEqualTo(1);
    }
    @Test void postedJournalAndLinesAreDatabaseImmutable(){
        var original=ledger.post(new JournalCommand("immutable-batch","immutable-journal","INR","Test",List.of(new JournalLineCommand(sourceLedger,JournalLineCommand.Direction.DEBIT,200,"INR"),new JournalLineCommand(destinationLedger,JournalLineCommand.Direction.CREDIT,200,"INR")),null));
        assertThatThrownBy(()->jdbc.sql("UPDATE journals SET description='tampered' WHERE id=:id").param("id",original.journalId()).update()).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(()->jdbc.sql("DELETE FROM journal_lines WHERE journal_id=:id").param("id",original.journalId()).update()).isInstanceOf(DataAccessException.class);
    }
    @Test void repeatedReversalIsRejectedWithoutChangingHistory(){
        var original=ledger.post(new JournalCommand("repeat-original-batch","repeat-original-journal","INR","Test",List.of(new JournalLineCommand(sourceLedger,JournalLineCommand.Direction.DEBIT,200,"INR"),new JournalLineCommand(destinationLedger,JournalLineCommand.Direction.CREDIT,200,"INR")),null));
        ledger.reverse(original.journalId(),"repeat-reversal-batch","repeat-reversal-journal");
        assertThatThrownBy(()->ledger.reverse(original.journalId(),"second-reversal-batch","second-reversal-journal")).isInstanceOf(DomainException.class).hasMessageContaining("cannot be reversed");
        assertThat(jdbc.sql("SELECT count(*) FROM journals WHERE reversal_of_journal_id=:id").param("id",original.journalId()).query(Long.class).single()).isEqualTo(1);
    }
    @Test void concurrentReversalAllowsExactlyOne() throws Exception {
        var original=ledger.post(new JournalCommand("concurrent-reverse-original-batch","concurrent-reverse-original-journal","INR","Test",List.of(new JournalLineCommand(sourceLedger,JournalLineCommand.Direction.DEBIT,200,"INR"),new JournalLineCommand(destinationLedger,JournalLineCommand.Direction.CREDIT,200,"INR")),null));
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()){
            var start=new CountDownLatch(1);
            var one=executor.submit(()->attemptReversal(start,original.journalId(),"concurrent-reversal-batch-1","concurrent-reversal-journal-1"));
            var two=executor.submit(()->attemptReversal(start,original.journalId(),"concurrent-reversal-batch-2","concurrent-reversal-journal-2"));
            start.countDown();
            assertThat(List.of(one.get(10,TimeUnit.SECONDS),two.get(10,TimeUnit.SECONDS)).stream().filter(Boolean::booleanValue).count()).isEqualTo(1);
        }
        assertThat(jdbc.sql("SELECT count(*) FROM journals WHERE reversal_of_journal_id=:id").param("id",original.journalId()).query(Long.class).single()).isEqualTo(1);
    }
    @Test void activeHoldReducesAvailableFundsAndReleaseRestoresThem(){
        var hold=holds.place("oidc-customer-1",sourceId,70_000,"INR","CARD_AUTHORIZATION","hold-001",Instant.now().plusSeconds(3600));
        assertThatThrownBy(()->transfers.execute("oidc-customer-1","held-funds-key-001",request(40_000),"corr")).isInstanceOf(DomainException.class).hasMessageContaining("available");
        holds.release("oidc-customer-1",hold);
        transfers.execute("oidc-customer-1","released-funds-key-1",request(40_000),"corr");
        assertThat(balance(sourceLedger)).isEqualTo(60_000);
    }
    @Test void idempotencyKeyIsIsolatedAcrossCustomers(){
        long customer=jdbc.sql("INSERT INTO customers(identity_issuer,identity_subject,display_name) VALUES('http://localhost:8081/realms/novara','oidc-customer-2','Second Customer') RETURNING id").query(Long.class).single();
        var third=createAccount("SAVINGS-3","hash-3","**** 1003",20_000); var fourth=createAccount("SAVINGS-4","hash-4","**** 1004",10_000);
        jdbc.sql("INSERT INTO account_owners(account_id,customer_id,ownership_role) VALUES(:account,:customer,'PRIMARY')").param("account",third.accountId).param("customer",customer).update();
        jdbc.sql("INSERT INTO account_owners(account_id,customer_id,ownership_role) VALUES(:account,:customer,'PRIMARY')").param("account",fourth.accountId).param("customer",customer).update();
        var key="shared-customer-key-001";
        var first=transfers.execute("oidc-customer-1",key,request(100),"corr");
        var second=transfers.execute("oidc-customer-2",key,new InternalTransferRequest(third.publicId,fourth.publicId,100,"INR",null),"corr");
        assertThat(second.id()).isNotEqualTo(first.id());
        transfers.execute("oidc-customer-2","customer-two-only-key",new InternalTransferRequest(third.publicId,fourth.publicId,100,"INR",null),"corr");
        assertThatThrownBy(()->transfers.requireByIdempotency("oidc-customer-1","customer-two-only-key")).isInstanceOf(DomainException.class);
    }
    @Test void concurrentDuplicateRequestPostsOnce() throws Exception {
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()){
            var start=new CountDownLatch(1); var key="duplicate-concurrent-key";
            var one=executor.submit(()->{start.await();return transfers.execute("oidc-customer-1",key,request(500),"corr");});
            var two=executor.submit(()->{start.await();return transfers.execute("oidc-customer-1",key,request(500),"corr");});
            start.countDown(); assertThat(two.get(10,TimeUnit.SECONDS).id()).isEqualTo(one.get(10,TimeUnit.SECONDS).id());
        }
        assertThat(jdbc.sql("SELECT count(*) FROM transfers WHERE idempotency_key='duplicate-concurrent-key'").query(Long.class).single()).isEqualTo(1);
    }
    @Test void databaseFailureRollsBackJournalAndBalances(){
        jdbc.sql("CREATE FUNCTION fail_transfer_insert() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'simulated failure'; END $$").update();
        jdbc.sql("CREATE TRIGGER test_fail_transfer BEFORE INSERT ON transfers FOR EACH ROW EXECUTE FUNCTION fail_transfer_insert()").update();
        assertThatThrownBy(()->transfers.execute("oidc-customer-1","rollback-test-key-01",request(1000),"corr")).isInstanceOf(RuntimeException.class);
        jdbc.sql("DROP TRIGGER test_fail_transfer ON transfers").update();jdbc.sql("DROP FUNCTION fail_transfer_insert()").update();
        assertThat(balance(sourceLedger)).isEqualTo(100_000);assertThat(jdbc.sql("SELECT count(*) FROM journals").query(Long.class).single()).isZero();
    }
    @Test void reconciliationReportsProjectionMismatchWithoutRepairingIt(){
        jdbc.sql("UPDATE ledger_balances SET balance_minor=balance_minor+1 WHERE ledger_account_id=:id").param("id",sourceLedger).update();
        var result=reconciliation.run();assertThat(result.exceptionCount()).isGreaterThanOrEqualTo(1);assertThat(balance(sourceLedger)).isEqualTo(100_001);
    }
    @Test void sensitiveEndpointsRejectMissingAuthenticationAndOperationsScope() throws Exception {
        mockMvc.perform(get("/api/v1/accounts")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/operations/reconciliation").with(jwt().jwt(token->token.subject("oidc-customer-1")))).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/operations/reconciliation").with(jwt().jwt(token->token.subject("staff-demo")).authorities(createAuthorityList("ROLE_OPERATIONS","SCOPE_operations.reconcile")))).andExpect(status().isForbidden());
    }
    @Test void forgedBearerRolesCannotBypassOperationsDenyAll() throws Exception {
        mockMvc.perform(get("/api/v1/operations/dashboard").with(jwt().jwt(token->token.subject("oidc-customer-1"))
            .authorities(createAuthorityList("ROLE_ADMINISTRATOR","ROLE_OPERATIONS","SCOPE_operations.reconcile"))))
            .andExpect(status().isForbidden());
    }
    @Test void browserSessionsCannotBypassOperationsDenyAll() throws Exception {
        mockMvc.perform(post("/api/v1/operations/reconciliation").with(oidcLogin().authorities(
            createAuthorityList("ROLE_OPERATIONS","SCOPE_operations.reconcile"))).with(csrf()))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/operations/customers").with(oidcLogin()))
            .andExpect(status().isForbidden());
    }
    @Test void operationsMutationShapesRemainUnavailable() throws Exception {
        var target="/api/v1/operations/accounts/"+UUID.randomUUID();
        var proposedStaff=jwt().jwt(token->token.subject("staff-demo"))
            .authorities(createAuthorityList("ROLE_OPERATIONS","SCOPE_operations.write"));
        mockMvc.perform(post(target).with(proposedStaff)).andExpect(status().isForbidden());
        mockMvc.perform(put(target).with(proposedStaff)).andExpect(status().isForbidden());
        mockMvc.perform(patch(target).with(proposedStaff)).andExpect(status().isForbidden());
        mockMvc.perform(delete(target).with(proposedStaff)).andExpect(status().isForbidden());
    }
    @Test void cardOwnershipStatusAndIdempotencyAreEnforced(){
        UUID card=seedCard("ACTIVE");var identity=new FederatedIdentity("http://localhost:8081/realms/novara","oidc-customer-1");
        var frozen=cards.status(identity,card,"FROZEN","card-freeze-key-0001","corr-card");
        assertThat(frozen.status()).isEqualTo("FROZEN");
        assertThat(cards.status(identity,card,"FROZEN","card-freeze-key-0001","corr-card").status()).isEqualTo("FROZEN");
        assertThat(jdbc.sql("SELECT count(*) FROM card_status_events").query(Long.class).single()).isEqualTo(1);
        assertThatThrownBy(()->cards.owned(new FederatedIdentity("http://localhost:8081/realms/novara","other-customer"),card)).isInstanceOf(DomainException.class);
    }
    @Test void cardControlsRejectInvalidLimitsAndPersistAcknowledgedChanges(){
        UUID card=seedCard("ACTIVE");var identity=new FederatedIdentity("http://localhost:8081/realms/novara","oidc-customer-1");
        assertThatThrownBy(()->cards.controls(identity,card,new CardCommands.Controls(true,true,true,true,false,500,200,200,100,300),"card-control-key-001","corr")).isInstanceOf(DomainException.class);
        var updated=cards.controls(identity,card,new CardCommands.Controls(true,false,true,false,false,500,200,700,100,1000),"card-control-key-002","corr");
        assertThat(updated.controls().onlineEnabled()).isFalse();assertThat(updated.controls().dailyLimitMinor()).isEqualTo(1000);
        assertThat(jdbc.sql("SELECT status FROM card_control_requests WHERE idempotency_key='card-control-key-002'").query(String.class).single()).isEqualTo("CONFIRMED");
    }
    @Test void accountEndpointEnforcesOwnershipWithoutRevealingExistence() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/"+UUID.randomUUID()).with(jwt().jwt(token->token.subject("oidc-customer-1")))).andExpect(status().isNotFound());
    }
    @Test void invalidBearerTokenIsRejected() throws Exception {
        when(jwtDecoder.decode("invalid-token")).thenThrow(new BadJwtException("invalid"));
        mockMvc.perform(get("/api/v1/accounts").header("Authorization","Bearer invalid-token")).andExpect(status().isUnauthorized());
        reset(jwtDecoder);
    }
    @Test void anonymousSessionBootstrapReturnsNoCustomerData() throws Exception {
        mockMvc.perform(get("/api/v1/auth/session"))
            .andExpect(status().isOk())
            .andExpect(result -> assertThat(result.getResponse().getContentAsString()).contains("\"authenticated\":false"));
    }
    @Test void browserTransferRejectsMissingAndInvalidCsrfWithoutPosting() throws Exception {
        String body="{\"sourceAccountId\":\""+sourceId+"\",\"destinationAccountId\":\""+destinationId+"\",\"amountMinor\":100,\"currency\":\"INR\"}";
        mockMvc.perform(post("/api/v1/transfers/internal").with(customerLogin()).header("Idempotency-Key","csrf-missing-key-01").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/transfers/internal").with(customerLogin()).with(csrf().useInvalidToken()).header("Idempotency-Key","csrf-invalid-key-001").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        assertThat(jdbc.sql("SELECT count(*) FROM transfers").query(Long.class).single()).isZero();
    }
    @Test void browserTransferWithValidCsrfAndMappedCustomerSucceeds() throws Exception {
        String body="{\"sourceAccountId\":\""+sourceId+"\",\"destinationAccountId\":\""+destinationId+"\",\"amountMinor\":100,\"currency\":\"INR\"}";
        mockMvc.perform(post("/api/v1/transfers/internal").with(customerLogin()).with(csrf()).header("Idempotency-Key","csrf-valid-key-0001").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        assertThat(jdbc.sql("SELECT count(*) FROM transfers").query(Long.class).single()).isEqualTo(1);
    }
    @Test void highValueTransferRequiresRecentIdentityProviderMfa() throws Exception {
        String body="{\"sourceAccountId\":\""+sourceId+"\",\"destinationAccountId\":\""+destinationId+"\",\"amountMinor\":10000000,\"currency\":\"INR\"}";
        mockMvc.perform(post("/api/v1/transfers/internal").with(customerLogin()).with(csrf()).header("Idempotency-Key","step-up-required-01").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        assertThat(jdbc.sql("SELECT count(*) FROM transfers").query(Long.class).single()).isZero();
    }
    @Test void unknownAndDisabledIdentitiesCannotReadCustomerData() throws Exception {
        mockMvc.perform(get("/api/v1/accounts").with(oidcLogin().idToken(token -> token.issuer("http://localhost:8081/realms/novara").subject("unknown-subject")))).andExpect(status().isForbidden());
        jdbc.sql("UPDATE customers SET status='RESTRICTED' WHERE identity_subject='oidc-customer-1'").update();
        mockMvc.perform(get("/api/v1/accounts").with(customerLogin())).andExpect(status().isForbidden());
    }
    @Test void openRedirectInputFallsBackToDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/auth/login").param("returnTo","//evil.example/phish"))
            .andExpect(status().is3xxRedirection())
            .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/oauth2/authorization/novara"))
            .andExpect(result -> assertThat(result.getResponse().getRedirectedUrl()).doesNotContain("evil.example"));
    }
    @Test void logoutRequiresCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout").with(customerLogin())).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/auth/logout").with(customerLogin()).with(csrf())).andExpect(status().isNoContent());
    }
    @Test void allUnsafeBrowserMethodsRequireCsrfAndUntrustedOriginsAreRejected() throws Exception {
        mockMvc.perform(put("/api/v1/security/sessions/example").with(customerLogin())).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/security/sessions/example").with(customerLogin())).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/security/sessions/example").with(customerLogin())).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/auth/session/continue").with(customerLogin()).header("Origin","https://evil.example"))
            .andExpect(status().isForbidden())
            .andExpect(result -> assertThat(result.getResponse().getHeader("Access-Control-Allow-Origin")).isNull());
    }

    @Test void beneficiaryCreationIsOwnedPendingAndIdempotent() {
        var request=beneficiaryRequest("123456789012");
        var first=beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-create-key-01",request,"corr");
        var replay=beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-create-key-01",request,"corr");
        assertThat(replay.id()).isEqualTo(first.id());
        assertThat(first.status()).isEqualTo("PENDING_VERIFICATION");
        assertThat(first.maskedAccount()).isEqualTo("•••• 9012");
        assertThat(jdbc.sql("SELECT count(*) FROM beneficiary_verifications WHERE status='PENDING'").query(Long.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("SELECT count(*) FROM beneficiaries").query(Long.class).single()).isEqualTo(1);
    }

    @Test void duplicateBeneficiaryAndChangedIdempotentPayloadAreRejected() {
        beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-create-key-02",beneficiaryRequest("123456789012"),"corr");
        assertThatThrownBy(() -> beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-create-key-03",beneficiaryRequest("123456789012"),"corr"))
            .isInstanceOf(DomainException.class).hasMessageContaining("already exists");
        assertThatThrownBy(() -> beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-create-key-02",beneficiaryRequest("999999999999"),"corr"))
            .isInstanceOf(DomainException.class).hasMessageContaining("different beneficiary details");
    }

    @Test void beneficiaryOwnershipDoesNotRevealAnotherCustomersRecord() {
        var beneficiary=beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-create-key-04",beneficiaryRequest("123456789012"),"corr");
        jdbc.sql("INSERT INTO customers(identity_issuer,identity_subject,display_name) VALUES(:issuer,'oidc-customer-2','Second Customer')").param("issuer",issuer()).update();
        assertThatThrownBy(() -> beneficiaries.requireOwned(issuer(),"oidc-customer-2",beneficiary.id()))
            .isInstanceOf(DomainException.class).hasMessageContaining("not found");
    }

    @Test void beneficiaryVerificationAppliesCoolingPeriodAndFailureRestricts() {
        var accepted=beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-create-key-05",beneficiaryRequest("123456789012"),"corr");
        var active=beneficiaries.completeVerification(accepted.id(),true,"sandbox-verification-1");
        assertThat(active.status()).isEqualTo("ACTIVE"); assertThat(active.availableFrom()).isAfter(Instant.now());
        var rejected=beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-create-key-06",beneficiaryRequest("987654321098"),"corr");
        assertThat(beneficiaries.completeVerification(rejected.id(),false,"sandbox-verification-2").status()).isEqualTo("RESTRICTED");
    }

    @Test void beneficiaryDeactivationRequiresCsrfAndRecentProviderMfa() throws Exception {
        var beneficiary=beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-create-key-07",beneficiaryRequest("123456789012"),"corr");
        mockMvc.perform(delete("/api/v1/beneficiaries/"+beneficiary.id()).with(customerLogin())).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/v1/beneficiaries/"+beneficiary.id()).with(customerLogin()).with(csrf())).andExpect(status().isForbidden());
        assertThat(beneficiaries.requireOwned(issuer(),"oidc-customer-1",beneficiary.id()).status()).isEqualTo("PENDING_VERIFICATION");
    }

    @Test void serverIntentPostsOwnAccountPaymentOnceAndKeepsJournalBalanced() {
        var intent=payments.createIntent(identity(),new CreatePaymentIntentRequest(sourceId,CreatePaymentIntentRequest.DestinationType.OWN_ACCOUNT,destinationId,2500,"INR","INTERNAL","Own transfer"),"corr");
        assertThat(intent.status()).isEqualTo("PENDING_AUTHORIZATION");
        var completed=payments.confirm(identity(),intent.id(),"payment-confirm-key-01","corr");
        var replay=payments.confirm(identity(),intent.id(),"payment-confirm-key-01","corr");
        assertThat(completed.status()).isEqualTo("COMPLETED");assertThat(replay.reference()).isEqualTo(completed.reference());
        assertThat(balance(sourceLedger)).isEqualTo(97_500);assertThat(balance(destinationLedger)).isEqualTo(52_500);
        assertThat(jdbc.sql("SELECT sum(CASE WHEN direction='DEBIT' THEN amount_minor ELSE -amount_minor END) FROM journal_lines").query(Long.class).single()).isZero();
    }

    @Test void beneficiaryPaymentRequiresResolutionVerificationAndElapsedCooling() {
        var beneficiary=beneficiaries.create(issuer(),"oidc-customer-1","beneficiary-payment-key-01",beneficiaryRequest("123456789012"),"corr");
        var command=new CreatePaymentIntentRequest(sourceId,CreatePaymentIntentRequest.DestinationType.BENEFICIARY,beneficiary.id(),100,"INR","INTERNAL",null);
        assertThatThrownBy(()->payments.createIntent(identity(),command,"corr")).isInstanceOf(DomainException.class);
        beneficiaries.completeVerification(beneficiary.id(),true,"verified-internal",destinationId);
        assertThatThrownBy(()->payments.createIntent(identity(),command,"corr")).isInstanceOf(DomainException.class);
        jdbc.sql("UPDATE beneficiaries SET cooling_until=now()-interval '1 second' WHERE public_id=:id").param("id",beneficiary.id()).update();
        assertThat(payments.createIntent(identity(),command,"corr").destinationMaskedNumber()).isNotBlank();
    }

    @Test void paymentLimitsRejectExcessAndUnauthorizedIncrease() {
        jdbc.sql("INSERT INTO payment_limit_policies(customer_id,method,per_payment_minor,daily_minor) SELECT id,'INTERNAL',1000,1500 FROM customers WHERE identity_subject='oidc-customer-1'").update();
        assertThatThrownBy(()->payments.createIntent(identity(),new CreatePaymentIntentRequest(sourceId,CreatePaymentIntentRequest.DestinationType.OWN_ACCOUNT,destinationId,1001,"INR","INTERNAL",null),"corr")).isInstanceOf(DomainException.class);
        var first=payments.createIntent(identity(),new CreatePaymentIntentRequest(sourceId,CreatePaymentIntentRequest.DestinationType.OWN_ACCOUNT,destinationId,1000,"INR","INTERNAL",null),"corr");payments.confirm(identity(),first.id(),"payment-limit-key-001","corr");
        var second=payments.createIntent(identity(),new CreatePaymentIntentRequest(sourceId,CreatePaymentIntentRequest.DestinationType.OWN_ACCOUNT,destinationId,600,"INR","INTERNAL",null),"corr");
        assertThatThrownBy(()->payments.confirm(identity(),second.id(),"payment-limit-key-002","corr")).isInstanceOf(DomainException.class);
        assertThatThrownBy(()->payments.lowerCustomerLimit(identity(),2000,"corr")).isInstanceOf(DomainException.class);
    }

    @Test void futureScheduleIsDurableOwnedAndCancellable() {
        var schedule=schedules.create(identity(),new CreatePaymentScheduleRequest(sourceId,CreatePaymentIntentRequest.DestinationType.OWN_ACCOUNT,destinationId,500,"INR","Future",Instant.now().plusSeconds(3600),"Asia/Kolkata",CreatePaymentScheduleRequest.Recurrence.MONTHLY,null,12),"corr");
        assertThat(schedule.status()).isEqualTo("ACTIVE");assertThat(schedules.cancel(identity(),schedule.id(),"corr").status()).isEqualTo("CANCELLED");
        assertThatThrownBy(()->schedules.requireOwned(new FederatedIdentity(issuer(),"another-customer"),schedule.id())).isInstanceOf(DomainException.class);
    }

    @Test void fixedDepositConfirmationIsAtomicBalancedOwnedAndIdempotent() {
        var request=depositRequest("SANDBOX_FD_GOLD",100_000,3);
        var review=deposits.review(identity(),request,"FIXED");
        assertThat(review.annualRateBasisPoints()).isEqualTo(650);
        var opened=deposits.confirm(identity(),request,"FIXED","fixed-open-key-0001","corr");
        var replay=deposits.confirm(identity(),request,"FIXED","fixed-open-key-0001","corr");
        assertThat(replay.id()).isEqualTo(opened.id());
        assertThat(balance(sourceLedger)).isZero();
        assertThat(jdbc.sql("SELECT count(*) FROM deposit_accounts").query(Long.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("SELECT count(*) FROM journals").query(Long.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("SELECT sum(amount_minor) FILTER(WHERE direction='DEBIT')=sum(amount_minor) FILTER(WHERE direction='CREDIT') FROM journal_lines").query(Boolean.class).single()).isTrue();
        assertThatThrownBy(()->deposits.requireOwned(new FederatedIdentity(issuer(),"another-customer"),opened.id())).isInstanceOf(DomainException.class);
    }

    @Test void fixedDepositFailureRollsBackContractLedgerAndDebit() {
        var request=depositRequest("SANDBOX_FD_GOLD",100_000,3);
        jdbc.sql("CREATE FUNCTION fail_deposit_reference() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'simulated deposit failure'; END $$").update();
        jdbc.sql("CREATE TRIGGER test_fail_deposit_reference BEFORE INSERT ON deposit_posting_references FOR EACH ROW EXECUTE FUNCTION fail_deposit_reference()").update();
        assertThatThrownBy(()->deposits.confirm(identity(),request,"FIXED","fixed-rollback-key-01","corr")).isInstanceOf(RuntimeException.class);
        jdbc.sql("DROP TRIGGER test_fail_deposit_reference ON deposit_posting_references").update();jdbc.sql("DROP FUNCTION fail_deposit_reference()").update();
        assertThat(balance(sourceLedger)).isEqualTo(100_000);
        assertThat(jdbc.sql("SELECT count(*) FROM deposit_accounts").query(Long.class).single()).isZero();
        assertThat(jdbc.sql("SELECT count(*) FROM journals").query(Long.class).single()).isZero();
    }

    @Test void concurrentFixedConfirmationPostsExactlyOnce() throws Exception {
        var request=depositRequest("SANDBOX_FD_GOLD",100_000,3);String key="fixed-concurrent-key-01";
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()){
            var start=new CountDownLatch(1);
            var one=executor.submit(()->{start.await();return deposits.confirm(identity(),request,"FIXED",key,"corr");});
            var two=executor.submit(()->{start.await();return deposits.confirm(identity(),request,"FIXED",key,"corr");});
            start.countDown();assertThat(two.get(10,TimeUnit.SECONDS).id()).isEqualTo(one.get(10,TimeUnit.SECONDS).id());
        }
        assertThat(jdbc.sql("SELECT count(*) FROM journals").query(Long.class).single()).isEqualTo(1);
        assertThat(balance(sourceLedger)).isZero();
    }

    @Test void recurringScheduleAndConcurrentWorkersPostOccurrenceOnce() throws Exception {
        var request=depositRequest("SANDBOX_RD_GROWTH",50_000,6);
        var opened=deposits.confirm(identity(),request,"RECURRING","recurring-open-key-01","corr");
        assertThat(deposits.schedule(identity(),opened.id())).extracting("sequence").containsExactly(1,2,3,4,5,6);
        assertThat(jdbc.sql("SELECT count(DISTINCT idempotency_key)=count(*) FROM deposit_installment_occurrences").query(Boolean.class).single()).isTrue();
        jdbc.sql("UPDATE deposit_installment_occurrences SET scheduled_date=CURRENT_DATE WHERE deposit_account_id=(SELECT id FROM deposit_accounts WHERE public_id=:id) AND sequence_number=2").param("id",opened.id()).update();
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()){
            var start=new CountDownLatch(1);var one=executor.submit(()->{start.await();return installments.processOne();});var two=executor.submit(()->{start.await();return installments.processOne();});start.countDown();one.get(10,TimeUnit.SECONDS);two.get(10,TimeUnit.SECONDS);
        }
        assertThat(jdbc.sql("SELECT count(*) FROM deposit_installment_occurrences WHERE status='COMPLETED'").query(Long.class).single()).isEqualTo(2);
        assertThat(jdbc.sql("SELECT count(*) FROM deposit_installment_occurrences WHERE sequence_number=2 AND journal_id IS NOT NULL").query(Long.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("SELECT count(*) FROM journals").query(Long.class).single()).isEqualTo(2);
    }

    @Test void recurringInsufficientFundsNeverCompletesWithoutJournal() {
        var request=depositRequest("SANDBOX_RD_GROWTH",50_000,6);var opened=deposits.confirm(identity(),request,"RECURRING","recurring-funds-key-01","corr");
        holds.place("oidc-customer-1",sourceId,50_000,"INR","CARD_AUTHORIZATION","rd-funds-hold",Instant.now().plusSeconds(3600));
        jdbc.sql("UPDATE deposit_installment_occurrences SET scheduled_date=CURRENT_DATE-1 WHERE deposit_account_id=(SELECT id FROM deposit_accounts WHERE public_id=:id) AND sequence_number=2").param("id",opened.id()).update();
        assertThat(installments.processOne()).isTrue();
        assertThat(jdbc.sql("SELECT status FROM deposit_installment_occurrences WHERE sequence_number=2").query(String.class).single()).isEqualTo("SCHEDULED");
        assertThat(jdbc.sql("SELECT journal_id IS NULL FROM deposit_installment_occurrences WHERE sequence_number=2").query(Boolean.class).single()).isTrue();
        assertThat(jdbc.sql("SELECT count(*) FROM journals").query(Long.class).single()).isEqualTo(1);
    }

    @Test void loanQuotePersistsExactScheduleWithoutLedgerPosting() {
        var product = loans.products().stream().filter(p -> p.code().equals("SANDBOX_PERSONAL")).findFirst().orElseThrow();
        var quote = loans.quote(identity(), new QuoteRequest(product.id(), 500_000, 6));
        assertThat(quote.schedule()).hasSize(6);
        assertThat(quote.schedule().getLast().closingPrincipalMinor()).isZero();
        assertThat(quote.schedule().stream().mapToLong(item -> item.principalMinor()).sum()).isEqualTo(500_000);
        assertThat(loans.quoteSchedule(identity(), quote.id())).isEqualTo(quote.schedule());
        assertThat(jdbc.sql("SELECT count(*) FROM journals").query(Long.class).single()).isZero();
    }

    @Test void loanApplicationIsOwnedIdempotentAndNonPosting() {
        var product = loans.products().getFirst();
        var quote = loans.quote(identity(), new QuoteRequest(product.id(), product.minimumPrincipalMinor(), product.minimumTenureMonths()));
        var request = new ApplicationRequest(quote.id(), "PERSONAL", true);
        var application = loans.apply(identity(), request, "loan-application-key-0001", "corr-loan");
        assertThat(loans.apply(identity(), request, "loan-application-key-0001", "corr-loan").id()).isEqualTo(application.id());
        assertThat(application.status()).isEqualTo("SUBMITTED");
        assertThat(application.bookingAvailable()).isFalse();
        assertThatThrownBy(() -> loans.requireApplication(new FederatedIdentity(issuer(), "another-customer"), application.id())).isInstanceOf(DomainException.class);
        assertThat(jdbc.sql("SELECT count(*) FROM loan_accounts").query(Long.class).single()).isZero();
        assertThat(jdbc.sql("SELECT count(*) FROM journals").query(Long.class).single()).isZero();
    }

    @Test void loanApplicationEndpointRequiresCsrfAndRecentProviderMfa() throws Exception {
        var product = loans.products().getFirst();
        var quote = loans.quote(identity(), new QuoteRequest(product.id(), product.minimumPrincipalMinor(), product.minimumTenureMonths()));
        String body = "{\"quoteId\":\"" + quote.id() + "\",\"purposeCategory\":\"PERSONAL\",\"customerDeclaration\":true}";
        mockMvc.perform(post("/api/v1/loans/applications").with(customerLogin()).header("Idempotency-Key", "loan-http-key-000001").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/loans/applications").with(customerLogin()).with(csrf()).header("Idempotency-Key", "loan-http-key-000002").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
    }

    @Test void statementBalancesDirectionsPaginationAndExportsReconcile() {
        var transfer=transfers.execute("oidc-customer-1","statement-transfer-key-01",request(10_000),"corr");
        var today=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        var page=statements.transactions(identity(),sourceId,today,today,"","","","newest",0,1);
        assertThat(page.summary().openingBalanceMinor()).isEqualTo(100_000);
        assertThat(page.summary().totalDebitMinor()).isEqualTo(10_000);
        assertThat(page.summary().totalCreditMinor()).isZero();
        assertThat(page.summary().closingBalanceMinor()).isEqualTo(90_000);
        assertThat(page.items()).singleElement().satisfies(e->{assertThat(e.debitMinor()).isEqualTo(10_000);assertThat(e.runningBalanceMinor()).isEqualTo(90_000);});
        var generated=statements.generate(identity(),sourceId,today,today,"statement-generate-key-01","corr");
        assertThat(statements.generate(identity(),sourceId,today,today,"statement-generate-key-01","corr").id()).isEqualTo(generated.id());
        assertThat(jdbc.sql("SELECT count(*) FROM statement_generations").query(Long.class).single()).isEqualTo(1);
        var snapshot=statements.snapshot(identity(),generated.id());
        assertThat(statementExports.statementPdf(snapshot)).startsWith((byte)'%',(byte)'P',(byte)'D',(byte)'F');
        assertThat(new String(statementExports.csv(snapshot),java.nio.charset.StandardCharsets.UTF_8)).contains("Transaction Reference",transfer.reference());
        assertThat(statements.receipt(identity(),page.items().getFirst().id()).status()).isEqualTo("COMPLETED");
    }

    @Test void statementAndReceiptOwnershipAreNonEnumerating() {
        transfers.execute("oidc-customer-1","statement-owner-key-001",request(500),"corr");
        var today=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        var generated=statements.generate(identity(),sourceId,today,today,"statement-owner-generate-01","corr");
        var other=new FederatedIdentity(issuer(),"another-customer");
        assertThatThrownBy(()->statements.snapshot(other,generated.id())).isInstanceOf(DomainException.class);
        assertThatThrownBy(()->statements.transactions(other,sourceId,today,today,null,null,null,"newest",0,25)).isInstanceOf(DomainException.class);
    }

    @Test void statementGenerationRequiresCsrf() throws Exception {
        var today=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        String body="{\"accountId\":\""+sourceId+"\",\"from\":\""+today+"\",\"to\":\""+today+"\"}";
        mockMvc.perform(post("/api/v1/statements/generations").with(customerLogin()).header("Idempotency-Key","statement-http-key-0001").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/statements/generations").with(customerLogin()).with(csrf()).header("Idempotency-Key","statement-http-key-0002").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/statements/generations").with(customerLogin()).with(csrf()).header("Idempotency-Key","statement-http-key-0003").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/statements/generations").with(customerLogin()).with(csrf()).header("Idempotency-Key","statement-http-key-0004").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isTooManyRequests()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().exists("Retry-After"));
    }

    @Test void historicalStatementCutoffSurvivesLaterReversalAndChecksumValidation() {
        var transfer=transfers.execute("oidc-customer-1","historical-cutoff-key-01",request(10_000),"corr");
        var today=java.time.LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        var first=statements.generate(identity(),sourceId,today,today,"historical-statement-a-01","corr");
        var snapshotA=statements.snapshot(identity(),first.id());
        byte[] csvA=statementExports.csv(snapshotA);
        long journal=jdbc.sql("SELECT journal_id FROM transfers WHERE public_id=:id").param("id",transfer.id()).query(Long.class).single();
        ledger.reverse(journal,"historical-reversal-batch","historical-reversal-journal");
        var second=statements.generate(identity(),sourceId,today,today,"historical-statement-b-01","corr");
        var snapshotAAgain=statements.snapshot(identity(),first.id());
        var snapshotB=statements.snapshot(identity(),second.id());
        assertThat(snapshotAAgain.summary()).isEqualTo(snapshotA.summary());
        assertThat(statementExports.csv(snapshotAAgain)).isEqualTo(csvA);
        assertThat(snapshotA.summary().closingBalanceMinor()).isEqualTo(90_000);
        assertThat(snapshotB.summary().closingBalanceMinor()).isEqualTo(100_000);
        assertThat(snapshotB.summary().totalDebitMinor()).isEqualTo(10_000);
        assertThat(snapshotB.summary().totalCreditMinor()).isEqualTo(10_000);
        assertThat(snapshotB.entries()).extracting("transactionType").containsExactlyInAnyOrder("TRANSFER","REVERSAL");
        jdbc.sql("UPDATE statement_generations SET content_checksum=repeat('0',64) WHERE public_id=:id").param("id",first.id()).update();
        assertThatThrownBy(()->statements.snapshot(identity(),first.id())).isInstanceOf(DomainException.class).hasMessageContaining("integrity");
    }

    @Test void fundedDepositHasReceiptButUnpostedResourcesDoNot() {
        var opened=deposits.confirm(identity(),depositRequest("SANDBOX_FD_GOLD",100_000,3),"FIXED","receipt-deposit-key-001","corr");
        UUID journal=jdbc.sql("SELECT j.public_id FROM deposit_posting_references d JOIN journals j ON j.id=d.journal_id JOIN deposit_accounts a ON a.id=d.deposit_account_id WHERE a.public_id=:id").param("id",opened.id()).query(UUID.class).single();
        assertThat(statements.receipt(identity(),journal).status()).isEqualTo("COMPLETED");
        assertThatThrownBy(()->statements.receipt(identity(),UUID.randomUUID())).isInstanceOf(DomainException.class);
    }

    @Test void profileAllowsOnlyVersionedLowRiskDisplayNameChange() {
        var before=profiles.view(identity());
        var changed=profiles.update(identity(),new ProfileService.ProfileUpdate("Fictional Preferred",before.version()));
        assertThat(changed.preferredDisplayName()).isEqualTo("Fictional Preferred");
        assertThatThrownBy(()->profiles.update(identity(),new ProfileService.ProfileUpdate("Stale",before.version())))
            .isInstanceOf(DomainException.class).hasMessageContaining("changed");
        assertThat(changed.registeredEmail()).isNull();
    }

    @Test void serviceRequestsAreOwnedDurableAndIdempotent() {
        var request=new ServiceRequestService.Create("CONTACT_UPDATE_ASSISTANCE","Please explain the approved contact update process.");
        var created=serviceRequests.create(identity(),request,"service-request-key-0001");
        assertThat(serviceRequests.create(identity(),request,"service-request-key-0001").id()).isEqualTo(created.id());
        assertThat(serviceRequests.history(identity(),created.id())).extracting("status").containsExactly("SUBMITTED");
        assertThatThrownBy(()->serviceRequests.one(new FederatedIdentity(issuer(),"another-customer"),created.id())).isInstanceOf(DomainException.class);
    }

    @Test void concurrentServiceRequestRetryCreatesOneRequestAndOneHistoryEvent() throws Exception {
        var request=new ServiceRequestService.Create("ACCOUNT_INFORMATION","Please explain the available account information.");
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()){
            var start=new CountDownLatch(1);
            var one=executor.submit(()->{start.await();return serviceRequests.create(identity(),request,"service-concurrent-key-01");});
            var two=executor.submit(()->{start.await();return serviceRequests.create(identity(),request,"service-concurrent-key-01");});
            start.countDown();assertThat(one.get(10,TimeUnit.SECONDS).id()).isEqualTo(two.get(10,TimeUnit.SECONDS).id());
        }
        assertThat(jdbc.sql("SELECT count(*) FROM service_requests").query(Long.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("SELECT count(*) FROM service_request_status_events").query(Long.class).single()).isEqualTo(1);
        assertThat(jdbc.sql("SELECT count(*) FROM customer_notifications").query(Long.class).single()).isEqualTo(1);
    }

    @Test void serviceRequestPaginationSearchAndSensitiveDescriptionProtection() {
        serviceRequests.create(identity(),new ServiceRequestService.Create("ACCOUNT_INFORMATION","Please provide safe account information."),"service-page-key-00001");
        serviceRequests.create(identity(),new ServiceRequestService.Create("CARD_SUPPORT","Please provide safe card support guidance."),"service-page-key-00002");
        var page=serviceRequests.page(identity(),0,1,"card","newest");
        assertThat(page.totalElements()).isEqualTo(1);assertThat(page.items()).singleElement().extracting("category").isEqualTo("CARD_SUPPORT");
        assertThatThrownBy(()->serviceRequests.create(identity(),new ServiceRequestService.Create("GENERAL_ACCOUNT_SUPPORT","My OTP is 123456, please investigate."),"service-sensitive-key-1")).isInstanceOf(DomainException.class).hasMessageContaining("Remove");
    }

    @Test void notificationsProjectCommittedOutboxOnceAndEnforceOwnership() {
        transfers.execute("oidc-customer-1","notification-transfer-key-01",request(500),"corr");
        assertThat(notifications.list(identity(),0,25)).extracting("title").contains("Transfer completed");
        assertThat(notifications.list(identity(),0,25).stream().filter(n->n.title().equals("Transfer completed"))).hasSize(1);
        UUID id=notifications.list(identity(),0,25).getFirst().id();
        assertThatThrownBy(()->notifications.read(new FederatedIdentity(issuer(),"another-customer"),id)).isInstanceOf(DomainException.class);
    }

    @Test void phaseHMutationsRequireCsrfAndSensitiveContactFieldsAreNotAccepted() throws Exception {
        mockMvc.perform(patch("/api/v1/profile").with(customerLogin()).contentType(MediaType.APPLICATION_JSON).content("{\"preferredDisplayName\":\"Safe name\",\"version\":0}")).andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/v1/profile").with(customerLogin()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"preferredDisplayName\":\"Safe name\",\"registeredEmail\":\"attacker@example.test\",\"emailVerified\":true,\"version\":0}")).andExpect(status().isOk());
        assertThat(jdbc.sql("SELECT registered_email IS NULL AND email_verified=false FROM customers WHERE identity_subject='oidc-customer-1'").query(Boolean.class).single()).isTrue();
    }

    private org.springframework.test.web.servlet.request.RequestPostProcessor customerLogin(){
        return oidcLogin().idToken(token -> token.issuer("http://localhost:8081/realms/novara").subject("oidc-customer-1"))
            .authorities(createAuthorityList("ROLE_CUSTOMER"));
    }

    private boolean attempt(CountDownLatch start,String key,long amount)throws InterruptedException{start.await();try{transfers.execute("oidc-customer-1",key,request(amount),"corr");return true;}catch(DomainException expected){return false;}}
    private boolean attemptReversal(CountDownLatch start,long journalId,String batch,String journal)throws InterruptedException{start.await();try{ledger.reverse(journalId,batch,journal);return true;}catch(DomainException expected){return false;}}
    private InternalTransferRequest request(long amount){return new InternalTransferRequest(sourceId,destinationId,amount,"INR","test");}
    private CreateBeneficiaryRequest beneficiaryRequest(String account){return new CreateBeneficiaryRequest("Fictional Beneficiary","Test",account,"NOVA0001","Novara Bank","INTERNAL");}
    private String issuer(){return "http://localhost:8081/realms/novara";}
    private FederatedIdentity identity(){return new FederatedIdentity(issuer(),"oidc-customer-1");}
    private ReviewRequest depositRequest(String code,long amount,int tenure){UUID product=jdbc.sql("SELECT public_id FROM deposit_products WHERE code=:code").param("code",code).query(UUID.class).single();return new ReviewRequest(product,amount,tenure,sourceId,"CREDIT_LINKED_ACCOUNT");}
    private long balance(long ledgerId){return jdbc.sql("SELECT balance_minor FROM ledger_balances WHERE ledger_account_id=:id").param("id",ledgerId).query(Long.class).single();}
    private UUID seedCard(String status){
        long customer=jdbc.sql("SELECT id FROM customers WHERE identity_subject='oidc-customer-1'").query(Long.class).single();
        long account=jdbc.sql("SELECT id FROM bank_accounts WHERE public_id=:id").param("id",sourceId).query(Long.class).single();
        long product=jdbc.sql("SELECT id FROM card_products WHERE code='NOVARA_GOLD'").query(Long.class).single();
        long card=jdbc.sql("INSERT INTO cards(product_id,customer_id,linked_account_id,cardholder_name,masked_reference,processor_reference,status,expiry_month,expiry_year) VALUES(:p,:c,:a,'FICTIONAL CUSTOMER','**** 4821',:processor,:status,9,2029) RETURNING id")
            .param("p",product).param("c",customer).param("a",account).param("processor","sandbox-"+UUID.randomUUID()).param("status",status).query(Long.class).single();
        jdbc.sql("INSERT INTO card_controls(card_id) VALUES(:id)").param("id",card).update();
        return jdbc.sql("SELECT public_id FROM cards WHERE id=:id").param("id",card).query(UUID.class).single();
    }
    private SeedAccount createAccount(String code,String hash,String masked,long balance){
        long ledgerId=jdbc.sql("INSERT INTO ledger_accounts(code,name,classification,currency) VALUES(:code,:code,'LIABILITY','INR') RETURNING id").param("code",code).query(Long.class).single();
        jdbc.sql("INSERT INTO ledger_balances(ledger_account_id,balance_minor) VALUES(:id,:balance)").param("id",ledgerId).param("balance",balance).update();
        return jdbc.sql("INSERT INTO bank_accounts(account_number_ciphertext,account_number_hash,masked_number,nickname,type,currency,ledger_account_id) VALUES(decode('00','hex'),:hash,:masked,:code,'SAVINGS','INR',:ledger) RETURNING id,public_id").param("hash",String.format("%-64s",hash).replace(' ','0')).param("masked",masked).param("code",code).param("ledger",ledgerId).query((rs,row)->new SeedAccount(rs.getLong(1),rs.getObject(2,UUID.class),ledgerId)).single();
    }
    private record SeedAccount(long accountId,UUID publicId,long ledgerId){}
}
