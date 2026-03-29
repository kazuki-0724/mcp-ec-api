package com.example.externalapi.usecase;

import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.example.externalapi.app.AppException;

@Component
public class InputValidator {

    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile("^E[0-9]{3}$");
    private static final Pattern ITEM_ID_PATTERN = Pattern.compile("^G[0-9]{3}$");

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
}
