package com.example.externalapi.domain;

public final class Employee {

        private final String employeeId;
        private final String name;
        private final String department;
        private final String role;
        private final String email;

        public Employee(String employeeId, String name, String department) {
                this(employeeId, name, department, null, null);
        }

        public Employee(String employeeId, String name, String department, String role, String email) {
                this.employeeId = employeeId;
                this.name = name;
                this.department = department;
                this.role = role;
                this.email = email;
        }

        public String employeeId() {
                return employeeId;
        }

        public String name() {
                return name;
        }

        public String department() {
                return department;
        }

        public String role() {
                return role;
        }

        public String email() {
                return email;
        }
}
