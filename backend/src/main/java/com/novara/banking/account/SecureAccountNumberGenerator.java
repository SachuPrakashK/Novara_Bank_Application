package com.novara.banking.account;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;
@Component
public class SecureAccountNumberGenerator implements AccountNumberGenerator {
    private final SecureRandom random=new SecureRandom();
    @Override public String nextAccountNumber(){var value=new StringBuilder("91");for(int i=0;i<12;i++)value.append(random.nextInt(10));return value.toString();}
}
