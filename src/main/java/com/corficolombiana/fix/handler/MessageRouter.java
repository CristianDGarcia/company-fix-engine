package com.company.fix.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.MsgType;
import quickfix.fix42.*;

public class MessageRouter {

    private static final Logger log = LoggerFactory.getLogger(MessageRouter.class);

    private final ExecutionReportHandler executionReportHandler;
    private final OrderCancelRejectHandler orderCancelRejectHandler;
    private final MarketDataHandler marketDataHandler;
    private final SecurityDefinitionHandler securityDefinitionHandler;

    public MessageRouter() {
        this.executionReportHandler = new ExecutionReportHandler();
        this.orderCancelRejectHandler = new OrderCancelRejectHandler();
        this.marketDataHandler = new MarketDataHandler();
        this.securityDefinitionHandler = new SecurityDefinitionHandler();
    }

    public void route(Message message, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {

        String msgType = message.getHeader().getString(MsgType.FIELD);

        switch (msgType) {
            case MsgType.EXECUTION_REPORT ->
                    executionReportHandler.handle((ExecutionReport) message, sessionID);

            case MsgType.ORDER_CANCEL_REJECT ->
                    orderCancelRejectHandler.handle((OrderCancelReject) message, sessionID);

            case MsgType.MARKET_DATA_SNAPSHOT_FULL_REFRESH ->
                    marketDataHandler.handleSnapshot(
                            (MarketDataSnapshotFullRefresh) message, sessionID);

            case MsgType.MARKET_DATA_INCREMENTAL_REFRESH ->
                    marketDataHandler.handleIncremental(
                            (MarketDataIncrementalRefresh) message, sessionID);

            case MsgType.MARKET_DATA_REQUEST_REJECT ->
                    marketDataHandler.handleReject(
                            (MarketDataRequestReject) message, sessionID);

            case MsgType.SECURITY_DEFINITION ->
                    securityDefinitionHandler.handle(
                            (SecurityDefinition) message, sessionID);

            default ->
                    log.warn("Unhandled message type: {} from session {}", msgType, sessionID);
        }
    }
}
