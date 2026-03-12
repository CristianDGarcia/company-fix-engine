package com.company.fix.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.field.*;
import quickfix.fix42.SecurityDefinition;

public class SecurityDefinitionHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityDefinitionHandler.class);

    public void handle(SecurityDefinition message, SessionID sessionID) throws FieldNotFound {
        String securityReqID = message.getString(SecurityReqID.FIELD);
        String securityResponseID = message.getString(SecurityResponseID.FIELD);
        int responseType = message.getInt(SecurityResponseType.FIELD);

        String responseTypeStr = switch (responseType) {
            case 1 -> "ACCEPT_WITH_REVISIONS";
            case 2 -> "ACCEPT_WITHOUT_REVISIONS";
            case 3 -> "ACCEPT_COMPLETE";
            case 4 -> "LIST_OF_SECURITY_TYPES";
            case 5 -> "REJECT_PROPOSAL";
            case 6 -> "CANNOT_MATCH_SELECTION";
            default -> "UNKNOWN(" + responseType + ")";
        };

        StringBuilder sb = new StringBuilder();
        sb.append("SECURITY DEFINITION | ReqID=").append(securityReqID)
                .append(" RespID=").append(securityResponseID)
                .append(" ResponseType=").append(responseTypeStr);

        if (message.isSetField(Symbol.FIELD)) {
            sb.append(" Symbol=").append(message.getString(Symbol.FIELD));
        }
        if (message.isSetField(SecurityType.FIELD)) {
            sb.append(" SecurityType=").append(message.getString(SecurityType.FIELD));
        }
        if (message.isSetField(SecurityExchange.FIELD)) {
            sb.append(" Exchange=").append(message.getString(SecurityExchange.FIELD));
        }
        if (message.isSetField(Currency.FIELD)) {
            sb.append(" Currency=").append(message.getString(Currency.FIELD));
        }
        if (message.isSetField(MaturityMonthYear.FIELD)) {
            sb.append(" Maturity=").append(message.getString(MaturityMonthYear.FIELD));
        }
        if (message.isSetField(ContractMultiplier.FIELD)) {
            sb.append(" Multiplier=").append(message.getDouble(ContractMultiplier.FIELD));
        }

        log.info(sb.toString());
    }
}
