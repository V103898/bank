package ru.effectivemobile.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Сущность банковской карты
 * @author EffectiveMobile Team
 */
@Entity
@Table(name = "bank_cards")
public class BankCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(name = "masked_card_number", nullable = false)
    private String maskedCardNumber;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(name = "expiry_date", nullable = false)
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

    /**
     * Статусы банковской карты
     */
    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED
    }

    // Конструкторы
    public BankCard() {}

    public BankCard(String cardNumber, String maskedCardNumber, String cardHolderName,
                    YearMonth expiryDate, CardStatus status, BigDecimal balance, User owner) {
        this.cardNumber = cardNumber;
        this.maskedCardNumber = maskedCardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.status = status;
        this.balance = balance;
        this.owner = owner;
    }

    @PreUpdate
    @PrePersist
    public void updateStatus() {
        if (expiryDate.isBefore(YearMonth.now())) {
            this.status = CardStatus.EXPIRED;
        }
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public YearMonth getExpiryDate() { return expiryDate; }
    public void setExpiryDate(YearMonth expiryDate) { this.expiryDate = expiryDate; }

    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankCard bankCard)) return false;
        return Objects.equals(id, bankCard.id) &&
                Objects.equals(cardNumber, bankCard.cardNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, cardNumber);
    }

    @Override
    public String toString() {
        return "BankCard{" +
                "id=" + id +
                ", maskedCardNumber='" + maskedCardNumber + '\'' +
                ", cardHolderName='" + cardHolderName + '\'' +
                ", expiryDate=" + expiryDate +
                ", status=" + status +
                ", balance=" + balance +
                '}';
    }
}