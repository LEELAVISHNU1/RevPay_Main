package com.revpay.service.interfaces;

import java.util.List;
import com.revpay.entity.MoneyRequest;

public interface RequestService {

    void createRequest(String senderEmail, Double amount, String note);

    void acceptRequest(Long requestId);

    void rejectRequest(Long requestId);

    List<MoneyRequest> getIncomingRequests();

    List<MoneyRequest> mySentRequests();
}