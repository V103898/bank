package ru.effectivemobile.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.effectivemobile.dto.CardDTO;
import ru.effectivemobile.dto.CreateCardRequest;
import ru.effectivemobile.entity.BankCard;
import ru.effectivemobile.entity.User;
import ru.effectivemobile.exception.ResourceNotFoundException;
import ru.effectivemobile.repository.BankCardRepository;
import ru.effectivemobile.util.CardNumberEncryptor;
import ru.effectivemobile.util.CardNumberGenerator;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class CardService {

    private final BankCardRepository cardRepository;
    private final UserService userService;
    private final CardNumberGenerator cardNumberGenerator;
    private final CardNumberEncryptor cardNumberEncryptor;

    @Transactional
    public CardDTO createCard(CreateCardRequest request, String username) {
        User user = request.getUserId() != null && isAdmin(username)
                ? userService.findById(request.getUserId())
                : userService.findByUsername(username);

        String cardNumber = cardNumberGenerator.generateCardNumber();
        String encryptedCardNumber = cardNumberEncryptor.encrypt(cardNumber);
        String maskedCardNumber = maskCardNumber(cardNumber);

        BankCard card = new BankCard();
        card.setCardNumber(encryptedCardNumber);
        card.setMaskedCardNumber(maskedCardNumber);
        card.setCardHolderName(request.getCardHolderName());
        card.setExpiryDate(request.getExpiryDate());
        card.setStatus(BankCard.CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);
        card.setOwner(user);

        card = cardRepository.save(card);
        return CardDTO.fromEntity(card);
    }

    public Page<CardDTO> getUserCards(String username, Pageable pageable) {
        User user = userService.findByUsername(username);
        return cardRepository.findByOwnerId(user.getId(), pageable)
                .map(CardDTO::fromEntity);
    }

    public Page<CardDTO> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable).map(CardDTO::fromEntity);
    }

    @Transactional
    public CardDTO blockCard(Long cardId, String username) {
        BankCard card = findCardById(cardId);
        validateCardOwnershipOrAdmin(card, username);

        card.setStatus(BankCard.CardStatus.BLOCKED);
        card = cardRepository.save(card);
        return CardDTO.fromEntity(card);
    }

    @Transactional
    public CardDTO activateCard(Long cardId, String username) {
        BankCard card = findCardById(cardId);
        if (!isAdmin(username)) {
            validateCardOwnership(card, username);
        }

        if (card.getExpiryDate().isBefore(YearMonth.now())) {
            throw new IllegalStateException("Cannot activate expired card");
        }

        card.setStatus(BankCard.CardStatus.ACTIVE);
        card = cardRepository.save(card);
        return CardDTO.fromEntity(card);
    }

    BankCard findCardById(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    private void validateCardOwnership(BankCard card, String username) {
        if (!card.getOwner().getUsername().equals(username)) {
            throw new SecurityException("Access denied");
        }
    }

    private void validateCardOwnershipOrAdmin(BankCard card, String username) {
        if (!card.getOwner().getUsername().equals(username) && !isAdmin(username)) {
            throw new SecurityException("Access denied");
        }
    }

    private boolean isAdmin(String username) {
        return userService.findByUsername(username).getRole() == User.Role.ROLE_ADMIN;
    }

    private String maskCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}