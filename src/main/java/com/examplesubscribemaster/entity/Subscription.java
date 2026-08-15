package com.examplesubscribemaster.entity;


import com.examplesubscribemaster.enums.Category;
import com.examplesubscribemaster.enums.Currency;
import com.examplesubscribemaster.enums.Periodicity;
import com.examplesubscribemaster.enums.SubscriptionStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions", indexes = {@Index(name = "idx_sub_user_deleted", columnList = "user_id,is_deleted"), @Index(name = "idx_sub_next_payment", columnList = "next_payment_date")})
public class Subscription extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate nextPaymentDate;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Periodicity periodicity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SubscriptionStatus status;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;
    @Column(nullable = false)
    private boolean isDeleted;
    @Version
    @Column(nullable = false)
    private Long version;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_subscription_user"))
    private User user;

    protected Subscription() {
    }

    public Subscription(String name, BigDecimal price, Currency currency, LocalDate startDate, Periodicity periodicity, SubscriptionStatus status, Category category, User user) {
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.startDate = startDate;
        this.nextPaymentDate = periodicity.next(startDate);
        this.periodicity = periodicity;
        this.status = status;
        this.category = category;
        this.user = user;
        this.isDeleted = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Currency getCurrency() {
        return currency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getNextPaymentDate() {
        return nextPaymentDate;
    }

    public Periodicity getPeriodicity() {
        return periodicity;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public Long getVersion() {
        return version;
    }

    public User getUser() {
        return user;
    }

    public void update(String name, BigDecimal price, Currency currency, LocalDate startDate, Periodicity periodicity, SubscriptionStatus status, Category category) {
        this.name = name;
        this.price = price;
        this.currency = currency;
        this.startDate = startDate;
        this.periodicity = periodicity;
        this.status = status;
        this.category = category;
        this.nextPaymentDate = periodicity.next(startDate);
    }

    public void softDelete() {
        this.isDeleted = true;
    }

    public void markPaid() {
        this.nextPaymentDate = periodicity.next(nextPaymentDate);
    }
}
