package common;

import java.io.Serializable;

/**
 * Represents a generic message sent between the Client and the Server.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private ActionType action;
    private Object content;

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