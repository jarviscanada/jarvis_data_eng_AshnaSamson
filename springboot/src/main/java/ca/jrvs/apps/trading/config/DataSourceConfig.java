package ca.jrvs.apps.trading.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(dataSourceProperties.getUrl());
        ds.setUsername(dataSourceProperties.getUsername());
        ds.setPassword(dataSourceProperties.getPassword());
        ds.setDriverClassName(dataSourceProperties.getDriverClassName());
        ds.setMaximumPoolSize(10);
        return ds;
    }
}
