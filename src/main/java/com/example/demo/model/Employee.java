package com.example.demo.model;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Employee {
    private Integer id;
    private String name;
    private String department;

    @EqualsAndHashCode.Exclude
    private BigDecimal salary;

    @EqualsAndHashCode.Include
    BigDecimal salary() {
        return salary.stripTrailingZeros();
    }



}
