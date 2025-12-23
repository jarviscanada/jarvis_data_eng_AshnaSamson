package ca.jrvs.apps.trading.dao;

import ca.jrvs.apps.trading.model.domain.Quote;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class QuoteDao implements CrudRepository<Quote, String> {

    private static final Logger logger = LoggerFactory.getLogger(QuoteDao.class);

    private static final String TABLE_NAME = "quote";
    private static final String ID_COLUMN_NAME = "ticker";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public QuoteDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName(TABLE_NAME);
    }

    @Override
    public Quote save(Quote quote) {
        if (existsById(quote.getTicker())) {
            int updatedRow = updateQuote(quote);
            if (updatedRow != 1) {
                throw new DataRetrievalFailureException("ERROR: Unable to update quote");
            }
        } else {
            addQuote(quote);
        }
        return quote;
    }

    private void addQuote(Quote quote) {
        SqlParameterSource source = new BeanPropertySqlParameterSource(quote);
        int rows = simpleJdbcInsert.execute(source);
        if (rows != 1) {
            throw new IncorrectResultSizeDataAccessException("ERROR: Failed to insert quote", 1, rows);
        }
    }

    /**
     * Update existing quote
     */
    private int updateQuote(Quote quote) {
        String updateSql =
                "UPDATE quote SET last_price=?, bid_price=?, bid_size=?, ask_price=?, ask_size=? WHERE ticker=?";
        return jdbcTemplate.update(updateSql, getUpdateValues(quote));
    }

    private Object[] getUpdateValues(Quote quote) {
        return new Object[]{
                quote.getLastPrice(),
                quote.getBidPrice(),
                quote.getBidSize(),
                quote.getAskPrice(),
                quote.getAskSize(),
                quote.getTicker()
        };
    }

    @Override
    public <S extends Quote> Iterable<S> saveAll(Iterable<S> quotes) {
        quotes.forEach(this::save);
        return quotes;
    }

    @Override
    public Optional<Quote> findById(String ticker) {
        try {
            return Optional.of(getQuoteByTicker(ticker, false));
        } catch (DataRetrievalFailureException ex) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsById(String ticker) {
        try {
            getQuoteByTicker(ticker, false);
            return true;
        } catch (DataRetrievalFailureException ex) {
            return false;
        }
    }

    /**
     * Get a quote by ticker
     */
    private Quote getQuoteByTicker(String ticker, boolean forUpdate) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLUMN_NAME + "=?";
        if (forUpdate) {
            sql += " FOR UPDATE";
        }

        try {
            return jdbcTemplate.queryForObject(
                    sql,
                    BeanPropertyRowMapper.newInstance(Quote.class),
                    ticker
            );
        } catch (EmptyResultDataAccessException ex) {
            logger.debug("Quote not found for ticker: {}", ticker, ex);
            throw new DataRetrievalFailureException("Quote not found");
        }
    }

    @Override
    public Iterable<Quote> findAll() {
        return getAllQuotes();
    }

    private List<Quote> getAllQuotes() {
        String sql = "SELECT * FROM " + TABLE_NAME;
        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(Quote.class));
    }

    @Override
    public Iterable<Quote> findAllById(Iterable<String> iterable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long count() {
        return getQuoteCount();
    }

    private int getQuoteCount() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    @Override
    public void deleteById(String ticker) {
        deleteQuoteByTicker(ticker);
    }

    private void deleteQuoteByTicker(String ticker) {
        if (ticker == null) {
            throw new IllegalArgumentException("ERROR: Ticker cannot be null");
        }
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + ID_COLUMN_NAME + "=?";
        jdbcTemplate.update(sql, ticker);
    }

    @Override
    public void delete(Quote quote) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteAll(Iterable<? extends Quote> iterable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteAll() {
        deleteAllQuotes();
    }

    private void deleteAllQuotes() {
        String sql = "DELETE FROM " + TABLE_NAME;
        jdbcTemplate.update(sql);
    }


}




















//
//
//    @Override
//    public List<Quote> findAll() {
//        return List.of();
//    }
//
//    @Override
//    public List<Quote> findAllById(Iterable<String> iterable) {
//        return List.of();
//    }
//
//    @Override
//    public long count() {
//        return 0;
//    }
//
//    @Override
//    public void deleteById(String s) {
//
//    }
//
//    @Override
//    public void deleteAll() {
//
//    }
//
//    @Override
//    public <S extends Quote> S save(S s) {
//        return null;
//    }
//
//    @Override
//    public <S extends Quote> List<S> saveAll(Iterable<S> iterable) {
//        return List.of();
//    }
//
//    @Override
//    public Optional<Quote> findById(String s) {
//        return Optional.empty();
//    }
//
//    @Override
//    public boolean existsById(String s) {
//        return false;
//    }
//
//    @Override
//    public <S extends Quote> List<S> findAll(Example<S> example) {
//        return List.of();
//    }
//}

//By extending JpaRepository<Quote, String>, Spring automatically gives you:
/*
Quote save(Quote quote);
List<Quote> saveAll(Iterable<Quote> quotes);
List<Quote> findAll();
Optional<Quote> findById(String ticker);
boolean existsById(String ticker);
void deleteById(String ticker);
long count();
void deleteAll();
 */
