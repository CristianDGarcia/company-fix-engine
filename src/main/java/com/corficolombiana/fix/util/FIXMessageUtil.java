package com.company.fix.util;

import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.field.TimeInForce;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public final class FIXMessageUtil {

    private static final AtomicLong ORDER_COUNTER = new AtomicLong(0);
    private static final AtomicLong MD_COUNTER = new AtomicLong(0);
    private static final AtomicLong SEC_COUNTER = new AtomicLong(0);
    private static final DateTimeFormatter ID_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private FIXMessageUtil() {
    }

    public static String generateClOrdID() {
        return "CORFI-" + LocalDateTime.now().format(ID_FORMAT) + "-" + ORDER_COUNTER.incrementAndGet();
    }

    public static String generateMDReqID() {
        return "MD-" + LocalDateTime.now().format(ID_FORMAT) + "-" + MD_COUNTER.incrementAndGet();
    }

    public static String generateSecurityReqID() {
        return "SEC-" + LocalDateTime.now().format(ID_FORMAT) + "-" + SEC_COUNTER.incrementAndGet();
    }

    public static char parseSide(String side) {
        return switch (side.toUpperCase()) {
            case "BUY" -> Side.BUY;
            case "SELL" -> Side.SELL;
            case "SELL_SHORT", "SHORT" -> Side.SELL_SHORT;
            default -> throw new IllegalArgumentException("Invalid side: " + side);
        };
    }

    public static char parseOrdType(String ordType) {
        return switch (ordType.toUpperCase()) {
            case "MARKET" -> OrdType.MARKET;
            case "LIMIT" -> OrdType.LIMIT;
            case "STOP" -> OrdType.STOP;
            case "STOP_LIMIT" -> OrdType.STOP_LIMIT;
            default -> throw new IllegalArgumentException("Invalid order type: " + ordType);
        };
    }

    public static char parseTimeInForce(String tif) {
        return switch (tif.toUpperCase()) {
            case "DAY" -> TimeInForce.DAY;
            case "GTC" -> TimeInForce.GOOD_TILL_CANCEL;
            case "IOC" -> TimeInForce.IMMEDIATE_OR_CANCEL;
            case "FOK" -> TimeInForce.FILL_OR_KILL;
            case "GTD" -> TimeInForce.GOOD_TILL_DATE;
            default -> throw new IllegalArgumentException("Invalid time in force: " + tif);
        };
    }

    public static String sideToString(char side) {
        return switch (side) {
            case Side.BUY -> "BUY";
            case Side.SELL -> "SELL";
            case Side.SELL_SHORT -> "SELL_SHORT";
            default -> String.valueOf(side);
        };
    }

    public static String ordStatusToString(char ordStatus) {
        return switch (ordStatus) {
            case '0' -> "NEW";
            case '1' -> "PARTIALLY_FILLED";
            case '2' -> "FILLED";
            case '3' -> "DONE_FOR_DAY";
            case '4' -> "CANCELED";
            case '5' -> "REPLACED";
            case '6' -> "PENDING_CANCEL";
            case '7' -> "STOPPED";
            case '8' -> "REJECTED";
            case '9' -> "SUSPENDED";
            case 'A' -> "PENDING_NEW";
            case 'B' -> "CALCULATED";
            case 'C' -> "EXPIRED";
            case 'D' -> "ACCEPTED_FOR_BIDDING";
            case 'E' -> "PENDING_REPLACE";
            default -> "UNKNOWN(" + ordStatus + ")";
        };
    }

    public static String execTypeToString(char execType) {
        return switch (execType) {
            case '0' -> "NEW";
            case '1' -> "PARTIAL_FILL";
            case '2' -> "FILL";
            case '3' -> "DONE_FOR_DAY";
            case '4' -> "CANCELED";
            case '5' -> "REPLACED";
            case '6' -> "PENDING_CANCEL";
            case '7' -> "STOPPED";
            case '8' -> "REJECTED";
            case '9' -> "SUSPENDED";
            case 'A' -> "PENDING_NEW";
            case 'B' -> "CALCULATED";
            case 'C' -> "EXPIRED";
            case 'D' -> "RESTATED";
            case 'E' -> "PENDING_REPLACE";
            default -> "UNKNOWN(" + execType + ")";
        };
    }
}
