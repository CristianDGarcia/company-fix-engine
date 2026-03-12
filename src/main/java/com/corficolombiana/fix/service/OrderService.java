package com.company.fix.service;

import com.company.fix.util.FIXMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.field.*;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;
import quickfix.fix42.OrderStatusRequest;

import java.time.LocalDateTime;

public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final SessionID sessionID;

    public OrderService(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public void sendNewOrder(String symbol, char side, char ordType,
                             double quantity, double price) throws SessionNotFound {
        String clOrdID = FIXMessageUtil.generateClOrdID();

        NewOrderSingle order = new NewOrderSingle(
                new ClOrdID(clOrdID),
                new HandlInst(HandlInst.AUTOMATED_EXECUTION_NO_INTERVENTION),
                new Symbol(symbol),
                new Side(side),
                new TransactTime(LocalDateTime.now()),
                new OrdType(ordType)
        );
        order.set(new OrderQty(quantity));

        if (ordType == OrdType.LIMIT || ordType == OrdType.STOP_LIMIT) {
            order.set(new Price(price));
        }
        if (ordType == OrdType.STOP || ordType == OrdType.STOP_LIMIT) {
            order.set(new StopPx(price));
        }

        order.set(new TimeInForce(TimeInForce.DAY));

        log.info("SENDING NewOrderSingle | ClOrdID={} Symbol={} Side={} OrdType={} Qty={} Price={}",
                clOrdID, symbol, FIXMessageUtil.sideToString(side), ordType, quantity, price);

        Session.sendToTarget(order, sessionID);
    }

    public void sendCancelOrder(String origClOrdID, String symbol, char side)
            throws SessionNotFound {
        String clOrdID = FIXMessageUtil.generateClOrdID();

        OrderCancelRequest cancel = new OrderCancelRequest(
                new OrigClOrdID(origClOrdID),
                new ClOrdID(clOrdID),
                new Symbol(symbol),
                new Side(side),
                new TransactTime(LocalDateTime.now())
        );

        log.info("SENDING OrderCancelRequest | OrigClOrdID={} ClOrdID={} Symbol={}",
                origClOrdID, clOrdID, symbol);

        Session.sendToTarget(cancel, sessionID);
    }

    public void sendReplaceOrder(String origClOrdID, String symbol, char side,
                                 char ordType, double newQty, double newPrice)
            throws SessionNotFound {
        String clOrdID = FIXMessageUtil.generateClOrdID();

        OrderCancelReplaceRequest replace = new OrderCancelReplaceRequest(
                new OrigClOrdID(origClOrdID),
                new ClOrdID(clOrdID),
                new HandlInst(HandlInst.AUTOMATED_EXECUTION_NO_INTERVENTION),
                new Symbol(symbol),
                new Side(side),
                new TransactTime(LocalDateTime.now()),
                new OrdType(ordType)
        );
        replace.set(new OrderQty(newQty));
        if (ordType == OrdType.LIMIT || ordType == OrdType.STOP_LIMIT) {
            replace.set(new Price(newPrice));
        }

        log.info("SENDING OrderCancelReplaceRequest | OrigClOrdID={} ClOrdID={} Symbol={} NewQty={} NewPrice={}",
                origClOrdID, clOrdID, symbol, newQty, newPrice);

        Session.sendToTarget(replace, sessionID);
    }

    public void sendOrderStatusRequest(String clOrdID, String symbol, char side)
            throws SessionNotFound {
        OrderStatusRequest statusReq = new OrderStatusRequest(
                new ClOrdID(clOrdID),
                new Symbol(symbol),
                new Side(side)
        );

        log.info("SENDING OrderStatusRequest | ClOrdID={} Symbol={}", clOrdID, symbol);

        Session.sendToTarget(statusReq, sessionID);
    }
}
