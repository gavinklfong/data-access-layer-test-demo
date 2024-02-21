package com.example.demo.dao;

import com.example.demo.model.Employee;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.locator.ClasspathSqlLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.jdbi.v3.core.Jdbi;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@Testcontainers
class EmployeeDaoMySQLTestContainersTest {

    @Container
    private static final MySQLContainer<?> MYSQL_CONTAINER =
            new MySQLContainer<>(DockerImageName.parse("mysql:latest"))
                    .withInitScript("schema.sql");

    private Jdbi jdbi;

    private EmployeeDao employeeDao;

    @BeforeEach
    void setup() {
        jdbi = Jdbi.create(MYSQL_CONTAINER.getJdbcUrl(),
                MYSQL_CONTAINER.getUsername(), MYSQL_CONTAINER.getPassword());

        employeeDao = new EmployeeDao(jdbi);

        resetData();
    }

    private void resetData() {
        jdbi.withHandle(handle ->
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

        Optional<Employee> insertedRecord = jdbi.withHandle(handle ->
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
