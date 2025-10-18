package ru.effectivemobile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.effectivemobile.dto.CardDTO;
import ru.effectivemobile.dto.CreateCardRequest;
import ru.effectivemobile.entity.BankCard;
import ru.effectivemobile.service.CardService;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private CardService cardService;

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void createCard_ShouldReturnCreatedCard() throws Exception {
        // Given
        CreateCardRequest request = new CreateCardRequest();
        request.setCardHolderName("Test User");
        request.setExpiryDate(YearMonth.now().plusYears(3));

        CardDTO responseDTO = new CardDTO();
        responseDTO.setId(1L);
        responseDTO.setMaskedCardNumber("**** **** **** 1234");
        responseDTO.setCardHolderName("Test User");
        responseDTO.setExpiryDate(YearMonth.now().plusYears(3));
        responseDTO.setStatus(BankCard.CardStatus.ACTIVE);
        responseDTO.setBalance(BigDecimal.ZERO);

        when(cardService.createCard(any(CreateCardRequest.class), anyString()))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.cardHolderName").value("Test User"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void getMyCards_ShouldReturnUserCards() throws Exception {
        // Given
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);
        cardDTO.setMaskedCardNumber("**** **** **** 1234");
        cardDTO.setCardHolderName("Test User");

        Page<CardDTO> cardPage = new PageImpl<>(List.of(cardDTO), PageRequest.of(0, 10), 1);

        when(cardService.getUserCards(anyString(), any()))
                .thenReturn(cardPage);

        // When & Then
        mockMvc.perform(get("/api/cards/my")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 1234"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void blockCard_ShouldBlockCardSuccessfully() throws Exception {
        // Given
        CardDTO responseDTO = new CardDTO();
        responseDTO.setId(1L);
        responseDTO.setStatus(BankCard.CardStatus.BLOCKED);

        when(cardService.blockCard(any(Long.class), anyString()))
                .thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(put("/api/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void createCard_Unauthenticated_ShouldReturnUnauthorized() throws Exception {
        // Given
        CreateCardRequest request = new CreateCardRequest();
        request.setCardHolderName("Test User");
        request.setExpiryDate(YearMonth.now().plusYears(3));

        // When & Then
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}