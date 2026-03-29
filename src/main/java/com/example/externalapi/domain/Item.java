package com.example.externalapi.domain;

public record Item(
        String itemId,
        String itemName,
        int unitPrice,
        String unit,
        int stock
) {
}
