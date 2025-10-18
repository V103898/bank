package ru.effectivemobile.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.effectivemobile.dto.TransferRequest;
import ru.effectivemobile.entity.BankCard;
import ru.effectivemobile.entity.Transaction;
import ru.effectivemobile.entity.User;
import ru.effectivemobile.exception.InsufficientFundsException;
import ru.effectivemobile.exception.ResourceNotFoundException;
import ru.effectivemobile.repository.BankCardRepository;
import ru.effectivemobile.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private BankCardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private BankCard fromCard;
    private BankCard toCard;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        fromCard = new BankCard();
        fromCard.setId(1L);
        fromCard.setCardHolderName("Test User");
        fromCard.setExpiryDate(YearMonth.now().plusYears(2));
        fromCard.setStatus(BankCard.CardStatus.ACTIVE);
        fromCard.setBalance(new BigDecimal("1000.00"));
        fromCard.setOwner(testUser);

        toCard = new BankCard();
        toCard.setId(2L);
        toCard.setCardHolderName("Test User");
        toCard.setExpiryDate(YearMonth.now().plusYears(3));
        toCard.setStatus(BankCard.CardStatus.ACTIVE);
        toCard.setBalance(new BigDecimal("500.00"));
        toCard.setOwner(testUser);

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(new BigDecimal("200.00"));
        transferRequest.setDescription("Test transfer");
    }

    @Test
    void transferBetweenOwnCards_ShouldTransferSuccessfully() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(BankCard.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });

        // When
        Transaction result = transferService.transferBetweenOwnCards(transferRequest, "testuser");

        // Then
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.SUCCESS, result.getStatus());
        assertEquals(new BigDecimal("800.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("700.00"), toCard.getBalance());
        assertEquals("Test transfer", result.getDescription());

        verify(cardRepository, times(2)).save(any(BankCard.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferBetweenOwnCards_InsufficientFunds_ShouldThrowException() {
        // Given
        transferRequest.setAmount(new BigDecimal("1500.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(InsufficientFundsException.class, () ->
                transferService.transferBetweenOwnCards(transferRequest, "testuser"));
    }

    @Test
    void transferBetweenOwnCards_FromCardNotFound_ShouldThrowException() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                transferService.transferBetweenOwnCards(transferRequest, "testuser"));
    }

    @Test
    void transferBetweenOwnCards_ToCardNotFound_ShouldThrowException() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                transferService.transferBetweenOwnCards(transferRequest, "testuser"));
    }

    @Test
    void transferBetweenOwnCards_NotOwnCards_ShouldThrowSecurityException() {
        // Given
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");
        toCard.setOwner(otherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(SecurityException.class, () ->
                transferService.transferBetweenOwnCards(transferRequest, "testuser"));
    }

    @Test
    void transferBetweenOwnCards_BlockedCard_ShouldThrowException() {
        // Given
        fromCard.setStatus(BankCard.CardStatus.BLOCKED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenOwnCards(transferRequest, "testuser"));
    }

    @Test
    void transferBetweenOwnCards_ExpiredCard_ShouldThrowException() {
        // Given
        fromCard.setExpiryDate(YearMonth.now().minusMonths(1));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));

        // When & Then
        assertThrows(IllegalStateException.class, () ->
                transferService.transferBetweenOwnCards(transferRequest, "testuser"));
    }
}
