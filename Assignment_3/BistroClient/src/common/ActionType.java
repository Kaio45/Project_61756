package common;

/**
 * Enumeration of all possible actions/commands that can be sent between Client and Server.
 * @author Group-17
 * @version 1.0
 */
public enum ActionType {
    LOGIN,
    ADD_ORDER,
    GET_ORDER,
    UPDATE_ORDER,
    DELETE_ORDER,
    REGISTER_SUBSCRIBER,
    IDENTIFY_SUBSCRIBER,
    IDENTIFY_BY_CODE,
    GET_SUBSCRIBER_LAST_ORDER,
    MARK_ARRIVED,
    MARK_FINISHED,
    GET_ALL_TABLES,
    GET_REPORT,
    UPDATE_SUBSCRIBER_DETAILS,
    ADD_TABLE,
    DELETE_TABLE,
    UPDATE_OPENING_HOURS,
    RECOVER_SUBSCRIBER_ID,
    GET_ALL_ORDERS,
    UPDATE_TABLE,
    GET_DAILY_REPORT,
    GET_WAITING_LIST,
    GET_HISTORY_BY_USER_ID,
    GET_SUBSCRIBER_DETAILS,
    GET_APPROVED_ORDERS_FOR_TODAY
}
