package com.example.demo.dao;

import com.example.demo.model.Employee;
import lombok.RequiredArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class EmployeeDao {

    private static final String SELECT_EMPLOYEE_BY_ID =
                    "SELECT id, name, department, salary " +
                    "FROM EMPLOYEE " +
                    "WHERE id = :id";

    private static final String SELECT_EMPLOYEE_BY_DEPARTMENT =
            "SELECT id, name, department, salary " +
            "FROM EMPLOYEE " +
            "WHERE department = :department";

    private static final String SELECT_AVERAGE_SALARY_BY_DEPARTMENT =
                    "SELECT department, round(avg(salary), 2) as average_salary " +
                    "FROM EMPLOYEE " +
                    "GROUP BY department";

    private static final String INSERT_EMPLOYEE =
            "INSERT INTO EMPLOYEE(id, name, department, salary) VALUES (:id, :name, :department, :salary)";

    private final Jdbi jdbi;

    public int insertEmployee(Employee employee) {
        return jdbi.withHandle(handle ->
                handle.createUpdate(INSERT_EMPLOYEE)
                        .bind("id", employee.getId())
                        .bind("name", employee.getName())
                        .bind("department", employee.getDepartment())
                        .bind("salary", employee.getSalary())
                        .execute()
        );
    }

    public Optional<Employee> getEmployeesById(int id) {
        return jdbi.withHandle(handle ->
                handle.createQuery(SELECT_EMPLOYEE_BY_ID)
                        .bind("id", id)
                        .mapToBean(Employee.class)
                        .findOne());
    }

    public List<Employee> getEmployeesByDepartment(String department) {
        return jdbi.withHandle(handle ->
                handle.createQuery(SELECT_EMPLOYEE_BY_DEPARTMENT)
                        .bind("department", department)
                        .mapToBean(Employee.class)
                        .list());
    }

    public Map<String, BigDecimal> getAverageSalaryByDepartment() {
        return jdbi.withHandle(handle ->
                handle.createQuery(SELECT_AVERAGE_SALARY_BY_DEPARTMENT)
                        .setMapKeyColumn("department")
                        .setMapValueColumn("average_salary")
                        .collectInto(new GenericType<>() {}));
    }

}
