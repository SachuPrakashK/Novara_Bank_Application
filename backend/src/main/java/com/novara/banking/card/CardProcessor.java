package com.novara.banking.card;
public interface CardProcessor {
    Result changeStatus(String processorReference,String targetStatus);
    Result updateControls(String processorReference,CardCommands.Controls controls);
    record Result(boolean acknowledged,String reference,String failureCode){}
}
