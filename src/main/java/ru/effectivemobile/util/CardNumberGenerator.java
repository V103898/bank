package ru.effectivemobile.util;

import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class CardNumberGenerator {

    private static final String BIN = "4276"; // Пример BIN кода
    private static final int CARD_NUMBER_LENGTH = 16;
    private final Random random = new Random();

    public String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(BIN);

        // Генерируем оставшиеся цифры (кроме последней контрольной)
        while (cardNumber.length() < CARD_NUMBER_LENGTH - 1) {
            cardNumber.append(random.nextInt(10));
        }

        // Добавляем контрольную цифру по алгоритму Луна
        String numberWithoutCheckDigit = cardNumber.toString();
        int checkDigit = calculateLuhnCheckDigit(numberWithoutCheckDigit);
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = false;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    public boolean validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != CARD_NUMBER_LENGTH) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10) == 0;
    }
}