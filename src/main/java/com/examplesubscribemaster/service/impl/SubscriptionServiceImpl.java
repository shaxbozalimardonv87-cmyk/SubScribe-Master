package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.dto.subscription.*;
import com.examplesubscribemaster.entity.*;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import com.examplesubscribemaster.exception.ResourceNotFoundException;
import com.examplesubscribemaster.mapper.SubscriptionMapper;
import com.examplesubscribemaster.repository.*;
import com.examplesubscribemaster.service.SubscriptionService;
import com.examplesubscribemaster.specification.SubscriptionSpecifications;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    private final SubscriptionRepository repo;
    private final UserRepository users;
    private final SubscriptionMapper mapper;

    public SubscriptionServiceImpl(SubscriptionRepository r, UserRepository u, SubscriptionMapper m) {
        repo = r;
        users = u;
        mapper = m;
    }

    @Transactional
    public SubscriptionResponse create(Long userId, SubscriptionRequest r) {
        User u = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return mapper.toResponse(repo.save(new Subscription(r.name(), r.price(), r.currency(), r.startDate(), r.periodicity(), r.status(), r.category(), u)));
    }

    @Transactional(readOnly = true)
    public PageResponse<SubscriptionResponse> findAll(Long userId, SubscriptionStatus status, Currency currency, BigDecimal min, BigDecimal max, Pageable p) {
        Specification<Subscription> s = Specification.allOf(SubscriptionSpecifications.ownedBy(userId), SubscriptionSpecifications.notDeleted(), SubscriptionSpecifications.status(status), SubscriptionSpecifications.currency(currency), SubscriptionSpecifications.min(min), SubscriptionSpecifications.max(max));
        Page<Subscription> page = repo.findAll(s, p);
        return new PageResponse<>(page.map(mapper::toResponse).getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    private Subscription owned(Long u, Long id) {
        return repo.findByIdAndUserIdAndIsDeletedFalse(id, u).orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse find(Long u, Long id) {
        return mapper.toResponse(owned(u, id));
    }

    @Transactional
    public SubscriptionResponse update(Long u, Long id, SubscriptionRequest r) {
        Subscription s = owned(u, id);
        s.update(r.name(), r.price(), r.currency(), r.startDate(), r.periodicity(), r.status(), r.category());
        return mapper.toResponse(s);
    }

    @Transactional
    public void delete(Long u, Long id) {
        owned(u, id).softDelete();
    }
}
