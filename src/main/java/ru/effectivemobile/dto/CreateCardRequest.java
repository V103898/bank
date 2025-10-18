package ru.effectivemobile.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.YearMonth;

@Data
public class CreateCardRequest {
    @NotBlank
    private String cardHolderName;

    @NotNull
    private YearMonth expiryDate;

    private Long userId; // For admin only
}