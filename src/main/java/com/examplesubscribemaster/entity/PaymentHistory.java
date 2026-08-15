package com.examplesubscribemaster.entity;

import com.examplesubscribemaster.enums.Currency;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_history", indexes = @Index(name = "idx_payment_subscription_date", columnList = "subscription_id,payment_date"))
public class PaymentHistory extends BaseAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_subscription"))
    private Subscription subscription;
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;
    @Column(nullable = false)
    private LocalDate paymentDate;

    protected PaymentHistory() {
    }

    public PaymentHistory(Subscription subscription, BigDecimal amount, Currency currency, LocalDate paymentDate) {
        this.subscription = subscription;
        this.amount = amount;
        this.currency = currency;
        this.paymentDate = paymentDate;
    }

    public Long getId() {
        return id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }
}
