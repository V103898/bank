package ru.effectivemobile.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "bank_cards")
@Data
public class BankCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber; // encrypted

    @Column(nullable = false)
    private String maskedCardNumber;

    @Column(nullable = false)
    private String cardHolderName;

    @Column(nullable = false)
    private YearMonth expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    @Column(nullable = false)
    private BigDecimal balance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Version
    private Long version;

    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED
    }

    @PreUpdate
    @PrePersist
    public void updateStatus() {
        if (expiryDate.isBefore(YearMonth.now())) {
            this.status = CardStatus.EXPIRED;
        }
    }
}