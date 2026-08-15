package com.examplesubscribemaster.specification;

import com.examplesubscribemaster.entity.*;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class SubscriptionSpecifications {
    private SubscriptionSpecifications() {
    }

    public static Specification<Subscription> ownedBy(Long userId) {
        return (r, q, c) -> c.equal(r.get("user").get("id"), userId);
    }

    public static Specification<Subscription> notDeleted() {
        return (r, q, c) -> c.isFalse(r.get("isDeleted"));
    }

    public static Specification<Subscription> status(SubscriptionStatus s) {
        return (r, q, c) -> s == null ? null : c.equal(r.get("status"), s);
    }

    public static Specification<Subscription> currency(Currency x) {
        return (r, q, c) -> x == null ? null : c.equal(r.get("currency"), x);
    }

    public static Specification<Subscription> min(BigDecimal x) {
        return (r, q, c) -> x == null ? null : c.greaterThanOrEqualTo(r.get("price"), x);
    }

    public static Specification<Subscription> max(BigDecimal x) {
        return (r, q, c) -> x == null ? null : c.lessThanOrEqualTo(r.get("price"), x);
    }
}
