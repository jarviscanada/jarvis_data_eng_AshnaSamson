package ca.jrvs.apps.stockquote.dao;

import ca.jrvs.apps.stockquote.model.Quote;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuoteDao implements CrudDao<Quote, String> {

    private final Connection c;

    public QuoteDao(Connection c) {
        this.c = c;
    }

    @Override
    public Quote save(Quote entity) throws IllegalArgumentException {
        String sql = """
            INSERT INTO quote (symbol, open, high, low, price, volume,
                               latest_trading_day, previous_close, change,
                               change_percent, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (symbol) DO UPDATE
            SET open = EXCLUDED.open,
                high = EXCLUDED.high,
                low = EXCLUDED.low,
                price = EXCLUDED.price,
                volume = EXCLUDED.volume,
                latest_trading_day = EXCLUDED.latest_trading_day,
                previous_close = EXCLUDED.previous_close,
                change = EXCLUDED.change,
                change_percent = EXCLUDED.change_percent,
                timestamp = EXCLUDED.timestamp
            """;

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, entity.getSymbol());
            ps.setDouble(2, entity.getOpen());
            ps.setDouble(3, entity.getHigh());
            ps.setDouble(4, entity.getLow());
            ps.setDouble(5, entity.getPrice());
            ps.setInt(6, entity.getVolume());
            ps.setDate(7, entity.getLatestTradingDay());
            ps.setDouble(8, entity.getPreviousClose());
            ps.setDouble(9, entity.getChange());
            ps.setString(10, entity.getChangePercent());
            ps.setTimestamp(11, entity.getTimestamp());
            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error saving Quote: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Quote> findById(String symbol) throws IllegalArgumentException {
        String sql = "SELECT * FROM quote WHERE symbol = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, symbol);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error finding Quote: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<Quote> findAll() {
        String sql = "SELECT * FROM quote";
        List<Quote> results = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error fetching all Quotes: " + e.getMessage(), e);
        }
        return results;
    }

    @Override
    public void deleteById(String symbol) throws IllegalArgumentException {
        String sql = "DELETE FROM quote WHERE symbol = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, symbol);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error deleting Quote: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM quote";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error deleting all Quotes: " + e.getMessage(), e);
        }
    }

    private Quote mapRow(ResultSet rs) throws SQLException {
        return new Quote(
                rs.getString("symbol"),
                rs.getDouble("open"),
                rs.getDouble("high"),
                rs.getDouble("low"),
                rs.getDouble("price"),
                rs.getInt("volume"),
                rs.getDate("latest_trading_day"),
                rs.getDouble("previous_close"),
                rs.getDouble("change"),
                rs.getString("change_percent"),
                rs.getTimestamp("timestamp")
        );
    }
}