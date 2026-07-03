package com.chhavi.votingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
@ComponentScan(basePackages = "com.chhavi")
@EnableJpaRepositories(basePackages = "com.chhavi.repository")
@EntityScan(basePackages = "com.chhavi.pojo")
public class VotingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(VotingSystemApplication.class, args);
    }

    @Bean
    public CommandLineRunner runSchemaModifications(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE candidates MODIFY symbol LONGTEXT");
                jdbcTemplate.execute("ALTER TABLE candidates MODIFY profile_image LONGTEXT");
                jdbcTemplate.execute("ALTER TABLE users MODIFY profile_image LONGTEXT");
                System.out.println("====== DATABASE SCHEMA MODIFIED SUCCESSFULLY TO LONGTEXT ======");
            } catch (Exception e) {
                System.err.println("Failed to alter database schema: " + e.getMessage());
            }
        };
    }
}