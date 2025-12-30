package common;

import java.io.Serializable;

/**
 * The Class Order.
 * Represents an order entity in the system, synchronized with the database structure.
 * Implements Serializable to allow network transmission.
 */
public class Order implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int order_number;
	private String order_date; // Format: YYYY-MM-DD
	private int number_of_guests;
	private int confirmation_code;
	private int subscriber_id;
	private String date_of_placing_order;
	
	private String order_time;  // Format: HH:MM (e.g., "19:00")
	private String status;      // Enum-like values: "ACTIVE", "WAITING", "CANCELLED", "DONE"
	private int table_id;       // The physical table assigned (0 if not assigned)
	private String arrival_time; // Actual arrival time for reports
	private String exit_time;    // Actual exit time for reports

	/**
	 * Full Constructor for creating a new/existing order with all details.
	 */
	public Order(int order_number, String order_date, String order_time, int number_of_guests, 
			     int confirmation_code, int subscriber_id, String date_of_placing_order,
			     String status, int table_id) {
		this.order_number = order_number;
		this.order_date = order_date;
		this.order_time = order_time;
		this.number_of_guests = number_of_guests;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
		this.date_of_placing_order = date_of_placing_order;
		this.status = status;
		this.table_id = table_id;
	}
	
	/**
	 * Legacy Constructor (Backwards compatibility).
	 * Sets default values for time ("12:00") and status ("ACTIVE").
	 */
	public Order(int order_number, String order_date, int number_of_guests, int confirmation_code, int subscriber_id, String date_of_placing_order) {
		this(order_number, order_date, "12:00", number_of_guests, confirmation_code, subscriber_id, date_of_placing_order, "ACTIVE", 0);
	}

	
	public int get_order_number() { return order_number; }
	public void set_order_number(int order_number) { this.order_number = order_number; }
	
	public String get_order_date() { return order_date; }
	public void set_order_date(String order_date) { this.order_date = order_date; }
	
	public int get_number_of_guests() { return number_of_guests; }
	public void set_number_of_guests(int number_of_guests) { this.number_of_guests = number_of_guests; }
	
	public int get_confirmation_code() { return confirmation_code; }
	public void set_confirmation_code(int confirmation_code) { this.confirmation_code = confirmation_code; }
	
	public int get_subscriber_id() { return subscriber_id; }
	public void set_subscriber_id(int subscriber_id) { this.subscriber_id = subscriber_id; }
	
	public String get_date_of_placing_order() { return date_of_placing_order; }
	public void set_date_of_placing_order(String date_of_placing_order) { this.date_of_placing_order = date_of_placing_order; }

	
	public String get_order_time() { return order_time; }
	public void set_order_time(String order_time) { this.order_time = order_time; }

	public String get_status() { return status; }
	public void set_status(String status) { this.status = status; }

	public int get_table_id() { return table_id; }
	public void set_table_id(int table_id) { this.table_id = table_id; }
	
	public String get_arrival_time() { return arrival_time; }
	public void set_arrival_time(String arrival_time) { this.arrival_time = arrival_time; }
	
	public String get_exit_time() { return exit_time; }
	public void set_exit_time(String exit_time) { this.exit_time = exit_time; }

	@Override
	public String toString() {
		return "Order #" + order_number + " [" + status + "]";
	}
}
