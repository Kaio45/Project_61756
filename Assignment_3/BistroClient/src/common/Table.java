package common;

import java.io.Serializable;

/**
 * Represents a physical dining table in the restaurant.
 * @author Group-17
 * @version 1.0
 */
public class Table implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** The unique ID of the table. */
    private int tableId;
    
    /** The number of seats at this table. */
    private int seats;
    
    /**
     * Constructs a Table object.
     * @param tableId the table number
     * @param seats the capacity of the table
     */
    public Table(int tableId, int seats) {
        this.tableId = tableId;
        this.seats = seats;
    }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
    
    @Override
    public String toString() {
        return "Table " + tableId + " (" + seats + " seats)";
    }
}
