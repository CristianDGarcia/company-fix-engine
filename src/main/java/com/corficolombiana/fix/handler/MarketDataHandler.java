package com.company.fix.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix42.MarketDataIncrementalRefresh;
import quickfix.fix42.MarketDataRequestReject;
import quickfix.fix42.MarketDataSnapshotFullRefresh;

public class MarketDataHandler {

    private static final Logger log = LoggerFactory.getLogger(MarketDataHandler.class);

    public void handleSnapshot(MarketDataSnapshotFullRefresh message, SessionID sessionID)
            throws FieldNotFound {
        String symbol = message.getString(Symbol.FIELD);
        int noEntries = message.getInt(NoMDEntries.FIELD);

        log.info("MARKET DATA SNAPSHOT | Symbol={} Entries={}", symbol, noEntries);

        for (int i = 1; i <= noEntries; i++) {
            Group group = message.getGroup(i, NoMDEntries.FIELD);
            char entryType = group.getChar(MDEntryType.FIELD);
            double price = group.getDouble(MDEntryPx.FIELD);

            double size = 0;
            if (group.isSetField(MDEntrySize.FIELD)) {
                size = group.getDouble(MDEntrySize.FIELD);
            }

            String typeStr = mdEntryTypeToString(entryType);
            log.info("  {} | Price={} Size={}", typeStr, price, size);
        }
    }

    public void handleIncremental(MarketDataIncrementalRefresh message, SessionID sessionID)
            throws FieldNotFound {
        int noEntries = message.getInt(NoMDEntries.FIELD);

        log.info("MARKET DATA INCREMENTAL | Entries={}", noEntries);

        for (int i = 1; i <= noEntries; i++) {
            Group group = message.getGroup(i, NoMDEntries.FIELD);
            char updateAction = group.getChar(MDUpdateAction.FIELD);
            char entryType = group.getChar(MDEntryType.FIELD);

            String symbol = group.isSetField(Symbol.FIELD) ? group.getString(Symbol.FIELD) : "N/A";
            double price = group.isSetField(MDEntryPx.FIELD) ? group.getDouble(MDEntryPx.FIELD) : 0;
            double size = group.isSetField(MDEntrySize.FIELD) ? group.getDouble(MDEntrySize.FIELD) : 0;

            String actionStr = switch (updateAction) {
                case MDUpdateAction.NEW -> "NEW";
                case MDUpdateAction.CHANGE -> "CHANGE";
                case MDUpdateAction.DELETE -> "DELETE";
                default -> "UNKNOWN(" + updateAction + ")";
            };

            String typeStr = mdEntryTypeToString(entryType);
            log.info("  {} {} | Symbol={} Price={} Size={}", actionStr, typeStr, symbol, price, size);
        }
    }

    public void handleReject(MarketDataRequestReject message, SessionID sessionID)
            throws FieldNotFound {
        String mdReqID = message.getString(MDReqID.FIELD);

        String reason = "UNKNOWN";
        if (message.isSetField(MDReqRejReason.FIELD)) {
            char rejReason = message.getChar(MDReqRejReason.FIELD);
            reason = switch (rejReason) {
                case MDReqRejReason.UNKNOWN_SYMBOL -> "UNKNOWN_SYMBOL";
                case MDReqRejReason.DUPLICATE_MDREQID -> "DUPLICATE_MDREQID";
                case MDReqRejReason.INSUFFICIENT_BANDWIDTH -> "INSUFFICIENT_BANDWIDTH";
                case MDReqRejReason.INSUFFICIENT_PERMISSIONS -> "INSUFFICIENT_PERMISSIONS";
                case MDReqRejReason.UNSUPPORTED_SUBSCRIPTIONREQUESTTYPE -> "UNSUPPORTED_SUBSCRIPTION_TYPE";
                case MDReqRejReason.UNSUPPORTED_MARKETDEPTH -> "UNSUPPORTED_MARKET_DEPTH";
                case MDReqRejReason.UNSUPPORTED_MDUPDATETYPE -> "UNSUPPORTED_MD_UPDATE_TYPE";
                case MDReqRejReason.UNSUPPORTED_AGGREGATEDBOOK -> "UNSUPPORTED_AGGREGATED_BOOK";
                case MDReqRejReason.UNSUPPORTED_MDENTRYTYPE -> "UNSUPPORTED_MD_ENTRY_TYPE";
                default -> "UNKNOWN(" + rejReason + ")";
            };
        }

        String text = message.isSetField(Text.FIELD) ? message.getString(Text.FIELD) : "";

        log.error("MARKET DATA REQUEST REJECTED | MDReqID={} Reason={} Text={}", mdReqID, reason, text);
    }

    private String mdEntryTypeToString(char entryType) {
        return switch (entryType) {
            case MDEntryType.BID -> "BID";
            case MDEntryType.OFFER -> "OFFER";
            case MDEntryType.TRADE -> "TRADE";
            case MDEntryType.INDEX_VALUE -> "INDEX";
            case MDEntryType.OPENING_PRICE -> "OPEN";
            case MDEntryType.CLOSING_PRICE -> "CLOSE";
            case MDEntryType.SETTLEMENT_PRICE -> "SETTLE";
            case MDEntryType.TRADING_SESSION_HIGH_PRICE -> "HIGH";
            case MDEntryType.TRADING_SESSION_LOW_PRICE -> "LOW";
            case MDEntryType.VWAP -> "VWAP";
            default -> "TYPE(" + entryType + ")";
        };
    }
}
