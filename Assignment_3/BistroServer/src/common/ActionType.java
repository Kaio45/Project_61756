package common;

/**
 * Enum defining all possible actions/commands that can be sent between the Client and the Server.
 */
public enum ActionType {
    
    // User / Login Actions
    LOGIN,
    LOGOUT,
    
    // Order Actions
    GET_ORDER,
    ADD_ORDER,
    UPDATE_ORDER,
    
    // System Actions
    CONNECTION_TEST
}