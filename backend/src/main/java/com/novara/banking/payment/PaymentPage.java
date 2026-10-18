package com.novara.banking.payment;

import java.util.List;
public record PaymentPage(List<PaymentView> items,int page,int size,long total) {}
