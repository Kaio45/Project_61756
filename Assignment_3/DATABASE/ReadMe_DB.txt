
                 BISTRO SYSTEM - DATABASE INFORMATION

Group: 17
Database Name: order_sch
Export File: G17_Assignment3_DB.sql

1. INSTALLATION INSTRUCTIONS
----------------------------
To import the database structure and data:
1. Open MySQL Workbench.
2. Go to Server -> Data Import.
3. Select "Import from Self-Contained File" and choose 'G17_Assignment3_DB.sql'.
4. Select target schema: 'order_sch' (or create new if needed).
5. Click "Start Import".

2. DATABASE CONNECTION CONFIGURATION (Server Side)
--------------------------------------------------
The Server application attempts to connect to local MySQL with:
- DB URL: jdbc:mysql://localhost/order_sch
- DB User: root
- DB Password: abc123

* Note: If your local MySQL root password is different, please update the 
  Server configuration or run with compatible credentials.

3. APPLICATION LOGIN CREDENTIALS (For Testing)
----------------------------------------------
Use these users to log in to the Client application:

A. MANAGER LOGIN:
   - Username: admin
   - Password: 1234

B. STAFF LOGIN:
   - Username: staff1
   - Password: 1234

C. SUBSCRIBER LOGIN (Identification):
   - You can identify as a subscriber using ID: 12345 (Titi Cohen)
   - You can identify as a subscriber using ID: 1 (Tate Moshen)

4. DATABASE CONTENT SUMMARY
---------------------------
The database 'order_sch' contains the following tables:
- users: Application users (Manager/Staff) with authentication info.
- subscribers: Registered customers club members.
- orders: Full history of orders (Active, Finished, Cancelled).
- restaurant_tables: Map of available tables and seat counts.
- restaurant_settings: Opening hours and operational settings.

Data has been pre-loaded with orders for January 2026 to allow 
generating reports and viewing history immediately.