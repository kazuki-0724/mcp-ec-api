package com.example.externalapi.infrastructure;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.externalapi.domain.Employee;
import com.example.externalapi.domain.EmployeeRepository;

@Repository
public class MockEmployeeRepository implements EmployeeRepository {

    private final MockExternalApiDataSource dataSource;

    public MockEmployeeRepository(MockExternalApiDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Employee> findById(String employeeId) {
        return dataSource.findEmployeeById(employeeId);
    }
}
