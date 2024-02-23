package com.example.demo.dao;

import com.example.demo.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.locator.ClasspathSqlLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class EmployeeDaoInMemoryH2Test {

    private static final Jdbi DB_CONNECTION = Jdbi
            .create("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE");

    private EmployeeDao employeeDao;

    @BeforeAll
    static void setupAll() {
        DB_CONNECTION.withHandle(handle ->
                handle.createScript(ClasspathSqlLocator.removingComments().getResource("schema.sql"))
                        .execute()
        );
    }

    @BeforeEach
    void setup() {
        employeeDao = new EmployeeDao(DB_CONNECTION);
        resetData();
    }

    private void resetData() {
        DB_CONNECTION.withHandle(handle ->
                handle.createScript(ClasspathSqlLocator.removingComments().getResource("employee.sql"))
                        .execute()
        );
    }

    @Test
    void testGetEmployeeById() {
        Optional<Employee> result = employeeDao.getEmployeesById(1);
        assertThat(result).isNotEmpty()
                .contains(Employee.builder()
                        .id(1)
                        .name("Rueben Hardy")
                        .department("FINANCE")
                        .salary(new BigDecimal("1291.8"))
                        .build());
    }

    @Test
    void testGetEmployeeByDepartment() {
        List<Employee> result = employeeDao.getEmployeesByDepartment("FINANCE");
        assertThat(result).hasSize(3)
                .containsOnly(
                        new Employee(1, "Rueben Hardy", "FINANCE", new BigDecimal("1291.8")),
                        new Employee(2, "Frank Dunlap", "FINANCE", new BigDecimal("10025.3")),
                        new Employee(3, "Anna Melendez", "FINANCE", new BigDecimal("8773.13"))
                );
    }

    @Test
    void testGetAverageSalaryByDepartment() {
        Map<String, BigDecimal> result = employeeDao.getAverageSalaryByDepartment();
        assertThat(result).hasSize(4)
                .containsOnly(
                        entry("FINANCE", new BigDecimal("6696.74")),
                        entry("OPERATION", new BigDecimal("4080.54")),
                        entry("MARKETING", new BigDecimal("6455.57")),
                        entry("SALES", new BigDecimal("5562.84"))
                );
    }

    @Test
    void testInsertEmployee() {
        Employee newEmployee = Employee.builder()
                .id(99)
                .name("Gina Bond")
                .department("MARKETING")
                .salary(new BigDecimal("3425.5"))
                .build();

        assertThat(employeeDao.insertEmployee(newEmployee)).isEqualTo(1);

        Optional<Employee> insertedRecord = DB_CONNECTION.withHandle(handle ->
                handle.createQuery("SELECT id, name, department, salary " +
                                "FROM EMPLOYEE " +
                                "WHERE id = :id")
                        .bind("id", 99)
                        .mapToBean(Employee.class)
                        .findOne());

        assertThat(insertedRecord).isNotEmpty()
                .contains(newEmployee);
    }
}
