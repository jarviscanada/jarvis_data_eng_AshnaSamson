package ca.jrvs.apps.trading;

import ca.jrvs.apps.trading.config.MarketDataConfig;
import javax.sql.DataSource;

import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
@ComponentScan(basePackages = {"ca.jrvs.apps.trading.dao", "ca.jrvs.apps.trading.service", "ca.jrvs.apps.trading"})
@EnableConfigurationProperties(TestDataSourceProperties.class)
public class TestConfig {

    private final Logger logger = LoggerFactory.getLogger(TestConfig.class);

//    @Bean
//    public MarketDataConfig marketDataConfig() {
//        MarketDataConfig marketDataConfig = new MarketDataConfig();
//        marketDataConfig.setHost("https://www.alphavantage.co/query?");
//        marketDataConfig.setToken(System.getenv("ALPHAVANTAGE_API_KEY"));
//        return marketDataConfig;
//    }

//    @Bean
//    public HttpClientConnectionManager httpClientConnectionManager() {
//        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
//        cm.setMaxTotal(50);
//        cm.setDefaultMaxPerRoute(50);
//        return cm;
//    }

      @Bean
      @ConditionalOnBean(DataSource.class)
      @Primary
      public DataSource testDataSource(TestDataSourceProperties testDataSourceProperties) {
          DriverManagerDataSource ds = new DriverManagerDataSource();
          ds.setUrl(testDataSourceProperties.getUrl());
          ds.setDriverClassName(testDataSourceProperties.getDriverClassName());
          return ds;
      }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource ds) {
        return new JdbcTemplate(ds);
    }
}


//    @Bean
//    public DataSource dataSource() {
//        BasicDataSource ds = new BasicDataSource();
//        ds.setDriverClassName("org.postgresql.Driver");
//        ds.setUrl(System.getenv().getOrDefault("PSQL_URL", "jdbc:postgresql://localhost:5432/jrvstrading"));
//        ds.setUsername(System.getenv().getOrDefault("PSQL_USER", "postgres"));
//        ds.setPassword(System.getenv().getOrDefault("PSQL_PASSWORD", "password"));
//        // Optional but good practice for tests
////        ds.setInitialSize(1);
////        ds.setMaxTotal(5);
//        return ds;
//    }
