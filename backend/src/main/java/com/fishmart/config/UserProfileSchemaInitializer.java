package com.fishmart.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(-100)
public class UserProfileSchemaInitializer implements CommandLineRunner {
  private final JdbcTemplate jdbcTemplate;

  public UserProfileSchemaInitializer(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public void run(String... args) {
    addColumnIfMissing("email", "varchar(128) null");
    addColumnIfMissing("phone", "varchar(32) null");
    addColumnIfMissing("farm_name", "varchar(128) null");
    addColumnIfMissing("farm_location", "varchar(128) null");
    addColumnIfMissing("main_species", "varchar(128) null");
    addColumnIfMissing("bio", "varchar(512) null");
    addColumnIfMissing("updated_at", "datetime(6) not null default current_timestamp(6)");
  }

  private void addColumnIfMissing(String column, String definition) {
    Integer count = jdbcTemplate.queryForObject(
        "select count(*) from information_schema.columns where table_schema = database() and table_name = 'users' and column_name = ?",
        Integer.class,
        column
    );
    if (count == null || count == 0) {
      jdbcTemplate.execute("alter table users add column " + column + " " + definition);
    }
  }
}
