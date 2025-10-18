package ru.effectivemobile.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private BankCardRepository cardRepository;

    @Mock
    private UserService userService;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private CardNumberEncryptor cardNumberEncryptor;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private User adminUser;
    private BankCard testCard;
    private CreateCardRequest createCardRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(User.Role.ROLE_USER);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(User.Role.ROLE_ADMIN);

        testCard = new BankCard();
        testCard.setId(1L);
        testCard.setCardNumber("encrypted1234");
        testCard.setMaskedCardNumber("**** **** **** 1234");
        testCard.setCardHolderName("Test User");
        testCard.setExpiryDate(YearMonth.now().plusYears(3));
        testCard.setStatus(BankCard.CardStatus.ACTIVE);
        testCard.setBalance(BigDecimal.ZERO);
        testCard.setOwner(testUser);

        createCardRequest = new CreateCardRequest();
        createCardRequest.setCardHolderName("Test User");
        createCardRequest.setExpiryDate(YearMonth.now().plusYears(3));
    }

    @Test
    void createCard_ShouldCreateCardSuccessfully() {
        // Given
        String generatedCardNumber = "4276123456789012";
        String encryptedCardNumber = "encrypted1234";

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(cardNumberGenerator.generateCardNumber()).thenReturn(generatedCardNumber);
        when(cardNumberEncryptor.encrypt(generatedCardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        CardDTO result = cardService.createCard(createCardRequest, "testuser");

        // Then
        assertNotNull(result);
        assertEquals(testCard.getId(), result.getId());
        assertEquals(testCard.getMaskedCardNumber(), result.getMaskedCardNumber());
        assertEquals(testCard.getCardHolderName(), result.getCardHolderName());

        verify(cardRepository).save(any(BankCard.class));
        verify(cardNumberGenerator).generateCardNumber();
        verify(cardNumberEncryptor).encrypt(generatedCardNumber);
    }

    @Test
    void createCard_AsAdminForOtherUser_ShouldCreateCardSuccessfully() {
        // Given
        createCardRequest.setUserId(3L);
        User targetUser = new User();
        targetUser.setId(3L);
        targetUser.setUsername("targetuser");

        String generatedCardNumber = "4276123456789012";
        String encryptedCardNumber = "encrypted1234";

        when(userService.findByUsername("admin")).thenReturn(adminUser);
        when(userService.findById(3L)).thenReturn(targetUser);
        when(cardNumberGenerator.generateCardNumber()).thenReturn(generatedCardNumber);
        when(cardNumberEncryptor.encrypt(generatedCardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.save(any(BankCard.class))).thenAnswer(invocation -> {
            BankCard card = invocation.getArgument(0);
            card.setId(2L);
            return card;
        });

        // When
        CardDTO result = cardService.createCard(createCardRequest, "admin");

        // Then
        assertNotNull(result);
        verify(userService).findById(3L);
    }

    @Test
    void getUserCards_ShouldReturnUserCards() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<BankCard> cards = List.of(testCard);
        Page<BankCard> cardPage = new PageImpl<>(cards, pageable, cards.size());

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(cardRepository.findByOwnerId(1L, pageable)).thenReturn(cardPage);

        // When
        Page<CardDTO> result = cardService.getUserCards("testuser", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCard.getId(), result.getContent().get(0).getId());
    }

    @Test
    void blockCard_ByOwner_ShouldBlockCard() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        CardDTO result = cardService.blockCard(1L, "testuser");

        // Then
        assertNotNull(result);
        assertEquals(BankCard.CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository).save(testCard);
    }

    @Test
    void blockCard_ByAdminForOtherUser_ShouldBlockCard() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(BankCard.class))).thenReturn(testCard);

        // When
        CardDTO result = cardService.blockCard(1L, "admin");

        // Then
        assertNotNull(result);
        assertEquals(BankCard.CardStatus.BLOCKED, testCard.getStatus());
    }

    @Test
    void blockCard_ByOtherUser_ShouldThrowSecurityException() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(SecurityException.class, () ->
                cardService.blockCard(1L, "otheruser"));
    }

    @Test
    void activateCard_ExpiredCard_ShouldThrowException() {
        // Given
        testCard.setExpiryDate(YearMonth.now().minusMonths(1));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                cardService.activateCard(1L, "testuser"));
    }

    @Test
    void findCardById_NonExistentCard_ShouldThrowException() {
        // Given
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                cardService.findCardById(999L));
    }
}