package com.example.externalapi.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.externalapi.app.AppException;
import com.example.externalapi.domain.Item;
import com.example.externalapi.domain.ItemRepository;

class GetItemByIdUseCaseTest {

    private final InputValidator inputValidator = new InputValidator();

    @Test
    void returnsItemForValidId() {
        ItemRepository repository = itemId -> Optional.of(new Item(itemId, "たまねぎ", 120, "個", 58));
        GetItemByIdUseCase useCase = new GetItemByIdUseCase(repository, inputValidator);

        Item result = useCase.execute("G001");

        assertThat(result.itemName()).isEqualTo("たまねぎ");
        assertThat(result.unitPrice()).isEqualTo(120);
    }

    @Test
    void rejectsInvalidItemId() {
        ItemRepository repository = itemId -> Optional.empty();
        GetItemByIdUseCase useCase = new GetItemByIdUseCase(repository, inputValidator);

        assertThatThrownBy(() -> useCase.execute("I001"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("itemId must match");
    }
}
