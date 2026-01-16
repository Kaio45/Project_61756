package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker; 
import javafx.scene.control.ComboBox; 
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import common.Order;
import common.Message;
import common.ActionType;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.scene.control.TextInputDialog;

/**
 * The main controller for the Order Management screen.
 * Handles creation, updates, deletion, and tracking of orders.
 * Manages UI permissions based on the logged-in user type (Manager, Staff, Subscriber, Guest).
 * @author Group-17
 * @version 1.0
 */
public class OrderFrameController {

	@FXML private TextField idTextField;
	@FXML private DatePicker datePicker;
	@FXML private ComboBox<String> timeComboBox;
	@FXML private TextField nameTextField;
	@FXML private TextField guestsTextField;
	@FXML private TextField statusTextField;
	@FXML private Label messageLabel;
	@FXML private Label nameLabel; 
	@FXML private Button smartCheckInBtn; 
	@FXML private Button saveBtn;
	@FXML private Button waitingListBtn;
	@FXML private Button deleteBtn;
	@FXML private Button searchBtn;
	@FXML private Button updateBtn;
	@FXML private Button myOrderBtn;
	@FXML private Button logoutBtn;
	@FXML private Button registerBtn;
	@FXML private Button arrivedBtn;
	@FXML private Button finishedBtn;
	@FXML private Button reportBtn;
	@FXML private Button manageBtn;
	@FXML private Button editProfileBtn;
	@FXML private Button dailyReportBtn;
	@FXML private Button clientHistoryBtn; 
	@FXML private Button viewWaitingListBtn;
	@FXML private Button searchClientBtn; 
	
	private ChatClient client;
	private Order lastAttemptedOrder;
	private String userType;
	private int currentUserId;
	private String guestName;
	private String guestPhone;
	private String guestEmail;

	/**
	 * Sets the client instance for communication.
	 * @param client the ChatClient instance
	 */
	public void setClient(ChatClient client) {
		this.client = client;
	}
	
	/**
	 * Initializes the controller class.
	 * Automatically populates the time selection list (08:00 to 23:30).
	 */
	@FXML
	public void initialize() {
		ArrayList<String> times = new ArrayList<>();
		for (int h = 8; h < 24; h++) {
			times.add(String.format("%02d:00", h));
			times.add(String.format("%02d:30", h));
		}
		timeComboBox.getItems().addAll(times);
	}

	/**
	 * Initializes the UI permissions and fields based on the user type.
	 *
	 * @param userType the type of the user (e.g., Staff, Manager, Subscriber)
	 * @param id the user ID
	 * @param name the user's name
	 * @param phone the user's phone
	 * @param email the user's email
	 */
	public void initPermissions(String userType, int id, String name, String phone, String email) {
		this.userType = userType;
		this.currentUserId = id;
		this.guestName = name;
		this.guestPhone = phone;
		this.guestEmail = email;

		clearFields();

		if (name != null && !name.isEmpty())
			messageLabel.setText("Welcome, " + name);
		else
			messageLabel.setText("Welcome, " + userType);

		// --- Reset buttons (hide all initially) ---
		if (reportBtn != null)
			reportBtn.setVisible(false);
		if (arrivedBtn != null)
			arrivedBtn.setVisible(false);
		if (finishedBtn != null)
			finishedBtn.setVisible(false);
		if (manageBtn != null)
			manageBtn.setVisible(false);
		if (editProfileBtn != null)
			editProfileBtn.setVisible(false);

		if (dailyReportBtn != null)
			dailyReportBtn.setVisible(false);
		if (viewWaitingListBtn != null)
			viewWaitingListBtn.setVisible(false);
		
		// Hide staff specific fields by default
		if (smartCheckInBtn != null)
			smartCheckInBtn.setVisible(false);
		if (searchClientBtn != null)
			searchClientBtn.setVisible(false);
		if (nameLabel != null)
			nameLabel.setVisible(false);
		if (nameTextField != null)
			nameTextField.setVisible(false);

		// --- Logic based on User Type ---
		if (userType.equalsIgnoreCase("Staff") || userType.equalsIgnoreCase("Manager")) {
			idTextField.setDisable(false);
			searchBtn.setVisible(true);
			updateBtn.setVisible(true);
			if (registerBtn != null)
				registerBtn.setVisible(true);
			myOrderBtn.setVisible(false);

			if (manageBtn != null)
				manageBtn.setVisible(true);
			if (smartCheckInBtn != null)
				smartCheckInBtn.setVisible(true);
			if (searchClientBtn != null)
				searchClientBtn.setVisible(true);
			if (nameLabel != null)
				nameLabel.setVisible(true);
			if (nameTextField != null)
				nameTextField.setVisible(true);
			if (searchClientBtn != null)
				searchClientBtn.setVisible(true);
			
			if (dailyReportBtn != null)
				dailyReportBtn.setVisible(true);
			if (viewWaitingListBtn != null)
				viewWaitingListBtn.setVisible(true);

			// Only Manager can see reports
			if (userType.equalsIgnoreCase("Manager")) {
				if (reportBtn != null)
					reportBtn.setVisible(true);
			} else {
				if (reportBtn != null)
					reportBtn.setVisible(false);
			}

		} else if (userType.equals("Subscriber")) {
			idTextField.setDisable(true);
			idTextField.setPromptText("Auto-Filled");
			searchBtn.setVisible(false);
			updateBtn.setVisible(false);
			if (registerBtn != null)
				registerBtn.setVisible(false);
			myOrderBtn.setVisible(true);

			if (editProfileBtn != null)
				editProfileBtn.setVisible(true);

		} else {
			// Guest View
			idTextField.setDisable(true);
			searchBtn.setVisible(false);
			updateBtn.setVisible(false);
			deleteBtn.setVisible(false);
			myOrderBtn.setVisible(false);
			if (registerBtn != null)
				registerBtn.setVisible(false);
			if (userType.equals("GuestView"))
				deleteBtn.setVisible(true);
		}
	}

	/**
	 * Handles saving a new order.
	 * Validates all inputs including date, time, and guest count before sending to server.
	 *
	 * @param event the button click event
	 */
	@FXML
	public void saveOrder(ActionEvent event) {
		// Reset state
		waitingListBtn.setVisible(false);
		deleteBtn.setVisible(false);
		messageLabel.setText("");

		// 1. Validate Date
		if (datePicker.getValue() == null) {
			messageLabel.setText("Please Select a Date!");
			return;
		}

		// 2. Validate Time
		if (timeComboBox.getValue() == null) {
			messageLabel.setText("Please Select a Time!");
			return;
		}

		// 3. Validate Guest Count
		String guestsStr = guestsTextField.getText().trim();
		if (guestsStr.isEmpty()) {
			messageLabel.setText("Please enter number of guests!");
			return;
		}

		try {
			// 4. Parse number and check range
			int guests = Integer.parseInt(guestsStr);

			if (guests <= 0) {
				messageLabel.setText("Guests must be at least 1");
				return;
			}

			// --- Inputs Validated ---

			String date = datePicker.getValue().toString(); 
			String time = timeComboBox.getValue(); 

			String type = userType.trim();
			System.out.println("DEBUG: User Type is -> '" + type + "'"); 

			// Apply restrictions for non-staff users
			if (!type.equalsIgnoreCase("Staff") && !type.equalsIgnoreCase("Manager")) {
				try {
					java.time.LocalDate selectedDate = datePicker.getValue();
					java.time.LocalTime selectedTime = java.time.LocalTime.parse(time);

					// Check if order is for today
					if (selectedDate.equals(java.time.LocalDate.now())) {
						// Require 1 hour notice
						if (selectedTime.isBefore(java.time.LocalTime.now().plusHours(1))) {
							messageLabel.setText("Reservation needs 1 hour notice.");
							return; 
						}
					}

					// Block past dates
					if (selectedDate.isBefore(java.time.LocalDate.now())) {
						messageLabel.setText("Cannot order to the past!");
						return;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// ==========================================

			// Identify User Logic
			int subIdToUse = 1;
			String cName = null, cPhone = null;

			if (userType.equals("Subscriber")) {
				subIdToUse = currentUserId;
			} else if (userType.equals("Guest")) {
				subIdToUse = 999;
				cName = guestName;
				cPhone = guestPhone;
			}

			// Generate codes
			int randomCode = 100000 + new java.util.Random().nextInt(900000);

			Order newOrder = new Order(0, date, time, guests, randomCode, subIdToUse, LocalDate.now().toString(),
					"PENDING", 0, cName, cPhone);
			if (userType.equalsIgnoreCase("Staff") || userType.equalsIgnoreCase("Manager")) {
				if (nameTextField != null && !nameTextField.getText().trim().isEmpty()) {
					newOrder.set_name(nameTextField.getText().trim());
				}
			} else if (userType.equalsIgnoreCase("Guest")) {
				if (this.guestName != null && !this.guestName.isEmpty()) {
					newOrder.set_name(this.guestName);
				}
			}
			newOrder.setEmail(this.guestEmail);

			this.lastAttemptedOrder = newOrder;

			// Send to server
			if (client != null) {
				client.handleMessageFromClientUI(new Message(ActionType.ADD_ORDER, newOrder));
			} else {
				messageLabel.setText("No connection to server");
			}

		} catch (NumberFormatException e) {
			messageLabel.setText("Guests field must contain numbers only!");
		} catch (Exception e) {
			messageLabel.setText("Invalid Input");
			e.printStackTrace();
		}
	}

	/**
	 * Handles updating an existing order.
	 * Sends a request to modify the date, time, or guest count of a loaded order.
	 *
	 * @param event the button click event
	 */
	@FXML
	public void updateOrder(ActionEvent event) {
		if (datePicker.getValue() == null) {
			messageLabel.setText("Please Select a Date!");
			return;
		}
		if (timeComboBox.getValue() == null) {
			messageLabel.setText("Please Select a Time!");
			return;
		}

		String idStr = idTextField.getText();
		if (idStr.isEmpty()) {
			messageLabel.setText("No Order ID loaded!");
			return;
		}

		try {
			int orderId = Integer.parseInt(idStr);
			int guests = Integer.parseInt(guestsTextField.getText().trim());

			if (guests <= 0) {
				messageLabel.setText("Guests must be at least 1");
				return;
			}

			String date = datePicker.getValue().toString();
			String time = timeComboBox.getValue();

			// Create update object focusing on ID, Date, Time, Guests
			Order orderToUpdate = new Order(orderId, date, time, guests, 0, 0, null, "", 0, "", "");

			if (client != null) {
				client.handleMessageFromClientUI(new Message(ActionType.UPDATE_ORDER, orderToUpdate));
				messageLabel.setText("Updating...");
			}

		} catch (NumberFormatException e) {
			messageLabel.setText("Error: Guests/ID must be numbers.");
		} catch (Exception e) {
			messageLabel.setText("Error updating order.");
			e.printStackTrace();
		}
	}

	/**
	 * Populates the UI fields with data from a provided Order object.
	 * Adjusts button visibility based on the order status and user permissions.
	 *
	 * @param order the order object to load
	 */
	private void fillOrderFields(Order order) {
	    // Keep track of this order for history/actions
	    this.lastAttemptedOrder = order;

	    // 1. Fill Fields
	    idTextField.setText(String.valueOf(order.get_order_number()));

	    try {
	        if (order.get_order_date() != null)
	            datePicker.setValue(LocalDate.parse(order.get_order_date()));
	        
	        String t = order.get_order_time();
	        if (t != null && t.length() == 4 && !t.contains(":"))
	            t = t.substring(0, 2) + ":" + t.substring(2);
	        timeComboBox.setValue(t);
	    } catch (Exception e) {
	    }

	    guestsTextField.setText(String.valueOf(order.get_number_of_guests()));
	    statusTextField.setText(order.get_status());

	    // 2. Reset buttons
	    waitingListBtn.setVisible(false);
	    deleteBtn.setVisible(false);
	    if (saveBtn != null)
	        saveBtn.setVisible(false);
	    if (arrivedBtn != null)
	        arrivedBtn.setVisible(false);
	    if (finishedBtn != null)
	        finishedBtn.setVisible(false);

	    if (clientHistoryBtn != null)
	        clientHistoryBtn.setVisible(false);

	    String currentStatus = order.get_status().trim().toUpperCase();

	    // --- 3. Deletion Permissions ---

	    // Group A: Staff, Manager, GuestView
	    if (userType.equals("Staff") || userType.equalsIgnoreCase("Manager") || userType.equals("GuestView")) {
	        deleteBtn.setVisible(true);
	    }
	    // Group B: Subscribers
	    else if (userType.equals("Subscriber")) {
	        if (currentStatus.equals("APPROVED") || currentStatus.equals("WAITING")
	                || currentStatus.equals("PENDING")) {
	            deleteBtn.setVisible(true);
	        } else {
	            deleteBtn.setVisible(false);
	        }
	    }

	    // 4. Logic based on User Type

	    // === Staff and Managers ===
	    if (userType.equalsIgnoreCase("Staff") || userType.equalsIgnoreCase("Manager")) {
	        // Check-in and Finish buttons
	        if (currentStatus.equalsIgnoreCase("APPROVED")) {
	            if (arrivedBtn != null) {
	                arrivedBtn.setVisible(true);
	                arrivedBtn.setText("Client Arrived");
	            }
	        } else if (currentStatus.equalsIgnoreCase("ACTIVE") || currentStatus.equalsIgnoreCase("ARRIVED")) {
	            if (finishedBtn != null)
	                finishedBtn.setVisible(true);
	        }

	        // --- CRM: History Button ---
	        if (clientHistoryBtn != null && order.get_subscriber_id() > 0) {
	            clientHistoryBtn.setVisible(true);
	        }
	    }

	    // === Clients (Subscriber / Casual / GuestView) ===
	    else if (userType.equals("Subscriber") || userType.equals("GuestView") || userType.equals("Customer")) {
	        
	        // A. Before Seating (Get Table Number)
	        if (currentStatus.equalsIgnoreCase("APPROVED")) {
	            if (isTimeForCheckIn(order.get_order_date(), order.get_order_time())) {
	                if (arrivedBtn != null) {
	                    arrivedBtn.setVisible(true);
	                    arrivedBtn.setText("Get Table Number");
	                }
	            }
	        }
	        
	        // B. Currently Seated (Pay & Leave)
	        else if (currentStatus.equalsIgnoreCase("ACTIVE") || currentStatus.equalsIgnoreCase("ARRIVED")) {
	             if (finishedBtn != null) {
	                 finishedBtn.setVisible(true);
	                 finishedBtn.setText("Pay & Leave"); 
	             }
	        }
	    }
	}
	
	private void clearFields() {
		idTextField.clear();
		datePicker.setValue(null);
		timeComboBox.setValue(null);
		guestsTextField.clear();
		deleteBtn.setVisible(false);
		statusTextField.setText("-");
	}

	/**
	 * Requests a report of all orders for the current day.
	 * @param event the button click event
	 */
	@FXML
	public void getDailyOrders(ActionEvent event) {
		if (client != null) {
			client.handleMessageFromClientUI(new Message(ActionType.GET_DAILY_REPORT, null));
		}
	}

	/**
	 * Requests the waiting list report.
	 * @param event the button click event
	 */
	@FXML
	public void getWaitingList(ActionEvent event) {
		if (client != null) {
			client.handleMessageFromClientUI(new Message(ActionType.GET_WAITING_LIST, null));
		}
	}

	// --- Navigation and Menu Actions ---
	@FXML
	public void openManagement(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ManagementFrame.fxml"));
			Parent root = loader.load();
			Stage stage = new Stage();
			stage.setTitle("Restaurant Management");
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void loadMyOrder(ActionEvent event) {
		if (client != null) {
			// Subscriber: Get own history
			if (userType.equals("Subscriber")) {
				client.handleMessageFromClientUI(new Message(ActionType.GET_ORDER, currentUserId));
				messageLabel.setText("Searching history...");
			}
			// Staff/Manager: Get ALL orders
			else if (userType.equalsIgnoreCase("Staff") || userType.equalsIgnoreCase("Manager")) {
				client.handleMessageFromClientUI(new Message(ActionType.GET_ALL_ORDERS, null));
				messageLabel.setText("Loading all orders...");
			}
		}
	}

	@FXML
	public void searchOrder(ActionEvent event) {
		String id = idTextField.getText();
		if (!id.isEmpty() && client != null) {
			client.handleMessageFromClientUI(new Message(ActionType.GET_ORDER, id));
			waitingListBtn.setVisible(false);
			deleteBtn.setVisible(false);
			messageLabel.setText("Searching...");
		}
	}

	@FXML
	public void deleteOrder(ActionEvent event) {
		String idStr = idTextField.getText();
		if (idStr.isEmpty())
			return;
		try {
			if (client != null) {
				client.handleMessageFromClientUI(new Message(ActionType.DELETE_ORDER, Integer.parseInt(idStr)));
				messageLabel.setText("Deleting...");
			}
		} catch (NumberFormatException e) {
			messageLabel.setText("Invalid ID");
		}
	}

	@FXML
	public void enterWaitingList(ActionEvent event) {
		if (lastAttemptedOrder != null && client != null) {
			lastAttemptedOrder.set_status("WAITING");
			lastAttemptedOrder.set_table_id(-1);
			client.handleMessageFromClientUI(new Message(ActionType.ADD_ORDER, lastAttemptedOrder));
			waitingListBtn.setVisible(false);
			messageLabel.setText("Requesting Waiting List...");
		}
	}

	@FXML
	public void logout(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/LoginFrame.fxml"));
			Parent root = loader.load();
			ChatClient.loginController = loader.getController();
			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.show();
			((Stage) logoutBtn.getScene().getWindow()).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void openRegister(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/RegisterFrame.fxml"));
			Parent root = loader.load();
			ChatClient.registerController = loader.getController();
			Stage stage = new Stage();
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void openEditProfile(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/EditProfileFrame.fxml"));
			Parent root = loader.load();
			EditProfileController ctrl = loader.getController();
			ctrl.initData(currentUserId, guestName, guestPhone, guestEmail);
			Stage stage = new Stage();
			stage.setTitle("Edit Profile");
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void disconnectBtn(ActionEvent event) {
		try {
			if (client != null)
				client.closeConnection();
		} catch (Exception e) {
		}
		System.exit(0);
	}

	@FXML
	public void markArrived(ActionEvent event) {
		String idStr = idTextField.getText();
		if (!idStr.isEmpty()) {
			client.handleMessageFromClientUI(new Message(ActionType.MARK_ARRIVED, Integer.parseInt(idStr)));
			messageLabel.setText("Updating status...");
			arrivedBtn.setVisible(false);
		}
	}

	@FXML
	public void markFinished(ActionEvent event) {
		String idStr = idTextField.getText();
		if (!idStr.isEmpty()) {
			client.handleMessageFromClientUI(new Message(ActionType.MARK_FINISHED, Integer.parseInt(idStr)));
			messageLabel.setText("Closing order...");
			finishedBtn.setVisible(false);
		}
	}

	@FXML
	public void openReports(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/ReportFrame.fxml"));
			Parent root = loader.load();
			ReportController controller = loader.getController();
			controller.requestReportData();
			Stage stage = new Stage();
			stage.setTitle("Monthly Reports");
			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the UI fields based on messages received from the server.
	 * Handles reports, order details, and success/failure notifications.
	 *
	 * @param msg the message object received from the server
	 */
	public void updateFields(Object msg) {
		Platform.runLater(() -> {
			if (msg instanceof Message) {
				Message receivedMsg = (Message) msg;

				// --- Checks by ActionType ---

				// 1. Daily Report
				if (receivedMsg.getAction() == ActionType.GET_DAILY_REPORT) {
					showScrollableAlert("Daily Active Orders", (String) receivedMsg.getContent());
					return;
				}
				// 2. Waiting List Report
				if (receivedMsg.getAction() == ActionType.GET_WAITING_LIST) {
					showScrollableAlert("Waiting List", (String) receivedMsg.getContent());
					return;
				}

				// >>> 3. Smart Check-In (Identification) <<<
				if (receivedMsg.getAction() == ActionType.GET_APPROVED_ORDERS_FOR_TODAY) {
					ArrayList<String> list = (ArrayList<String>) receivedMsg.getContent();
					showSmartSelectionDialog(list); 
					return;
				}
				// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

				// 4. Handle General Content
				Object content = receivedMsg.getContent();

				if (content instanceof ArrayList) {
					// --- Handle History ---
					ArrayList<Order> list = (ArrayList<Order>) content;
					if (!list.isEmpty()) {
						try {
							FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/HistoryFrame.fxml"));
							Parent root = loader.load();
							HistoryController historyCtrl = loader.getController();
							historyCtrl.setOrderList(list);
							historyCtrl.setMainController(this);
							Stage stage = new Stage();
							stage.setTitle("My Order History");
							stage.setScene(new Scene(root));
							stage.show();
							messageLabel.setText("History loaded.");
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						messageLabel.setText("No orders found in history.");
					}
				} else if (content instanceof Order) {
					fillOrderFields((Order) content);
					messageLabel.setText("Order found.");
				} else if (content instanceof String) {
					String response = (String) content;
					messageLabel.setText(response);

					// --- Client Details & CRM History ---
					if (response.contains("Subscriber Details:")) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Client Details");
						alert.setHeaderText("Client Found");
						alert.setContentText(response);
						alert.showAndWait();

						if (searchedClientId > 0) {
							client.handleMessageFromClientUI(
									new common.Message(common.ActionType.GET_HISTORY_BY_USER_ID, searchedClientId));
						}
						return;
					}

					if (response.contains("Assigned Table")) {
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Welcome");
						alert.setHeaderText("Check-In Successful");
						alert.setContentText(response + "\nPlease proceed to your table.");
						alert.showAndWait();

						clearFields();
						if (arrivedBtn != null)
							arrivedBtn.setVisible(false);
					}

					// --- SMS Simulation & Billing ---
					if (response.contains("Finished")) {
						String billMessage = "Bill Sent via SMS";

						if (lastAttemptedOrder != null && lastAttemptedOrder.get_subscriber_id() > 1
								&& lastAttemptedOrder.get_subscriber_id() != 999) {

							billMessage = "Bill include 10% discount";
						}

						System.out.println(billMessage);
						clearFields();
						if (finishedBtn != null)
							finishedBtn.setVisible(false);

						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Simulation");
						alert.setHeaderText("Bill Sent");
						alert.setContentText(billMessage);
						alert.show();
					}

					// --- Success / Failure Messages ---
					if (response.toLowerCase().contains("success")) {
						if (response.contains("Order Created") && lastAttemptedOrder != null) {
							showConfirmationCode(lastAttemptedOrder.get_confirmation_code());
							if (!userType.equals("Subscriber") && !userType.equals("Customer")) {
								clearFields();
							}
						}

						if (response.contains("Deleted")) {
							clearFields();
							deleteBtn.setVisible(false);
							saveBtn.setVisible(false);
						} else {
							saveBtn.setVisible(true);
							saveBtn.setDisable(false);
						}
						waitingListBtn.setVisible(false);
					} else if (response.toLowerCase().contains("no available table")
							|| response.toLowerCase().contains("full")) {
						waitingListBtn.setVisible(true);
					} else {
						waitingListBtn.setVisible(false);
						saveBtn.setVisible(true);
						saveBtn.setDisable(false);
					}
				}
			}
		});
	}

	private void showScrollableAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(title);

		// Create scrollable text area
		TextArea textArea = new TextArea(content);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);

		// Layout
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(textArea, 0, 0);

		alert.getDialogPane().setContent(expContent);
		alert.showAndWait();
	}

	/**
	 * Validates if the current time allows for checking in.
	 * Window: 30 minutes before to 15 minutes after the reservation time.
	 *
	 * @param orderDate the date of the order
	 * @param orderTime the time of the order
	 * @return true if within the check-in window, false otherwise
	 */
	private boolean isTimeForCheckIn(String orderDate, String orderTime) {
		try {
			// Normalize time string if needed
			if (orderTime != null && orderTime.length() == 4 && !orderTime.contains(":")) {
				orderTime = orderTime.substring(0, 2) + ":" + orderTime.substring(2);
			}

			java.time.LocalDate date = java.time.LocalDate.parse(orderDate);
			java.time.LocalTime time = java.time.LocalTime.parse(orderTime);

			java.time.LocalDateTime orderDateTime = java.time.LocalDateTime.of(date, time);
			java.time.LocalDateTime now = java.time.LocalDateTime.now();

			// Check date
			if (!date.equals(java.time.LocalDate.now()))
				return false;

			// Check time window
			java.time.LocalDateTime startWindow = orderDateTime.minusMinutes(30);
			java.time.LocalDateTime endWindow = orderDateTime.plusMinutes(15);

			return now.isAfter(startWindow) && now.isBefore(endWindow);

		} catch (Exception e) {
			System.out.println("Time Check Error: " + e.getMessage()); 
			return false;
		}
	}

	@FXML
	public void viewClientHistory(ActionEvent event) {
		if (lastAttemptedOrder != null) {
			int targetId = lastAttemptedOrder.get_subscriber_id();
			System.out.println("DEBUG: Looking for history of User ID: " + targetId);
			client.handleMessageFromClientUI(new common.Message(common.ActionType.GET_HISTORY_BY_USER_ID, targetId));
		} else {
			messageLabel.setText("No client selected.");
		}
	}

	/**
	 * Loads order details into the main form.
	 * Typically called from the HistoryController when a row is selected.
	 *
	 * @param order the selected order
	 */
	public void loadOrderFromHistory(Order order) {
		fillOrderFields(order);
		messageLabel.setText("Order " + order.get_order_number() + " loaded.");
	}

	private int searchedClientId = 0;

	@FXML
	public void searchClientById(ActionEvent event) {
		// 1. Input Dialog
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Search Client");
		dialog.setHeaderText("Client Lookup");
		dialog.setContentText("Please enter Subscriber ID:");

		// 2. Get Result
		java.util.Optional<String> result = dialog.showAndWait();

		result.ifPresent(idStr -> {
			try {
				int id = Integer.parseInt(idStr);
				this.searchedClientId = id; 

				// 3. Request details from server
				client.handleMessageFromClientUI(new common.Message(common.ActionType.GET_SUBSCRIBER_DETAILS, id));

			} catch (NumberFormatException e) {
				messageLabel.setText("Invalid ID format.");
			}
		});
	}

	/**
	 * Triggers the Smart Check-In process.
	 * Fetches the list of approved orders for today to allow quick selection.
	 *
	 * @param event the button click event
	 */
	@FXML
	public void smartCheckIn(ActionEvent event) {
		if (client != null) {
			client.handleMessageFromClientUI(new common.Message(common.ActionType.GET_APPROVED_ORDERS_FOR_TODAY, null));
		}
	}

	/**
	 * Displays a dialog for selecting an arriving client from a list.
	 *
	 * @param ordersList the list of strings representing today's approved orders
	 */
	private void showSmartSelectionDialog(ArrayList<String> ordersList) {
		if (ordersList.isEmpty()) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Smart Check-In");
			alert.setHeaderText("No Arrivals Pending");
			alert.setContentText("All orders for today have already arrived (or none exist).");
			alert.showAndWait();
			return;
		}

		// Selection Dialog
		javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>(ordersList.get(0),
				ordersList);
		dialog.setTitle("Quick Identification");
		dialog.setHeaderText("Select Arriving Client (No Manual Entry)");
		dialog.setContentText("Choose Order:");

		java.util.Optional<String> result = dialog.showAndWait();

		result.ifPresent(selectedString -> {
			try {
				// Format: "Order #105 | 18:00..."
				// Extract the number after '#'
				String[] parts = selectedString.split("#");
				String idPart = parts[1].split(" ")[0]; 

				// --- Automatic Action ---
				idTextField.setText(idPart); // 1. Fill ID
				searchOrder(null); // 2. Trigger Search

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void showConfirmationCode(int code) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Order Confirmed");
		alert.setHeaderText("Order Success");
		alert.setContentText("Your Code: " + code);
		alert.showAndWait();
	}
}
