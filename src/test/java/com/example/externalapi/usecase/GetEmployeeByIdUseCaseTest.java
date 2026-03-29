package com.example.externalapi.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.externalapi.app.AppException;
import com.example.externalapi.domain.Employee;
import com.example.externalapi.domain.EmployeeRepository;

class GetEmployeeByIdUseCaseTest {

    private final InputValidator inputValidator = new InputValidator();

    @Test
    void returnsEmployeeForValidId() {
        EmployeeRepository repository = employeeId -> Optional.of(new Employee(employeeId, "山田 太郎", "営業部"));
        GetEmployeeByIdUseCase useCase = new GetEmployeeByIdUseCase(repository, inputValidator);

        Employee result = useCase.execute("E001");

        assertThat(result.name()).isEqualTo("山田 太郎");
        assertThat(result.department()).isEqualTo("営業部");
    }

    @Test
    void rejectsInvalidEmployeeId() {
        EmployeeRepository repository = employeeId -> Optional.empty();
        GetEmployeeByIdUseCase useCase = new GetEmployeeByIdUseCase(repository, inputValidator);

        assertThatThrownBy(() -> useCase.execute("X001"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("employeeId must match");
    }
}
