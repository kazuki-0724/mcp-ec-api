package com.example.externalapi.usecase;

import org.springframework.stereotype.Service;

import com.example.externalapi.app.AppException;
import com.example.externalapi.domain.Employee;
import com.example.externalapi.domain.EmployeeRepository;

@Service
public class GetEmployeeByIdUseCase {

    private final EmployeeRepository employeeRepository;
    private final InputValidator inputValidator;

    public GetEmployeeByIdUseCase(EmployeeRepository employeeRepository, InputValidator inputValidator) {
        this.employeeRepository = employeeRepository;
        this.inputValidator = inputValidator;
    }

    public Employee execute(String employeeId) {
        String validatedEmployeeId = inputValidator.validateEmployeeId(employeeId);
        return employeeRepository.findById(validatedEmployeeId)
                .orElseThrow(() -> AppException.notFound("employee not found: " + validatedEmployeeId));
    }
}
