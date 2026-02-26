package com.revpay.service.interfaces;

import com.revpay.entity.PaymentMethod;

import java.util.List;

public interface PaymentMethodService {

    void addCard(String number, String holder, String expiry, String cvv);

    List<PaymentMethod> myCards();

    void deleteCard(Long id);
    
    
}
