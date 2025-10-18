package ru.effectivemobile.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.effectivemobile.dto.CardDTO;
import ru.effectivemobile.dto.CreateCardRequest;
import ru.effectivemobile.service.CardService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final CardService cardService;

    @GetMapping("/cards")
    public ResponseEntity<Page<CardDTO>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CardDTO> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/cards")
    public ResponseEntity<CardDTO> createCardForUser(
            @Valid @RequestBody CreateCardRequest request) {
        CardDTO card = cardService.createCard(request, "admin");
        return ResponseEntity.ok(card);
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        // Implementation for card deletion
        return ResponseEntity.noContent().build();
    }
}