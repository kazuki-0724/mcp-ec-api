package com.example.externalapi.usecase;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.example.externalapi.app.AppException;

@Component
public class InputValidator {

    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("^E[0-9]{3}$");
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("^G[0-9]{3}$");
    private static final Pattern CUSTOMER_ID_PATTERN = Pattern.compile("^C[0-9]{3}$");
    private static final Pattern ORDER_ID_PATTERN = Pattern.compile("^O[0-9]{4}$");
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("^[0-9]{7}$");

    public String validateEmployeeId(String employeeId) {
        if (employeeId == null || !EMPLOYEE_ID_PATTERN.matcher(employeeId).matches()) {
            throw AppException.badUserInput("employeeId must match ^E[0-9]{3}$");
        }
        return employeeId;
    }

    public String validateItemId(String itemId) {
        if (itemId == null || !ITEM_ID_PATTERN.matcher(itemId).matches()) {
            throw AppException.badUserInput("itemId must match ^G[0-9]{3}$");
        }
        return itemId;
    }

    public String validateKeyword(String keyword) {
        if (keyword == null) {
            throw AppException.badUserInput("keyword must not be null");
        }
        String normalized = keyword.trim();
        if (normalized.isEmpty()) {
            throw AppException.badUserInput("keyword must not be blank after trim");
        }
        return normalized;
    }

    public String validateCustomerId(String customerId) {
        if (customerId == null || !CUSTOMER_ID_PATTERN.matcher(customerId).matches()) {
            throw AppException.badUserInput("customerId must match ^C[0-9]{3}$");
        }
        return customerId;
    }

    public String validateOrderId(String orderId) {
        if (orderId == null || !ORDER_ID_PATTERN.matcher(orderId).matches()) {
            throw AppException.badUserInput("orderId must match ^O[0-9]{4}$");
        }
        return orderId;
    }

    public String validateRequiredText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw AppException.badUserInput(fieldName + " must not be blank");
        }
        return value.trim();
    }

    public int validateNonNegativeQuantity(int quantity, String fieldName) {
        if (quantity < 0) {
            throw AppException.badUserInput(fieldName + " must be greater than or equal to 0");
        }
        return quantity;
    }

    public int validatePositiveQuantity(int quantity, String fieldName) {
        if (quantity <= 0) {
            throw AppException.badUserInput(fieldName + " must be greater than 0");
        }
        return quantity;
    }

    public int validatePositiveLimit(Integer limit, int defaultValue) {
        if (limit == null) {
            return defaultValue;
        }
        if (limit <= 0) {
            throw AppException.badUserInput("limit must be greater than 0");
        }
        return limit;
    }

    public String validatePostalCode(String postalCode) {
        if (postalCode == null || !POSTAL_CODE_PATTERN.matcher(postalCode).matches()) {
            throw AppException.badUserInput("postalCode must be 7 digits without hyphen");
        }
        return postalCode;
    }

    public String normalizeTier(String customerTier) {
        if (customerTier == null || customerTier.trim().isEmpty()) {
            return "bronze";
        }
        return customerTier.trim().toLowerCase();
    }
}
