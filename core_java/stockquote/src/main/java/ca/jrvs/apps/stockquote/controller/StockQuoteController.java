package ca.jrvs.apps.stockquote.controller;

import java.util.Optional;
import java.util.Scanner;

import ca.jrvs.apps.stockquote.model.Quote;
import ca.jrvs.apps.stockquote.service.PositionService;
import ca.jrvs.apps.stockquote.service.QuoteService;


public class StockQuoteController {

    private QuoteService quoteService;
    private PositionService positionService;

    public StockQuoteController(QuoteService quoteService,
                                PositionService positionService) {
        this.quoteService = quoteService;
        this.positionService = positionService;
    }

    /**
     * User interface for our application
     * Displays a Console UI for the App
     */
    public void initClient() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("----- Welcome to the Stock Quote App -----");

        while (running) {
            System.out.println("\nChoose an option:");
            System.out.println("1. View stock quote");
            System.out.println("2. Buy stock");
            System.out.println("3. Sell stock");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1": // View quote
                        System.out.print("Enter ticker symbol: ");
                        String symbol = scanner.nextLine().trim().toUpperCase();
                        Optional<Quote> optQuote = quoteService.fetchQuoteDataFromAPI(symbol);

                        if (optQuote.isPresent()) {
                            Quote q = optQuote.get();
                            System.out.println("Latest quote: " + q);
                        } else {
                            System.out.println("No quote found for " + symbol);
                        }



                        break;

                    case "2": // Buy stock
                        System.out.print("Enter ticker symbol: ");
                        symbol = scanner.nextLine().trim().toUpperCase();
                        System.out.print("Enter quantity to buy: ");
                        int qty = Integer.parseInt(scanner.nextLine().trim());
                        positionService.buy(symbol, qty); // service does the logic
                        System.out.println("Bought " + qty + " shares of " + symbol);
                        break;

                    case "3": // Sell stock
                        System.out.print("Enter ticker symbol to sell (all shares): ");
                        symbol = scanner.nextLine().trim().toUpperCase();
                        positionService.sell(symbol); // service liquidates entire position
                        System.out.println("Sold all shares of " + symbol);
                        break;

                    case "4": // Exit
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid option. Please try again!");
                }
            } catch (Exception e) {
                // Catch anything from service layer or parsing so the app keeps running
                System.out.println("Error: " + e.getMessage());
            }
        }

        System.out.println("----- Goodbye! ------");
        scanner.close();
    }

}

