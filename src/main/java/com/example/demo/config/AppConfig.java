package com.example.demo.config;

import org.jdbi.v3.core.ConnectionFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.spring5.SpringConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AppConfig {

    @Bean
    public Jdbi jdbi(DataSource dataSource) {
        ConnectionFactory cf = new SpringConnectionFactory(dataSource);
        return Jdbi.create(cf);
    }
}
