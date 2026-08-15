package com.examplesubscribemaster.controller;

import com.examplesubscribemaster.dto.payment.*;
import com.examplesubscribemaster.security.UserPrincipal;
import com.examplesubscribemaster.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions/{id}/payments")
public class PaymentController {
    private final PaymentService s;

    public PaymentController(PaymentService s) {
        this.s = s;
    }

    @PostMapping
    PaymentResponse pay(Authentication a, @PathVariable Long id, @Valid @RequestBody PaymentRequest r) {
        return s.pay(((UserPrincipal) a.getPrincipal()).id(), id, r);
    }
}
