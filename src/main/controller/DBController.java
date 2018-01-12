package controller;

import controller.OrderController;
import entity.Employee;
import entity.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A class that interfaces with the database.
 */
public class DBController {
    private static Connection db_conn; //The connection to the database.
    private ArrayList<String> listTables = new ArrayList<>(); //The list of tables in the database. //TODO: what for? OrB
    public boolean isTest = false;

    // Date formatter for DB insertions
    private java.text.SimpleDateFormat _simpleDateFormatForDb =
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static DBController instance;

    static {
        instance = new DBController();
    }
    /** Static 'instance' method */
    public static DBController getInstance() {
        return instance;
    }
    /**
     * A private Constructor prevents any other class from
     * instantiating.
     */
    private DBController(){}

    /**
     * Init the Database Controller instance
     * @param url URL of the MySQL server (formatted as mysql://hostname:port/db_name)
     * @param username Username for MySQL server
     * @param password Password for MySQL server
     * @throws SQLException
     */

    public void init(String url, String username, String password) throws SQLException {
        try {
            db_conn = connect(url, username, password);

            /**
             * Get tables in database
             */
            DatabaseMetaData meta = db_conn.getMetaData();
            ResultSet res = meta.getTables(null, null, "%", null);
            while (res.next()) {
                listTables.add(res.getString("TABLE_NAME"));
            }
        }
        catch (SQLException ex)
        {
            throw ex;
        }
    }

    /**
     * Connect to the Database and return the connection object
     * @param url URL of the MySQL server (formatted as mysql://hostname:port/db_name)
     * @param username Username for MySQL Server
     * @param password Password for MySQL Server
     * @return Connection object
     * @throws SQLException If an error occurs in SQL
     */
    public static Connection connect(String url, String username, String password) throws SQLException {
        try
        {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            Connection conn = DriverManager.getConnection("jdbc:" + url, username, password);
            System.out.println("Database connected!");
            return conn;
        }
        catch (SQLException ex)
        {
            throw ex;
        }
    }

     /* Query Parsers */

    /**
     * Returns a string representing the table
     * @param tableName name of the table to query
     * @return a printable table.
     */
    public String GetData(String tableName)
    {
        try {
            if (!listTables.contains(tableName)) { //Check if the table exists in the database.
                String returnString;
                returnString = String.format("Unknown table \"%s\"!\n", tableName);
                returnString += ListTables();
                return returnString;
            }
            ResultSet rs = this.QueryTable(tableName); //Query the table
            switch (tableName) {
                case "employees":
                    //Format printout:
                    String columnNamesEmployees = String.format("%3s %15s %20s %25s", "UID", "Name", "Email", "Creation Time");
                    ArrayList<String> rowsEmployees = new ArrayList<>();
                    while (rs.next()) {
                        String row = String.format("%3d %15s %20s %25s",
                                rs.getInt("UID"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getTimestamp("create_time"));
                        rowsEmployees.add(row);
                    }
                    return TableFormatter(columnNamesEmployees, rowsEmployees);

                case "parking_lots":
                    //Format printout:
                    String columnNamesParkingLots = String.format("%3s %15s %10s %15s", "UID", "Location", "Size", "Manager ID");
                    ArrayList<String> rowsParkingLots = new ArrayList<>();
                    while (rs.next()) {
                        String row = String.format("%3d %15s %10s %15d",
                                rs.getInt("UID"),
                                rs.getString("location"),
                                rs.getInt("rows") + "x" + rs.getInt("columns") + "x" + rs.getInt("depth"),
                                rs.getInt("manager_id"));
                        rowsParkingLots.add(row);

                    }
                    return TableFormatter(columnNamesParkingLots, rowsParkingLots);
                default: //Unknown table - fallback, technically shouldn't happen.
                    String returnString;
                    returnString = "Unknown table \"" + tableName + "\"!\n";
                    returnString += ListTables();
                    return returnString;
            }
        } catch (SQLException e) {
            System.err.printf("Error occurred getting data from table \"%s\":\n%s", tableName, e.getMessage());
            return "Error occurred!\nSee server output for details.";
        }
    }

    /**
     * Formats a table string.
     * @param columnsString the columns of the table.
     * @param rows The rows.
     * @return A table in a string.
     */
    private String TableFormatter(String columnsString, ArrayList<String> rows)
    {
        String returnString =
                String.join("", Collections.nCopies(columnsString.length(),"-"))
                + "\n" + columnsString + "\n"
                + String.join("", Collections.nCopies(columnsString.length(),"-")) + "\n";
        for(String row : rows){
            returnString += row + "\n";
        }
        returnString += String.join("", Collections.nCopies(columnsString.length(),"-"));
        return returnString;
    }

    /* Query Performers */

    /**
     * Queries the given table for all its content.
     * @param tableName  name of the table to query.
     * @return The resultset.
     */
    private ResultSet QueryTable(String tableName)
    {
        return QueryTable(tableName, null, -1);
        /*ResultSet result;
        try {
            Statement stmt = db_conn.createStatement();
            result = stmt.executeQuery(String.format("SELECT * FROM %s",tableName));
        } catch (SQLException e) {
            System.err.printf("An error occured querying table %s:\n%s\n", tableName, e.getMessage());
            e.printStackTrace();
            return null;
        }
        return result;*/
    }
    /**
     * Queries the given table for all its content.
     * @param tableName  name of the table to query.
     * @return The resultset.
     */
    private ResultSet QueryTable(String tableName, String field, int value) {
        String query = String.format("SELECT * FROM %s", tableName);

        if ((field != null) && (value != 0)) {
            query += String.format(" WHERE %s = %s", field, value);
        }

        ResultSet result;
        try {
            Statement stmt = db_conn.createStatement();
            //result = stmt.executeQuery(String.format("SELECT * FROM %s",tableName));
            result = stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.printf("An error occurred querying table %s:\n%s\n", tableName, e.getMessage());
            e.printStackTrace();
            return null;
        }
        return result;
    }

    /* Insertion */

    /**
     * Insert a new employee into the database
     * @param employee Employee objects to insert
     * @return True if successful, False otherwise.
     */
    public boolean InsertEmployee(Employee employee) {
        try {
            Statement stmt = db_conn.createStatement();
            Date creationDate;
            int uid = -1;
            stmt.executeUpdate(String.format("INSERT INTO employees (name, email, password) VALUES ('%s', '%s', '%s')", employee.getName(), employee.getEmail(), employee.getPassword()), Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                uid = rs.getInt(1); //Get the UID from the DB.
            else
                throw new SQLException("Couldn't get auto-generated UID");

            rs = stmt.executeQuery(String.format("SELECT * FROM employees WHERE UID=%d", uid)); //Get the creation time from the DB.
            if (rs.next())
                creationDate = new Date(rs.getTimestamp("create_time").getTime());
            else
                throw new SQLException("Something went wrong retrieving the employee just inserted!");

            employee.setUID(uid);
            employee.setCreationDate(creationDate);

            return true;
        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", employee, e.getMessage());
            return false;
        }
    }

    /**
     * Insert new order / preOrder to DB
     * @param order order object to insert
     * @return True if successful, False otherwise.
     */
    public boolean InsertOrder(Order order){
        if (this.isTest){
            order.setOrderID(1);
            return true;
        }
        try {
            Statement stmt = db_conn.createStatement();
            Date creationDate;
            int uid = -1;
            String _actualTime = (order.getActualExitTime() == null)? _simpleDateFormatForDb.format(new Date(0)) :  _simpleDateFormatForDb.format(order.getActualExitTime());
            stmt.executeUpdate(String.format("INSERT INTO Orders (idCar, idCustomer, idParkingLot, entryTime," +
                            " exitTimeEstimated, exitTimeActual, price)" +
                            " VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                    order.getCarID(), order.getCostumerID(), order.getParkingLotNumber(),
                    _simpleDateFormatForDb.format(order.getEntryTime()),
                    _simpleDateFormatForDb.format(order.getEstimatedExitTime()),
                    _actualTime, order.getPrice()),
                    Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                uid = rs.getInt(1); //Get the UID from the DB.
            else
                throw new SQLException("Couldn't get auto-generated UID");

            rs = stmt.executeQuery(String.format("SELECT * FROM ORDERS WHERE UID=%d", uid)); //Get the creation time from the DB.
            if (rs.next())
                creationDate = new Date(rs.getTimestamp("orderCreationTime").getTime());
            else
                throw new SQLException("Something went wrong retrieving the order just inserted!");

            order.setOrderID(uid);
            order.setCreationTime(creationDate);
            return true;
        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", order, e.getMessage());
            return false;
        }
    }

    /**
     * get orders from DB (all orders)
     * @return all orders in list
     */
    public ArrayList<Order> getAllOrders() {
        return getOrdersByID(-1);
    }

    public ArrayList<Order> parseOrdersFromDB(ResultSet rs) {
        ArrayList<Order> myOrders = new ArrayList<>();
        if (rs == null)
            return null;
        else {
            try {
                while (rs.next()) {
                    Order rowOrder = OrderController.getInstance().makeOrderFromDb(rs.getInt("idOrders"),
                            rs.getInt("customerId"),
                            rs.getInt("carId"),
                            rs.getInt("parkingLotNumber"),
                            rs.getDate("entryTime"),
                            rs.getDate("estimatedExitTime"),
                            rs.getDate("actualExitTime"),
                            rs.getDouble("price"),
                            rs.getDate("creationTime"));
                    myOrders.add(rowOrder);
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "Orders", e.getMessage());
                return null;
            }
        }
        return myOrders;
    }



    /**
     * get orders from DB
     * @param orderId specific order identifier, '-1' for all orders
     * @return specific / all orders , null if error
     */
    public ArrayList<Order> getOrdersByID(int orderId) {
        ArrayList<Order> myOrders = new ArrayList<>();
        ResultSet rs;

        if (orderId == -1) { // get all rows
            rs = QueryTable("ORDERS");

        } else { // get specific order
            rs = QueryTable("ORDERS", "idOrders", orderId);
        }

        return parseOrdersFromDB(rs);
    }

    /**
     * get orders from DB for a user
     * @param userID specific order identifier, '-1' for all orders
     * @return specific / all orders , null if error
     */
    public ArrayList<Order> getOrdersByUserID(int userID) {
        ArrayList<Order> myOrders = new ArrayList<>();
        ResultSet rs;

        if (userID == -1) { // get all rows
            rs = QueryTable("ORDERS");

        } else { // get specific order
            rs = QueryTable("ORDERS", "customerID", userID);
        }

        return parseOrdersFromDB(rs);
    }

    /* Output Formatters */

    /**
     * Returns a list of available tables in the DB.
     * @return just that.
     */
    public String ListTables()
    {
        String returnString = "Available tables:\n";
        for (String tableName : listTables) {
            returnString += "\t\t" + tableName + "\n";
        }
        return returnString;
    }

}
