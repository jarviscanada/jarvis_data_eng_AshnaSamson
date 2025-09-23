package ca.jrvs.apps.stockquote;

import ca.jrvs.apps.stockquote.client.QuoteHTTPHelper;
import ca.jrvs.apps.stockquote.dao.PositionDao;
import ca.jrvs.apps.stockquote.dao.QuoteDao;
import ca.jrvs.apps.stockquote.model.Quote;
import ca.jrvs.apps.stockquote.service.PositionService;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    private static final String API_KEY = "c4ab4d07cemsh83cb82e76e49b53p18cb68jsn7c0c0b0bdb9d";
    private static final String url = "jdbc:postgresql://localhost:5432/database";
    private static final String username = "postgres";
    private static final String password = "postgres";

    public static void main(String[] args) throws SQLException {
        userInterface();

    }

    public static void userInterface() throws SQLException {
        // Initialize
        Connection connection = DriverManager.getConnection(url, username, password);
        System.out.println("Connection Established successfully");
        PositionDao positionDao = new PositionDao(connection);
        QuoteDao quoteDao = new QuoteDao(connection);
        QuoteHTTPHelper quoteClient = new QuoteHTTPHelper(API_KEY);

     //??   PositionService positionService = new PositionService(positionDao, quoteDao, quoteClient);
     //??   positionService.buy("MSFT");
     //??   positionService.savePosition("MSFT", 300);
    }
}