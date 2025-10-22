package ru.effectivemobile.service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.effectivemobile.dto.TransferRequest;
import ru.effectivemobile.entity.BankCard;
import ru.effectivemobile.entity.Transaction;
import ru.effectivemobile.exception.InsufficientFundsException;
import ru.effectivemobile.exception.ResourceNotFoundException;
import ru.effectivemobile.repository.BankCardRepository;
import ru.effectivemobile.repository.TransactionRepository;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final BankCardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Transactional
    public Transaction transferBetweenOwnCards(TransferRequest request, String username) {
        BankCard fromCard = cardRepository.findById(request.getFromCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Source card not found"));
        BankCard toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination card not found"));

        // Validate ownership
        if (!fromCard.getOwner().getUsername().equals(username) ||
                !toCard.getOwner().getUsername().equals(username)) {
            throw new SecurityException("You can only transfer between your own cards");
        }

        // Validate card status
        validateCardForTransfer(fromCard);
        validateCardForTransfer(toCard);

        // Check sufficient funds
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // Perform transfer
        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        // Record transaction
        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setDescription(request.getDescription());

        return transactionRepository.save(transaction);
    }

    private void validateCardForTransfer(BankCard card) {
        if (card.getStatus() != BankCard.CardStatus.ACTIVE) {
            throw new IllegalStateException("Card is not active: " + card.getStatus());
        }

        if (card.getExpiryDate().isBefore(java.time.YearMonth.now())) {
            throw new IllegalStateException("Card has expired");
        }
    }
}