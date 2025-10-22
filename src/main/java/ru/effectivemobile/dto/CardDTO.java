package ru.effectivemobile.dto;
import lombok.Data;
import ru.effectivemobile.entity.BankCard;
import java.math.BigDecimal;
import java.time.YearMonth;

@Data
public class CardDTO {
    private Long id;
    private String maskedCardNumber;
    private String cardHolderName;
    private YearMonth expiryDate;
    private BankCard.CardStatus status;
    private BigDecimal balance;

    public static CardDTO fromEntity(BankCard card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setMaskedCardNumber(card.getMaskedCardNumber());
        dto.setCardHolderName(card.getCardHolderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setStatus(card.getStatus());
        dto.setBalance(card.getBalance());
        return dto;
    }
}