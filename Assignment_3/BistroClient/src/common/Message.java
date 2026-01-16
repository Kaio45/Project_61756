package common;

import java.io.Serializable;

/**
 * Represents a generic message wrapper for communication between Client and Server.
 * Contains an action type and a payload object.
 * @author Group-17
 * @version 1.0
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The action to be performed. */
    private ActionType action;
    
    /** The data associated with the action (can be null). */
    private Object content;

    /**
     * Constructs a new Message.
     *
     * @param action the action type
     * @param content the message content/payload
     */
    public Message(ActionType action, Object content) {
        this.action = action;
        this.content = content;
    }

    public ActionType getAction() {
        return action;
    }

    public void setAction(ActionType action) {
        this.action = action;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Message [Action=" + action + ", Content=" + content + "]";
    }
}
