package com.company.fix;

import com.company.fix.service.MarketDataService;
import com.company.fix.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.SessionID;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.field.SubscriptionRequestType;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

public class FIXEngineApp {

    private static final Logger log = LoggerFactory.getLogger(FIXEngineApp.class);

    private FIXInitiator fixInitiator;
    private OrderService orderService;
    private MarketDataService marketDataService;

    public static void main(String[] args) {
        String configFile = "quickfix.cfg";
        boolean serviceMode = false;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--service" -> serviceMode = true;
                case "--config" -> {
                    if (i + 1 < args.length) {
                        configFile = args[++i];
                    } else {
                        System.err.println("Error: --config requires a file path argument");
                        System.exit(1);
                    }
                }
                default -> {
                    System.err.println("Unknown argument: " + args[i]);
                    System.err.println("Usage: FIXEngine [--service] [--config <path>]");
                    System.exit(1);
                }
            }
        }

        FIXEngineApp app = new FIXEngineApp();
        if (serviceMode) {
            app.runAsService(configFile);
        } else {
            app.runInteractive(configFile);
        }
    }

    public void runAsService(String configFile) {
        CountDownLatch shutdownLatch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received.");
            if (fixInitiator != null) {
                fixInitiator.stop();
            }
            shutdownLatch.countDown();
        }, "shutdown-hook"));

        try {
            fixInitiator = new FIXInitiator(configFile);
            fixInitiator.start();

            log.info("FIX Engine running as service. Waiting for shutdown signal...");
            shutdownLatch.await();
            log.info("FIX Engine service stopped.");

        } catch (Exception e) {
            log.error("Fatal error in service mode: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    public void runInteractive(String configFile) {
        try {
            fixInitiator = new FIXInitiator(configFile);
            fixInitiator.start();

            waitForLogon();

            SessionID sessionID = fixInitiator.getSessionID();
            orderService = new OrderService(sessionID);
            marketDataService = new MarketDataService(sessionID);

            runInteractiveCLI();

        } catch (Exception e) {
            log.error("Fatal error: {}", e.getMessage(), e);
        } finally {
            if (fixInitiator != null) {
                fixInitiator.stop();
            }
        }
    }

    private void waitForLogon() throws InterruptedException {
        log.info("Waiting for logon...");
        int attempts = 0;
        while (!fixInitiator.isLoggedOn() && attempts < 30) {
            Thread.sleep(1000);
            attempts++;
        }
        if (!fixInitiator.isLoggedOn()) {
            throw new RuntimeException("Failed to logon within 30 seconds");
        }
        log.info("Logon established.");
    }

    private void runInteractiveCLI() {
        Scanner scanner = new Scanner(System.in);
        printHelp();

        while (true) {
            System.out.print("\nFIX> ");
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                switch (command) {
                    case "buy" -> handleBuySell(parts, Side.BUY);
                    case "sell" -> handleBuySell(parts, Side.SELL);
                    case "cancel" -> handleCancel(parts);
                    case "replace" -> handleReplace(parts);
                    case "status" -> handleStatus(parts);
                    case "md" -> handleMarketData(parts);
                    case "secdef" -> handleSecurityDefinition(parts);
                    case "help" -> printHelp();
                    case "quit", "exit" -> {
                        log.info("Shutting down...");
                        return;
                    }
                    default -> System.out.println("Unknown command. Type 'help' for usage.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format - " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                log.error("Command error", e);
            }
        }
    }

    private void handleBuySell(String[] parts, char side) throws Exception {
        if (parts.length < 3) {
            System.out.println("Usage: buy|sell SYMBOL QTY [PRICE]");
            return;
        }
        String symbol = parts[1].toUpperCase();
        double qty = Double.parseDouble(parts[2]);
        if (parts.length >= 4) {
            double price = Double.parseDouble(parts[3]);
            orderService.sendNewOrder(symbol, side, OrdType.LIMIT, qty, price);
        } else {
            orderService.sendNewOrder(symbol, side, OrdType.MARKET, qty, 0);
        }
    }

    private void handleCancel(String[] parts) throws Exception {
        if (parts.length < 4) {
            System.out.println("Usage: cancel ORIG_CLORDID SYMBOL BUY|SELL");
            return;
        }
        char side = parts[3].equalsIgnoreCase("BUY") ? Side.BUY : Side.SELL;
        orderService.sendCancelOrder(parts[1], parts[2].toUpperCase(), side);
    }

    private void handleReplace(String[] parts) throws Exception {
        if (parts.length < 6) {
            System.out.println("Usage: replace ORIG_CLORDID SYMBOL BUY|SELL QTY PRICE");
            return;
        }
        char side = parts[3].equalsIgnoreCase("BUY") ? Side.BUY : Side.SELL;
        double qty = Double.parseDouble(parts[4]);
        double price = Double.parseDouble(parts[5]);
        orderService.sendReplaceOrder(parts[1], parts[2].toUpperCase(), side,
                OrdType.LIMIT, qty, price);
    }

    private void handleStatus(String[] parts) throws Exception {
        if (parts.length < 4) {
            System.out.println("Usage: status CLORDID SYMBOL BUY|SELL");
            return;
        }
        char side = parts[3].equalsIgnoreCase("BUY") ? Side.BUY : Side.SELL;
        orderService.sendOrderStatusRequest(parts[1], parts[2].toUpperCase(), side);
    }

    private void handleMarketData(String[] parts) throws Exception {
        if (parts.length < 2) {
            System.out.println("Usage: md SYMBOL [snapshot|subscribe|unsubscribe MDREQID]");
            return;
        }
        String symbol = parts[1].toUpperCase();
        String type = parts.length >= 3 ? parts[2].toLowerCase() : "snapshot";

        switch (type) {
            case "snapshot" ->
                    marketDataService.requestMarketData(symbol,
                            SubscriptionRequestType.SNAPSHOT, 0);
            case "subscribe" ->
                    marketDataService.requestMarketData(symbol,
                            SubscriptionRequestType.SNAPSHOT_UPDATES, 0);
            case "unsubscribe" -> {
                if (parts.length < 4) {
                    System.out.println("Usage: md SYMBOL unsubscribe MDREQID");
                    return;
                }
                marketDataService.unsubscribeMarketData(parts[3], symbol);
            }
            default ->
                    System.out.println("Unknown md type. Use: snapshot|subscribe|unsubscribe");
        }
    }

    private void handleSecurityDefinition(String[] parts) throws Exception {
        String symbol = parts.length >= 2 ? parts[1].toUpperCase() : null;
        int requestType = symbol != null ? 0 : 3;
        marketDataService.requestSecurityDefinition(symbol, requestType);
    }

    private void printHelp() {
        System.out.println("""
                ================================================
                  Company FIX Engine - Interactive CLI
                  Protocol: FIX 4.2 | Role: Initiator
                ================================================
                Commands:
                  buy SYMBOL QTY [PRICE]                          - Buy order (market if no price, limit if price given)
                  sell SYMBOL QTY [PRICE]                         - Sell order (market if no price, limit if price given)
                  cancel ORIG_CLORDID SYMBOL BUY|SELL             - Cancel an existing order
                  replace ORIG_CLORDID SYMBOL BUY|SELL QTY PRICE  - Modify an existing order
                  status CLORDID SYMBOL BUY|SELL                  - Request order status
                  md SYMBOL [snapshot|subscribe|unsubscribe ID]   - Market data operations
                  secdef [SYMBOL]                                 - Security definition request
                  help                                            - Show this help
                  quit                                            - Shutdown engine

                Examples:
                  buy ECOPETROL 1000 2850.50       - Buy 1000 ECOPETROL at 2850.50
                  sell PFBCOLOM 500                 - Sell 500 PFBCOLOM at market
                  md ECOPETROL subscribe            - Subscribe to ECOPETROL market data
                  secdef ECOPETROL                  - Get ECOPETROL security definition
                """);
    }
}
