package com.novara.banking.statement;

import com.lowagie.text.pdf.PdfReader;
import com.novara.banking.statement.StatementModels.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatementExportServiceTest {
 @Test void csvNeutralizesSpreadsheetFormulaTriggersAndQuotesContent(){
  assertThat(StatementExportService.csv("=HYPERLINK(\"bad\")")).isEqualTo("\"'=HYPERLINK(\"\"bad\"\")\"");
  assertThat(StatementExportService.csv("+1")).startsWith("\"'+");
  assertThat(StatementExportService.csv("ordinary, value")).isEqualTo("\"ordinary, value\"");
  assertThat(StatementExportService.csv("  =SUM(A1:A2)")).startsWith("\"'  =");
  assertThat(StatementExportService.csv("\t=SUM(A1:A2)")).startsWith("\"'\t=");
  assertThat(StatementExportService.csv("\r@attack")).startsWith("\"'\r@");
 }

 @Test void statementPdfIsValidMultipageAndRejectsUnsupportedGlyphs()throws Exception{
  var service=new StatementExportService();UUID account=UUID.randomUUID(),statement=UUID.randomUUID();Instant now=Instant.parse("2026-10-25T10:00:00Z");List<Entry> entries=new ArrayList<>();for(int i=1;i<=160;i++)entries.add(new Entry(UUID.randomUUID(),"TRF-"+i,now,LocalDate.of(2026,10,25),"Internal transfer "+i,"TRANSFER",100,0,100_000-i*100L,"INR","POSTED",true));var summary=new Summary(account,"•••• 1001","SAVINGS","INR",LocalDate.of(2026,10,1),LocalDate.of(2026,10,25),now,100_000,16_000,0,84_000,160);var snapshot=new StatementService.Snapshot(statement,summary,entries,"Fictional Customer");byte[] pdf=service.statementPdf(snapshot);var reader=new PdfReader(pdf);assertThat(reader.getNumberOfPages()).isGreaterThan(1);assertThat(reader.getInfo().get("Title")).isEqualTo("Novara Bank Account Statement");reader.close();var unsafe=new StatementService.Snapshot(statement,summary,entries,"ग्राहक");assertThatThrownBy(()->service.statementPdf(unsafe)).isInstanceOf(com.novara.banking.shared.DomainException.class);
 }
}
