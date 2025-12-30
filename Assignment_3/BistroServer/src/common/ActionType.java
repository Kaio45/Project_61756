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
    
    GET_ALL_TABLES,       // Client requests the list of all tables
    CHECK_AVAILABILITY    // Optional: Explicit check for availability
}
