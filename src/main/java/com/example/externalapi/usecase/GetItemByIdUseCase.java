package com.example.externalapi.usecase;

import org.springframework.stereotype.Service;

import com.example.externalapi.app.AppException;
import com.example.externalapi.domain.Item;
import com.example.externalapi.domain.ItemRepository;

@Service
public class GetItemByIdUseCase {

    private final ItemRepository itemRepository;
    private final InputValidator inputValidator;

    public GetItemByIdUseCase(ItemRepository itemRepository, InputValidator inputValidator) {
        this.itemRepository = itemRepository;
        this.inputValidator = inputValidator;
    }

    public Item execute(String itemId) {
        String validatedItemId = inputValidator.validateItemId(itemId);
        return itemRepository.findById(validatedItemId)
                .orElseThrow(() -> AppException.notFound("item not found: " + validatedItemId));
    }
}
