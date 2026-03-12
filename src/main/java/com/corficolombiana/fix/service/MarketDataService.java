package com.company.fix.service;

import com.company.fix.util.FIXMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.*;
import quickfix.fix42.MarketDataRequest;
import quickfix.fix42.SecurityDefinitionRequest;

public class MarketDataService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataService.class);

    private final SessionID sessionID;

    public MarketDataService(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public void requestMarketData(String symbol, char subscriptionType, int marketDepth)
            throws SessionNotFound {
        String mdReqID = FIXMessageUtil.generateMDReqID();

        MarketDataRequest request = new MarketDataRequest(
                new MDReqID(mdReqID),
                new SubscriptionRequestType(subscriptionType),
                new MarketDepth(marketDepth)
        );

        MarketDataRequest.NoMDEntryTypes bidGroup = new MarketDataRequest.NoMDEntryTypes();
        bidGroup.set(new MDEntryType(MDEntryType.BID));
        request.addGroup(bidGroup);

        MarketDataRequest.NoMDEntryTypes offerGroup = new MarketDataRequest.NoMDEntryTypes();
        offerGroup.set(new MDEntryType(MDEntryType.OFFER));
        request.addGroup(offerGroup);

        MarketDataRequest.NoMDEntryTypes tradeGroup = new MarketDataRequest.NoMDEntryTypes();
        tradeGroup.set(new MDEntryType(MDEntryType.TRADE));
        request.addGroup(tradeGroup);

        MarketDataRequest.NoRelatedSym symGroup = new MarketDataRequest.NoRelatedSym();
        symGroup.set(new Symbol(symbol));
        request.addGroup(symGroup);

        String typeStr = switch (subscriptionType) {
            case SubscriptionRequestType.SNAPSHOT -> "SNAPSHOT";
            case SubscriptionRequestType.SNAPSHOT_UPDATES -> "SUBSCRIBE";
            case SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_UPDATE_REQUEST -> "UNSUBSCRIBE";
            default -> String.valueOf(subscriptionType);
        };

        log.info("SENDING MarketDataRequest | MDReqID={} Symbol={} Type={} Depth={}",
                mdReqID, symbol, typeStr, marketDepth);

        Session.sendToTarget(request, sessionID);
    }

    public void unsubscribeMarketData(String originalMdReqID, String symbol)
            throws SessionNotFound {
        MarketDataRequest unsub = new MarketDataRequest(
                new MDReqID(originalMdReqID),
                new SubscriptionRequestType(
                        SubscriptionRequestType.DISABLE_PREVIOUS_SNAPSHOT_UPDATE_REQUEST),
                new MarketDepth(0)
        );

        MarketDataRequest.NoMDEntryTypes bidGroup = new MarketDataRequest.NoMDEntryTypes();
        bidGroup.set(new MDEntryType(MDEntryType.BID));
        unsub.addGroup(bidGroup);

        MarketDataRequest.NoRelatedSym symGroup = new MarketDataRequest.NoRelatedSym();
        symGroup.set(new Symbol(symbol));
        unsub.addGroup(symGroup);

        log.info("SENDING MarketData Unsubscribe | MDReqID={} Symbol={}", originalMdReqID, symbol);

        Session.sendToTarget(unsub, sessionID);
    }

    public void requestSecurityDefinition(String symbol, int requestType)
            throws SessionNotFound {
        String secReqID = FIXMessageUtil.generateSecurityReqID();

        SecurityDefinitionRequest request = new SecurityDefinitionRequest(
                new SecurityReqID(secReqID),
                new SecurityRequestType(requestType)
        );

        if (symbol != null && !symbol.isEmpty()) {
            request.set(new Symbol(symbol));
        }

        log.info("SENDING SecurityDefinitionRequest | ReqID={} Symbol={} RequestType={}",
                secReqID, symbol, requestType);

        Session.sendToTarget(request, sessionID);
    }
}
