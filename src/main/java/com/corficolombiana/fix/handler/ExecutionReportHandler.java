package com.company.fix.handler;

import com.company.fix.util.FIXMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix42.ExecutionReport;

public class ExecutionReportHandler {

    private static final Logger log = LoggerFactory.getLogger(ExecutionReportHandler.class);

    public void handle(ExecutionReport message, SessionID sessionID) throws FieldNotFound {
        String orderID = message.getString(OrderID.FIELD);
        String clOrdID = message.getString(ClOrdID.FIELD);
        String execID = message.getString(ExecID.FIELD);
        char execTransType = message.getChar(ExecTransType.FIELD);
        char execType = message.getChar(ExecType.FIELD);
        char ordStatus = message.getChar(OrdStatus.FIELD);
        String symbol = message.getString(Symbol.FIELD);
        char side = message.getChar(Side.FIELD);
        double cumQty = message.getDouble(CumQty.FIELD);
        double leavesQty = message.getDouble(LeavesQty.FIELD);
        double avgPx = message.getDouble(AvgPx.FIELD);

        double lastShares = 0;
        double lastPx = 0;
        if (message.isSetField(LastShares.FIELD)) {
            lastShares = message.getDouble(LastShares.FIELD);
        }
        if (message.isSetField(LastPx.FIELD)) {
            lastPx = message.getDouble(LastPx.FIELD);
        }

        String text = message.isSetField(Text.FIELD) ? message.getString(Text.FIELD) : "";

        String execTypeStr = FIXMessageUtil.execTypeToString(execType);
        String statusStr = FIXMessageUtil.ordStatusToString(ordStatus);
        String sideStr = FIXMessageUtil.sideToString(side);

        switch (execType) {
            case ExecType.NEW -> log.info(
                    "ORDER ACCEPTED | OrderID={} ClOrdID={} Symbol={} Side={} Status={}",
                    orderID, clOrdID, symbol, sideStr, statusStr);

            case ExecType.PARTIAL_FILL -> log.info(
                    "PARTIAL FILL | OrderID={} Symbol={} Side={} LastQty={} LastPx={} CumQty={} LeavesQty={} AvgPx={}",
                    orderID, symbol, sideStr, lastShares, lastPx, cumQty, leavesQty, avgPx);

            case ExecType.FILL -> log.info(
                    "FULLY FILLED | OrderID={} Symbol={} Side={} LastQty={} LastPx={} CumQty={} AvgPx={}",
                    orderID, symbol, sideStr, lastShares, lastPx, cumQty, avgPx);

            case ExecType.CANCELED -> log.warn(
                    "ORDER CANCELED | OrderID={} ClOrdID={} Symbol={} Side={} CumQty={} Text={}",
                    orderID, clOrdID, symbol, sideStr, cumQty, text);

            case ExecType.REPLACED -> log.info(
                    "ORDER REPLACED | OrderID={} ClOrdID={} Symbol={} Side={} LeavesQty={}",
                    orderID, clOrdID, symbol, sideStr, leavesQty);

            case ExecType.REJECTED -> log.error(
                    "ORDER REJECTED | OrderID={} ClOrdID={} Symbol={} Side={} Text={}",
                    orderID, clOrdID, symbol, sideStr, text);

            case ExecType.PENDING_NEW -> log.info(
                    "PENDING NEW | OrderID={} ClOrdID={} Symbol={} Side={}",
                    orderID, clOrdID, symbol, sideStr);

            case ExecType.PENDING_CANCEL -> log.info(
                    "PENDING CANCEL | OrderID={} ClOrdID={} Symbol={}",
                    orderID, clOrdID, symbol);

            default -> log.info(
                    "EXECUTION REPORT | ExecType={} OrderID={} ClOrdID={} Symbol={} Side={} Status={} CumQty={} AvgPx={}",
                    execTypeStr, orderID, clOrdID, symbol, sideStr, statusStr, cumQty, avgPx);
        }
    }
}
