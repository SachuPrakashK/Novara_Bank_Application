package com.novara.banking.card;
import jakarta.validation.constraints.*;
public final class CardCommands { private CardCommands(){}
 public record Controls(boolean atmEnabled,boolean onlineEnabled,boolean posEnabled,boolean contactlessEnabled,boolean internationalEnabled,
  @PositiveOrZero @Max(1000000000) long atmLimitMinor,@PositiveOrZero @Max(1000000000) long onlineLimitMinor,@PositiveOrZero @Max(1000000000) long posLimitMinor,@PositiveOrZero @Max(1000000000) long contactlessLimitMinor,@Positive @Max(1000000000) long dailyLimitMinor,@PositiveOrZero long version){
   public Controls(boolean atm,boolean online,boolean pos,boolean contactless,boolean international,long atmLimit,long onlineLimit,long posLimit,long contactlessLimit,long dailyLimit){this(atm,online,pos,contactless,international,atmLimit,onlineLimit,posLimit,contactlessLimit,dailyLimit,0);}
  }
 public record ServiceRequest(@Pattern(regexp="LOST|DAMAGED|REPLACEMENT|SUPPORT") String requestType,@Size(max=240) String notes){}
}
