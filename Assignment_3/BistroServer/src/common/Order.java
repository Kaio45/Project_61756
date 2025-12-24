package common;

import java.io.Serializable;

/**
 * The Class Order.
 */
public class Order implements Serializable {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The order number. */
	private int order_number;
	
	/** The order date. */
	private String order_date;
	
	/** The number of guests. */
	private int number_of_guests;
	
	/** The confirmation code. */
	private int confirmation_code;
	
	/** The subscriber id. */
	private int subscriber_id;
	
	/** The date of placing order. */
	private String date_of_placing_order;

	/**
	 * Instantiates a new order.
	 *
	 * @param order_number the order number
	 * @param order_date the order date
	 * @param number_of_guests the number of guests
	 * @param confirmation_code the confirmation code
	 * @param subscriber_id the subscriber id
	 * @param date_of_placing_order the date of placing order
	 */
	public Order(int order_number, String order_date, int number_of_guests, int confirmation_code, int subscriber_id, String date_of_placing_order) {
		this.order_number = order_number;
		this.order_date = order_date;
		this.number_of_guests = number_of_guests;
		this.confirmation_code = confirmation_code;
		this.subscriber_id = subscriber_id;
		this.date_of_placing_order = date_of_placing_order;
	}

	/**
	 * Gets the order number.
	 *
	 * @return the order number
	 */
	public int get_order_number() { return order_number; }
	
	/**
	 * Gets the order date.
	 *
	 * @return the order date
	 */
	public String get_order_date() { return order_date; }
	
	/**
	 * Gets the number of guests.
	 *
	 * @return the number of guests
	 */
	public int get_number_of_guests() { return number_of_guests; }
	
	/**
	 * Gets the confirmation code.
	 *
	 * @return the confirmation code
	 */
	public int get_confirmation_code() { return confirmation_code; }
	
	/**
	 * Gets the subscriber id.
	 *
	 * @return the subscriber id
	 */
	public int get_subscriber_id() { return subscriber_id; }
	
	/**
	 * Gets the date of placing order.
	 *
	 * @return the date of placing order
	 */
	public String get_date_of_placing_order() { return date_of_placing_order; }
	
	/**
	 * Sets the order number.
	 *
	 * @param order_number the new order number
	 */
	public void set_order_number(int order_number) { this.order_number = order_number; }
	
	/**
	 * Sets the order date.
	 *
	 * @param order_date the new order date
	 */
	public void set_order_date(String order_date) { this.order_date = order_date; }
	
	/**
	 * Sets the number of guests.
	 *
	 * @param number_of_guests the new number of guests
	 */
	public void set_number_of_guests(int number_of_guests) { this.number_of_guests = number_of_guests; }
	
	/**
	 * Sets the confirmation code.
	 *
	 * @param confirmation_code the new confirmation code
	 */
	public void set_confirmation_code(int confirmation_code) { this.confirmation_code = confirmation_code; }
	
	/**
	 * Sets the subscriber id.
	 *
	 * @param subscriber_id the new subscriber id
	 */
	public void set_subscriber_id(int subscriber_id) { this.subscriber_id = subscriber_id; }
	
	/**
	 * Sets the date of placing order.
	 *
	 * @param date_of_placing_order the new date of placing order
	 */
	public void set_date_of_placing_order(String date_of_placing_order) { this.date_of_placing_order = date_of_placing_order; }

	/**
	 * To string.
	 *
	 * @return the string
	 */
	@Override
	public String toString() {
		return "Order #" + order_number;
	}
}