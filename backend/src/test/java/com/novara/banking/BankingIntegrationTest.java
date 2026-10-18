package com.novara.banking;

import com.novara.banking.ledger.*;
import com.novara.banking.account.AccountHoldService;
import com.novara.banking.reconciliation.ReconciliationService;
import com.novara.banking.shared.DomainException;
import com.novara.banking.transfer.InternalTransferRequest;
import com.novara.banking.transfer.InternalTransferService;
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
    @DynamicPropertySource static void database(DynamicPropertyRegistry registry){registry.add("spring.datasource.url",POSTGRES::getJdbcUrl);registry.add("spring.datasource.username",POSTGRES::getUsername);registry.add("spring.datasource.password",POSTGRES::getPassword);}
    @MockBean JwtDecoder jwtDecoder;
    @Autowired JdbcClient jdbc; @Autowired LedgerPostingService ledger; @Autowired InternalTransferService transfers; @Autowired ReconciliationService reconciliation; @Autowired AccountHoldService holds; @Autowired MockMvc mockMvc;
    UUID sourceId,destinationId; long sourceLedger,destinationLedger;

    @BeforeEach void seed(){
        jdbc.sql("TRUNCATE reconciliation_exceptions,reconciliation_runs,outbox_events,audit_events,idempotency_records,transfers,account_holds,journal_lines,journals,posting_batches,account_owners,bank_accounts,ledger_balances,ledger_accounts,customers RESTART IDENTITY CASCADE").update();
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

    private org.springframework.test.web.servlet.request.RequestPostProcessor customerLogin(){
        return oidcLogin().idToken(token -> token.issuer("http://localhost:8081/realms/novara").subject("oidc-customer-1"))
            .authorities(createAuthorityList("ROLE_CUSTOMER"));
    }

    private boolean attempt(CountDownLatch start,String key,long amount)throws InterruptedException{start.await();try{transfers.execute("oidc-customer-1",key,request(amount),"corr");return true;}catch(DomainException expected){return false;}}
    private boolean attemptReversal(CountDownLatch start,long journalId,String batch,String journal)throws InterruptedException{start.await();try{ledger.reverse(journalId,batch,journal);return true;}catch(DomainException expected){return false;}}
    private InternalTransferRequest request(long amount){return new InternalTransferRequest(sourceId,destinationId,amount,"INR","test");}
    private long balance(long ledgerId){return jdbc.sql("SELECT balance_minor FROM ledger_balances WHERE ledger_account_id=:id").param("id",ledgerId).query(Long.class).single();}
    private SeedAccount createAccount(String code,String hash,String masked,long balance){
        long ledgerId=jdbc.sql("INSERT INTO ledger_accounts(code,name,classification,currency) VALUES(:code,:code,'LIABILITY','INR') RETURNING id").param("code",code).query(Long.class).single();
        jdbc.sql("INSERT INTO ledger_balances(ledger_account_id,balance_minor) VALUES(:id,:balance)").param("id",ledgerId).param("balance",balance).update();
        return jdbc.sql("INSERT INTO bank_accounts(account_number_ciphertext,account_number_hash,masked_number,nickname,type,currency,ledger_account_id) VALUES(decode('00','hex'),:hash,:masked,:code,'SAVINGS','INR',:ledger) RETURNING id,public_id").param("hash",String.format("%-64s",hash).replace(' ','0')).param("masked",masked).param("code",code).param("ledger",ledgerId).query((rs,row)->new SeedAccount(rs.getLong(1),rs.getObject(2,UUID.class),ledgerId)).single();
    }
    private record SeedAccount(long accountId,UUID publicId,long ledgerId){}
}
