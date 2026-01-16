package common;

import java.io.Serializable;

/**
 * Represents a dining order in the Bistro system.
 * Contains all details regarding the reservation, including time, date, guests, and status.
 * Implements Serializable for network transmission.
 * @author Group-17
 * @version 1.0
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The unique order number. */
    private int order_number;
    
    /** The date of the visit (YYYY-MM-DD). */
    private String order_date;
    
    /** The time of the visit (HH:MM). */
    private String order_time;
    
    /** The number of guests. */
    private int number_of_guests;
    
    /** The confirmation code provided to the client. */
    private int confirmation_code;
    
    /** The ID of the subscriber (if applicable, 0 otherwise). */
    private int subscriber_id;
    
    /** The date and time when the order was created. */
    private String date_of_placing_order;
    
    /** The current status of the order (e.g., ACTIVE, WAITING, APPROVED). */
    private String status;
    
    /** The ID of the assigned table. */
    private int table_id;
    
    /** The contact phone number. */
    private String phone;
    
    /** The client's email address. */
    private String email; 
    
    /** The contact person's name (for casual clients). */
    private String contactName; 
    
    /** The client's display name. */
    private String client_name;

    /**
     * Constructs a new Order object.
     *
     * @param order_number the unique order ID
     * @param order_date the date of the order
     * @param order_time the time of the order
     * @param number_of_guests the number of people
     * @param confirmation_code the secure code for identification
     * @param subscriber_id the subscriber ID
     * @param date_of_placing_order timestamp of creation
     * @param status the order status
     * @param table_id the table ID
     * @param contactName the contact name
     * @param phone the phone number
     */
    public Order(int order_number, String order_date, String order_time, int number_of_guests, 
                 int confirmation_code, int subscriber_id, String date_of_placing_order, 
                 String status, int table_id, String contactName, String phone) {
        this.order_number = order_number;
        this.order_date = order_date;
        this.order_time = order_time;
        this.number_of_guests = number_of_guests;
        this.confirmation_code = confirmation_code;
        this.subscriber_id = subscriber_id;
        this.date_of_placing_order = date_of_placing_order;
        this.status = status;
        this.table_id = table_id;
        this.contactName = contactName;
        this.phone = phone;
    }

    // Getters and Setters

    public int get_order_number() { return order_number; }
    public void set_order_number(int order_number) { this.order_number = order_number; }

    public String get_order_date() { return order_date; }
    public void set_order_date(String order_date) { this.order_date = order_date; }

    public String get_order_time() { return order_time; }
    public void set_order_time(String order_time) { this.order_time = order_time; }

    public int get_number_of_guests() { return number_of_guests; }
    public void set_number_of_guests(int number_of_guests) { this.number_of_guests = number_of_guests; }

    public int get_confirmation_code() { return confirmation_code; }
    public void set_confirmation_code(int confirmation_code) { this.confirmation_code = confirmation_code; }

    public int get_subscriber_id() { return subscriber_id; }
    public void set_subscriber_id(int subscriber_id) { this.subscriber_id = subscriber_id; }

    public String get_date_of_placing_order() { return date_of_placing_order; }
    public void set_date_of_placing_order(String date_of_placing_order) { this.date_of_placing_order = date_of_placing_order; }

    public String get_status() { return status; }
    public void set_status(String status) { this.status = status; }

    public int get_table_id() { return table_id; }
    public void set_table_id(int table_id) { this.table_id = table_id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String get_name() { return client_name; }
    public void set_name(String client_name) { this.client_name = client_name; }

    @Override
    public String toString() {
        return "Order #" + order_number + " (" + status + ") - " + order_date + " " + order_time;
    }
}
