package com.novara.banking.card;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.util.UUID;
@Component @ConditionalOnProperty(name="novara.cards.processor-mode",havingValue="sandbox") public class SandboxCardProcessor implements CardProcessor {
 public Result changeStatus(String ref,String status){return new Result(true,"sandbox_"+UUID.randomUUID(),null);}
 public Result updateControls(String ref,CardCommands.Controls controls){return new Result(true,"sandbox_"+UUID.randomUUID(),null);}
}
