package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.dto.payment.*;
import com.examplesubscribemaster.entity.*;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import com.examplesubscribemaster.exception.*;
import com.examplesubscribemaster.repository.*;
import com.examplesubscribemaster.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final SubscriptionRepository subscriptions;
    private final PaymentHistoryRepository payments;

    public PaymentServiceImpl(SubscriptionRepository s, PaymentHistoryRepository p) {
        subscriptions = s;
        payments = p;
    }

    @Transactional
    public PaymentResponse pay(Long userId, Long id, PaymentRequest r) {
        Subscription s = subscriptions.findByIdAndUserIdAndIsDeletedFalse(id, userId).orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
        if (s.getStatus() == SubscriptionStatus.CANCELLED)
            throw new BusinessException("Cancelled subscription cannot be paid");
        PaymentHistory p = payments.save(new PaymentHistory(s, r.amount(), s.getCurrency(), r.paymentDate()));
        s.markPaid();
        return new PaymentResponse(p.getId(), s.getId(), p.getAmount(), p.getCurrency(), p.getPaymentDate(), s.getNextPaymentDate());
    }
}
