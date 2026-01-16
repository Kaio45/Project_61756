package common;

import java.io.Serializable;

/**
 * Represents a registered subscriber in the Bistro system.
 * Holds personal details and account information.
 * @author Group-17
 * @version 1.0
 */
public class Subscriber implements Serializable {
    
    private static final long serialVersionUID = 1L;

    /** The unique subscriber ID. */
    private int id;
    
    /** The subscriber's first name. */
    private String firstName;
    
    /** The subscriber's last name. */
    private String lastName;
    
    /** The subscriber's phone number. */
    private String phone;
    
    /** The subscriber's email address. */
    private String email;
    
    /** The credit card number (stored for billing). */
    private String creditCardNumber; 
    
    /** The internal subscriber number. */
    private int subscriberNumber;
    
    /** The username for system login. */
    private String username;

    /**
     * Full constructor for creating a Subscriber.
     *
     * @param id the subscriber ID
     * @param firstName the first name
     * @param lastName the last name
     * @param phone the phone number
     * @param email the email address
     * @param creditCardNumber the credit card number
     * @param subscriberNumber the internal number
     */
    public Subscriber(int id, String firstName, String lastName, String phone, String email, String creditCardNumber, int subscriberNumber) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.creditCardNumber = creditCardNumber;
        this.subscriberNumber = subscriberNumber;
    }

    /**
     * Partial constructor for basic details.
     *
     * @param id the subscriber ID
     * @param firstName the first name
     * @param lastName the last name
     * @param phone the phone number
     * @param email the email address
     */
    public Subscriber(int id, String firstName, String lastName, String phone, String email) {
        this(id, firstName, lastName, phone, email, null, 0);
    }

    // Getters and Setters

    public void setUsername(String username) { this.username = username; }
    public String getUsername() { return username; }

    public String getPhoneNumber() { return phone; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getCreditCardNumber() { return creditCardNumber; }
    public void setCreditCardNumber(String creditCardNumber) { this.creditCardNumber = creditCardNumber; }

    public int getSubscriberNumber() { return subscriberNumber; }
    public void setSubscriberNumber(int subscriberNumber) { this.subscriberNumber = subscriberNumber; }
    
    @Override
    public String toString() {
        return String.format("Subscriber [ID=%d, Name=%s %s, Phone=%s]", id, firstName, lastName, phone);
    }
}