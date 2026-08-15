package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.dto.subscription.PageResponse;
import com.examplesubscribemaster.dto.subscription.SubscriptionRequest;
import com.examplesubscribemaster.dto.subscription.SubscriptionResponse;
import com.examplesubscribemaster.entity.Subscription;
import com.examplesubscribemaster.entity.User;
import com.examplesubscribemaster.enums.Category;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.Periodicity;
import com.examplesubscribemaster.enums.Role;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import com.examplesubscribemaster.exception.ResourceNotFoundException;
import com.examplesubscribemaster.mapper.SubscriptionMapper;
import com.examplesubscribemaster.repository.SubscriptionRepository;
import com.examplesubscribemaster.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock private SubscriptionRepository repo;
    @Mock private UserRepository users;
    @Mock private SubscriptionMapper mapper;

    private SubscriptionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SubscriptionServiceImpl(repo, users, mapper);
    }

    private User user() {
        return new User("user@example.com", "hash", Role.USER);
    }

    private Subscription subscription() {
        return new Subscription("Netflix", new BigDecimal("10.00"), Currency.USD, LocalDate.of(2026, 1, 1),
            Periodicity.MONTHLY, SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT, user());
    }

    @Test
    void create_ownerExists_savesAndReturnsMappedResponse() {
        SubscriptionRequest request = new SubscriptionRequest("Netflix", new BigDecimal("10.00"), Currency.USD,
            LocalDate.of(2026, 1, 1), Periodicity.MONTHLY, SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT);
        User owner = user();
        when(users.findById(1L)).thenReturn(Optional.of(owner));
        when(repo.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));
        SubscriptionResponse expected = new SubscriptionResponse(1L, "Netflix", request.price(), Currency.USD,
            request.startDate(), Periodicity.MONTHLY.next(request.startDate()), Periodicity.MONTHLY,
            SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT, 0L);
        when(mapper.toResponse(any(Subscription.class))).thenReturn(expected);

        SubscriptionResponse response = service.create(1L, request);

        assertThat(response).isEqualTo(expected);
        verify(repo).save(any(Subscription.class));
    }

    @Test
    void create_ownerMissing_throwsResourceNotFound() {
        SubscriptionRequest request = new SubscriptionRequest("Netflix", new BigDecimal("10.00"), Currency.USD,
            LocalDate.of(2026, 1, 1), Periodicity.MONTHLY, SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT);
        when(users.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(99L, request)).isInstanceOf(ResourceNotFoundException.class);
        verify(repo, never()).save(any());
    }

    @Test
    void find_notOwnedByCaller_throwsResourceNotFound() {
        when(repo.findByIdAndUserIdAndIsDeletedFalse(5L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(1L, 5L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void find_ownedByCaller_returnsMappedResponse() {
        Subscription entity = subscription();
        SubscriptionResponse mapped = new SubscriptionResponse(1L, "Netflix", entity.getPrice(), entity.getCurrency(),
            entity.getStartDate(), entity.getNextPaymentDate(), entity.getPeriodicity(), entity.getStatus(),
            entity.getCategory(), 0L);
        when(repo.findByIdAndUserIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(mapped);

        assertThat(service.find(1L, 1L)).isEqualTo(mapped);
    }

    @Test
    void update_ownedByCaller_recalculatesNextPaymentDate() {
        Subscription entity = subscription();
        LocalDate newStart = LocalDate.of(2026, 6, 1);
        SubscriptionRequest request = new SubscriptionRequest("Netflix Premium", new BigDecimal("15.00"), Currency.USD,
            newStart, Periodicity.YEARLY, SubscriptionStatus.ACTIVE, Category.ENTERTAINMENT);
        when(repo.findByIdAndUserIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenAnswer(inv -> new SubscriptionResponse(1L, entity.getName(),
            entity.getPrice(), entity.getCurrency(), entity.getStartDate(), entity.getNextPaymentDate(),
            entity.getPeriodicity(), entity.getStatus(), entity.getCategory(), 0L));

        SubscriptionResponse response = service.update(1L, 1L, request);

        assertThat(response.name()).isEqualTo("Netflix Premium");
        assertThat(response.nextPaymentDate()).isEqualTo(newStart.plusYears(1));
    }

    @Test
    void delete_ownedByCaller_softDeletesWithoutPhysicalRemoval() {
        Subscription entity = subscription();
        when(repo.findByIdAndUserIdAndIsDeletedFalse(1L, 1L)).thenReturn(Optional.of(entity));

        service.delete(1L, 1L);

        assertThat(entity.isDeleted()).isTrue();
        verify(repo, never()).delete(any(Subscription.class));
        verify(repo, never()).deleteById(any());
    }

    @Test
    void delete_notOwnedByCaller_throwsResourceNotFound() {
        when(repo.findByIdAndUserIdAndIsDeletedFalse(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(2L, 1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_delegatesToRepositoryWithSpecificationAndPageable() {
        Pageable pageable = PageRequest.of(0, 20);
        Subscription entity = subscription();
        Page<Subscription> page = new PageImpl<>(List.of(entity), pageable, 1);
        when(repo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponse(entity)).thenReturn(new SubscriptionResponse(1L, entity.getName(), entity.getPrice(),
            entity.getCurrency(), entity.getStartDate(), entity.getNextPaymentDate(), entity.getPeriodicity(),
            entity.getStatus(), entity.getCategory(), 0L));

        PageResponse<SubscriptionResponse> result = service.findAll(1L, SubscriptionStatus.ACTIVE, Currency.USD,
            null, null, pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        verify(repo).findAll(any(Specification.class), any(Pageable.class));
    }
}
