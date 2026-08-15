package com.examplesubscribemaster.controller;

import com.examplesubscribemaster.dto.subscription.*;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import com.examplesubscribemaster.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {
    private final SubscriptionService s;

    public SubscriptionController(SubscriptionService s) {
        this.s = s;
    }

    private Long uid(Authentication a) {
        return ((com.examplesubscribemaster.security.UserPrincipal) a.getPrincipal()).id();
    }

    @PostMapping
    ResponseEntity<SubscriptionResponse> create(Authentication a, @Valid @RequestBody SubscriptionRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(s.create(uid(a), r));
    }

    @GetMapping
    PageResponse<SubscriptionResponse> all(Authentication a, @RequestParam(required = false) SubscriptionStatus status, @RequestParam(required = false) Currency currency, @RequestParam(required = false) java.math.BigDecimal minPrice, @RequestParam(required = false) java.math.BigDecimal maxPrice, Pageable p) {
        return s.findAll(uid(a), status, currency, minPrice, maxPrice, p);
    }

    @GetMapping("/{id}")
    SubscriptionResponse one(Authentication a, @PathVariable Long id) {
        return s.find(uid(a), id);
    }

    @PutMapping("/{id}")
    SubscriptionResponse update(Authentication a, @PathVariable Long id, @Valid @RequestBody SubscriptionRequest r) {
        return s.update(uid(a), id, r);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(Authentication a, @PathVariable Long id) {
        s.delete(uid(a), id);
        return ResponseEntity.noContent().build();
    }
}
