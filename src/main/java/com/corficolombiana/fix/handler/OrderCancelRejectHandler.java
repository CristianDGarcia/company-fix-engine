package com.company.fix.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix42.OrderCancelReject;

public class OrderCancelRejectHandler {

    private static final Logger log = LoggerFactory.getLogger(OrderCancelRejectHandler.class);

    public void handle(OrderCancelReject message, SessionID sessionID) throws FieldNotFound {
        String orderID = message.getString(OrderID.FIELD);
        String clOrdID = message.getString(ClOrdID.FIELD);
        String origClOrdID = message.getString(OrigClOrdID.FIELD);
        char ordStatus = message.getChar(OrdStatus.FIELD);
        char cxlRejResponseTo = message.getChar(CxlRejResponseTo.FIELD);

        int cxlRejReason = -1;
        if (message.isSetField(CxlRejReason.FIELD)) {
            cxlRejReason = message.getInt(CxlRejReason.FIELD);
        }

        String text = message.isSetField(Text.FIELD) ? message.getString(Text.FIELD) : "";

        String responseToStr = cxlRejResponseTo == CxlRejResponseTo.ORDER_CANCEL_REQUEST
                ? "CANCEL" : "CANCEL/REPLACE";

        String reasonStr = switch (cxlRejReason) {
            case 0 -> "TOO_LATE_TO_CANCEL";
            case 1 -> "UNKNOWN_ORDER";
            case 2 -> "BROKER_OPTION";
            default -> "UNKNOWN(" + cxlRejReason + ")";
        };

        log.warn("CANCEL REJECTED | {} Request | OrderID={} ClOrdID={} OrigClOrdID={} Reason={} Text={}",
                responseToStr, orderID, clOrdID, origClOrdID, reasonStr, text);
    }
}
