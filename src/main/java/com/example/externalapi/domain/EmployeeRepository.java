package com.example.externalapi.domain;

import java.util.Optional;

public interface EmployeeRepository {
    Optional<Employee> findById(String employeeId);
}
