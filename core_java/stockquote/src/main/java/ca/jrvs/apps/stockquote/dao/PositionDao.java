package ca.jrvs.apps.stockquote.dao;

import ca.jrvs.apps.stockquote.model.Quote;
import ca.jrvs.apps.stockquote.model.Position;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PositionDao implements CrudDao<Position, String> {

    private final Connection c;
    private final QuoteDao quoteDao; // to fetch associated Quote

    public PositionDao(Connection c) {
        this.c = c;
        this.quoteDao = new QuoteDao(c);
    }

    @Override
    public Position save(Position entity) throws IllegalArgumentException {
        String sql = """
            INSERT INTO position (symbol, number_of_shares, value_paid)
            VALUES (?, ?, ?)
            ON CONFLICT (symbol) DO UPDATE
            SET number_of_shares = EXCLUDED.number_of_shares,
                value_paid = EXCLUDED.value_paid
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, entity.getSymbol());
            ps.setInt(2, entity.getNumberOfShares());
            ps.setDouble(3, entity.getValuePaid());
            ps.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error saving Position: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Position> findById(String symbol) throws IllegalArgumentException {
        String sql = "SELECT * FROM position WHERE symbol = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, symbol);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error finding Position: " + e.getMessage(), e);
        }
    }

    @Override
    public Iterable<Position> findAll() {
        String sql = "SELECT * FROM position";
        List<Position> results = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                results.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error fetching all Positions: " + e.getMessage(), e);
        }
        return results;
    }

    @Override
    public void deleteById(String symbol) throws IllegalArgumentException {
        String sql = "DELETE FROM position WHERE symbol = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, symbol);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error deleting Position: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        String sql = "DELETE FROM position";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalArgumentException("Error deleting all Positions: " + e.getMessage(), e);
        }
    }

    private Position mapRow(ResultSet rs) throws SQLException {
        String symbol = rs.getString("symbol");
        int shares = rs.getInt("number_of_shares");
        var valuePaid = rs.getDouble("value_paid");

        Quote quote = quoteDao.findById(symbol).orElse(null);

        return new Position(symbol, shares, valuePaid, quote);
    }
}