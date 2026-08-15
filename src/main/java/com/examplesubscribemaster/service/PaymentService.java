package com.examplesubscribemaster.service;

import com.examplesubscribemaster.dto.payment.*;

public interface PaymentService {
    PaymentResponse pay(Long userId, Long subscriptionId, PaymentRequest request);
}
