package ru.effectivemobile.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.effectivemobile.entity.BankCard;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("ALL")
public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    Page<BankCard> findByOwnerId(Long ownerId, Pageable pageable);

    List<BankCard> findByOwnerId(Long ownerId);

    Optional<BankCard> findByCardNumber(String cardNumber);

    @Query("SELECT c FROM BankCard c WHERE c.owner.id = :userId AND c.status = 'ACTIVE'")
    List<BankCard> findActiveCardsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) > 0 FROM BankCard c WHERE c.owner.id = :userId AND c.id = :cardId")
    boolean existsByUserIdAndCardId(@Param("userId") Long userId, @Param("cardId") Long cardId);

    @Query("SELECT c FROM BankCard c WHERE c.expiryDate < CURRENT_DATE AND c.status = 'ACTIVE'")
    List<BankCard> findExpiredActiveCards();
}