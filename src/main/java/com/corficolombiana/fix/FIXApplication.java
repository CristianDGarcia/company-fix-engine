package com.company.fix;

import com.company.fix.handler.MessageRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Application;
import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

public class FIXApplication implements Application {

    private static final Logger log = LoggerFactory.getLogger(FIXApplication.class);

    private final MessageRouter messageRouter;

    public FIXApplication() {
        this.messageRouter = new MessageRouter();
    }

    @Override
    public void onCreate(SessionID sessionID) {
        log.info("Session created: {}", sessionID);
    }

    @Override
    public void onLogon(SessionID sessionID) {
        log.info("Logon successful: {}", sessionID);
    }

    @Override
    public void onLogout(SessionID sessionID) {
        log.info("Logout: {}", sessionID);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionID) {
        // Hook to add credentials to Logon message if required by the broker:
        // if (message instanceof quickfix.fix42.Logon) {
        //     message.setString(quickfix.field.Username.FIELD, "user");
        //     message.setString(quickfix.field.Password.FIELD, "pass");
        // }
        log.debug("Admin OUT: {}", message);
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        log.debug("Admin IN: {}", message);
    }

    @Override
    public void toApp(Message message, SessionID sessionID) throws DoNotSend {
        log.debug("App OUT: {}", message);
    }

    @Override
    public void fromApp(Message message, SessionID sessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        log.debug("App IN: {}", message);
        messageRouter.route(message, sessionID);
    }
}
