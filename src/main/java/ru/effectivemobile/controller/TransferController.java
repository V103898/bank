package ru.effectivemobile.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.effectivemobile.dto.TransferRequest;
import ru.effectivemobile.entity.Transaction;
import ru.effectivemobile.service.TransferService;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<Transaction> transferBetweenCards(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Transaction transaction = transferService.transferBetweenOwnCards(request, userDetails.getUsername());
        return ResponseEntity.ok(transaction);
    }
}