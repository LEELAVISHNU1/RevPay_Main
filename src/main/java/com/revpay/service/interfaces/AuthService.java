package com.revpay.service.interfaces;

import com.revpay.dto.request.LoginRequest;
import com.revpay.dto.request.RegisterRequest;
import com.revpay.entity.User;

public interface AuthService {

    void register(RegisterRequest request);


}
