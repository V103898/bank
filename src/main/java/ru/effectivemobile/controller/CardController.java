package ru.effectivemobile.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.effectivemobile.dto.CardDTO;
import ru.effectivemobile.dto.CreateCardRequest;
import ru.effectivemobile.service.CardService;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardDTO> createCard(
            @Valid @RequestBody CreateCardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        CardDTO card = cardService.createCard(request, userDetails.getUsername());
        return ResponseEntity.ok(card);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<CardDTO>> getMyCards(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<CardDTO> cards = cardService.getUserCards(userDetails.getUsername(), pageable);
        return ResponseEntity.ok(cards);
    }

    @PutMapping("/{cardId}/block")
    public ResponseEntity<CardDTO> blockCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CardDTO card = cardService.blockCard(cardId, userDetails.getUsername());
        return ResponseEntity.ok(card);
    }

    @PutMapping("/{cardId}/activate")
    public ResponseEntity<CardDTO> activateCard(
            @PathVariable Long cardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        CardDTO card = cardService.activateCard(cardId, userDetails.getUsername());
        return ResponseEntity.ok(card);
    }
}