package controller;

import Exceptions.NotImplementedException;
import entity.*;
import utils.StringUtils;
import utils.TimeUtils;

import java.sql.*;
import java.util.*;
import java.util.Date;

import static controller.Controllers.IS_DEBUG_CONTROLLER;
import static utils.StringUtils.desanitizeFromSQL;
import static utils.StringUtils.sanitizeForSQL;

/**
 * A class that interfaces with the database.
 */
public class DBController {
    private static Connection db_conn; //The connection to the database.
    private ArrayList<String> listTables = new ArrayList<>(); //The list of tables in the database. //TODO: what for? OrB
    public boolean isTest = false;
    public boolean firstRunOfServerToday = false;

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
     * @throws SQLException in case of sql error
     */

    public void init(String url, String username, String password) throws SQLException {
        try {
            db_conn = connect(url, username, password);

            /*
             * Get tables in database
             */
            DatabaseMetaData meta = db_conn.getMetaData();
            ResultSet res = meta.getTables(null, null, "%", null);
            while (res.next()) {
                listTables.add(res.getString("TABLE_NAME"));
            }
            checkFirstRunOfTheDay();
        }
        catch (SQLException ex)
        {
            if (IS_DEBUG_CONTROLLER)
            System.err.println("SQL Exception on DB init");
            throw ex;
        }
    }

    private void checkFirstRunOfTheDay() throws SQLException {
        try{
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            // Update server Login to DB
            String query = String.format("SELECT * FROM ServerLogins WHERE YEAR = %s AND MONTH = %s AND DAY = %s",
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_WEEK));
            Statement stmt = db_conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (!rs.next()) {
                firstRunOfServerToday = true;
                // insert today's login to server
                stmt = db_conn.createStatement();
                query = String.format("INSERT INTO ServerLogins (YEAR, MONTH, DAY) VALUES (%s, %s, %s)",
                        cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_WEEK));
                int result = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
                if (result != 1) {
                    throw new SQLException("Failed inserting today's login to server");
                }
            }
        }catch (SQLException e){
            throw e;
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
    public String GetData(String tableName) throws SQLException
    {
        try {
            if (!listTables.contains(tableName)) { //Check if the table exists in the database.
                String returnString;
                returnString = String.format("Unknown table \"%s\"!\n", tableName);
                returnString += listTables();
                return returnString;
            }
            ResultSet rs = this.queryTable(tableName); //Query the table
            switch (tableName) {
                case "employees":
                    //Format printout:
                    String columnNamesEmployees = String.format("%3s %15s %20s %25s", "UID", "Name", "Email", "Creation Time");
                    ArrayList<String> rowsEmployees = new ArrayList<>();
                    while (rs.next()) {
                        String row = String.format("%3d %15s %20s %25s",
                                rs.getInt("UID"),
                                StringUtils.desanitizeFromSQL(rs.getString("name")),
                                StringUtils.desanitizeFromSQL(rs.getString("email")),
                                returnTimeStampFromDB(rs, "create_time"));
                        rowsEmployees.add(row);
                    }
                    return tableFormatter(columnNamesEmployees, rowsEmployees);

                case "parking_lots":
                    //Format printout:
                    String columnNamesParkingLots = String.format("%3s %15s %10s %15s", "UID", "Location", "Size", "Manager ID");
                    ArrayList<String> rowsParkingLots = new ArrayList<>();
                    while (rs.next()) {
                        String row = String.format("%3d %15s %10s %15d",
                                rs.getInt("UID"),
                                StringUtils.desanitizeFromSQL(rs.getString("location")),
                                rs.getInt("rows") + "x" + rs.getInt("columns") + "x" + rs.getInt("depth"),
                                rs.getInt("manager_id"));
                        rowsParkingLots.add(row);

                    }
                    return tableFormatter(columnNamesParkingLots, rowsParkingLots);
                default: //Unknown table - fallback, technically shouldn't happen.
                    String returnString;
                    returnString = "Unknown table \"" + tableName + "\"!\n";
                    returnString += listTables();
                    return returnString;
            }
        } catch (SQLException e) {
            System.err.printf("Error occurred getting data from table \"%s\":\n%s", tableName, e.getMessage());
            throw e;
        }
    }

    /**
     * Formats a table string.
     * @param columnsString the columns of the table.
     * @param rows The rows.
     * @return A table in a string.
     */
    private String tableFormatter(String columnsString, ArrayList<String> rows)
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
    private ResultSet queryTable(String tableName) throws SQLException
    {
        return queryTable(tableName, null, -1);
    }
    /**
     * Queries the given table for all its content.
     * @param tableName  name of the table to query.
     * @param field name of column to check by
     * @param value value to find with
     * @return  The resultset.
     */
    private ResultSet queryTable(String tableName, String field, int value) throws SQLException {
        String query = String.format("SELECT * FROM %s", tableName);

        if ((field != null) && (value != 0)) {
            query += String.format(" WHERE %s = %s", field, value);
        }

        return callStatement(query, tableName);
    }

    /*
        Overloading function of the function above with field of String type.
     */
    private ResultSet queryTable(String tableName, String field, String value) throws SQLException{
        String query = String.format("SELECT * FROM %s", tableName);

        if ((field != null) && (!value.equals(""))) {
            query += String.format(" WHERE %s = '%s'", field, value);
        }

        return  callStatement(query, tableName);
    }

    /*
        Overloading function of the function above with field of String type.
     */
    private ResultSet queryTable(String tableName, String field, String value, String field2, String value2) throws SQLException{
        String query = String.format("SELECT * FROM %s", tableName);

        if ((field != null) && (!value.equals(""))) {
            query += String.format(" WHERE %s = '%s'", field, value);
        }
        if ((field2 != null) && (!value2.equals(-1))) {
            query += String.format(" AND %s = %s", field2, value2);
        }

        return  callStatement(query, tableName);
    }

    /**
     * Query table with [tableName] and 2 [columns] with 2 [values] with the choise of condition:
     *                  OR, AND, ETC.
     * @param tableName
     * @param field
     * @param value
     * @param condition
     * @param field2
     * @param value2
     * @return
     * @throws SQLException
     */
    private ResultSet queryTable(String tableName, String field, String value, String condition, String field2, String value2) throws SQLException{
        String query = String.format("SELECT * FROM %s", tableName);

        if ((field != null) && (!value.equals(""))) {
            query += String.format(" WHERE %s = '%s'", field, value);
        }
        if ((field2 != null) && (!value2.equals(-1))) {
            query += String.format(" %s %s = %s", condition, field2, value2);
        }

        return  callStatement(query, tableName);
    }

    /**
     * private calling Statement of query (prevent code duplication.
     * @param query
     * @return
     */
    private ResultSet callStatement(String query, String tableName) throws SQLException
    {
        ResultSet result;
        try {
            Statement stmt = db_conn.createStatement();
            result = stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.printf("An error occurred querying table %s:\n%s\n", tableName, e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return result;
    }


    /* Insertion */

    /**
     * Insert a new employee into the database
     * @param employee Employee objects to insert
     * @return True if successful, False otherwise.
     */
    public boolean InsertEmployee(Employee employee) throws SQLException{
        try {
            Statement stmt = db_conn.createStatement();
            Date creationDate;
            int uid = -1;
            stmt.executeUpdate(String.format("INSERT INTO employees (name, email, password) VALUES ('%s', '%s', '%s')", sanitizeForSQL(employee.getName()), sanitizeForSQL(employee.getEmail()), sanitizeForSQL(employee.getPassword())), Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                uid = rs.getInt(1); //Get the UID from the DB.
            else
                throw new SQLException("Couldn't get auto-generated UID");

            rs = stmt.executeQuery(String.format("SELECT * FROM employees WHERE UID=%d", uid)); //Get the creation time from the DB.
            if (rs.next())
                if (returnTimeStampFromDB(rs,"create_time") != null)
                    creationDate = new Date(returnTimeStampFromDB(rs, "create_time").getTime());
                else
                    creationDate = null;
            else
                throw new SQLException("Something went wrong retrieving the employee just inserted!");

            employee.setUID(uid);
            employee.setCreationDate(creationDate);

            return true;
        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", employee, e.getMessage());
            throw e;
        }
    }

    /**
     * Insert new order / preOrder to DB
     * @param order order object to insert
     * @param priceType - the type of user for this order.
     * @return True if successful, False otherwise.
     */
    public boolean insertOrder(Order order, Billing.priceList priceType) throws SQLException{
        if (isTest) {
            order.setOrderID(1);
            return true;
        }
        try {
            String clientType = parsePriceTypeToColumnName(priceType);
            Statement stmt = db_conn.createStatement();
            Date creationDate;
            int uid;
            String _actualExitTime = (order.getActualExitTime() == null)
                    ? "NULL"
                    :  "'" + _simpleDateFormatForDb.format(order.getActualExitTime()) + "'";
            stmt.executeUpdate(String.format("INSERT INTO Orders (idCar, idCustomer, idParkingLot, orderType," +
                            " entryTimeEstimated, entryTimeActual, exitTimeEstimated, exitTimeActual, price, clientType)" +
                            " VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', %s, '%s', '%s')",
                    order.getCarID(), order.getCostumerID(), order.getParkingLotNumber(),
                    order.getOrderStatus(),
                    _simpleDateFormatForDb.format(order.getEstimatedEntryTime()),
                    _simpleDateFormatForDb.format(order.getActualEntryTime()),
                    _simpleDateFormatForDb.format(order.getEstimatedExitTime()),
                    _actualExitTime, order.getPrice(), clientType),
                    Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                uid = rs.getInt(1); //Get the UID from the DB.
            }
            else
                throw new SQLException("Couldn't get auto-generated UID");

            rs = stmt.executeQuery(String.format("SELECT * FROM Orders WHERE idOrders=%d", uid)); //Get the creation time from the DB.
            if (rs.next())
                if (returnTimeStampFromDB(rs,"create_time") != null)
                    creationDate = new Date(returnTimeStampFromDB(rs, "create_time").getTime());
                else
                    creationDate = null;
            else
                throw new SQLException("Something went wrong retrieving the order just inserted!");

            order.setOrderID(uid);
            order.setCreationTime(creationDate);
            return true;
        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", order, e.getMessage());
            throw e;
        }
    }

    /**
     * "Delete" order by setting its type to 'DELETED'
     * @param orderId
     * @return True upon success, false otherwise
     */
    public boolean deleteOrder (int orderId, double charged) throws SQLException
    {

        try {
            Statement stmt = db_conn.createStatement();
            stmt.executeUpdate(String.format("UPDATE Orders SET orderType='DELETED', price= '%s' WHERE  idOrders=%s",
                    charged,
                    orderId),
                    Statement.RETURN_GENERATED_KEYS);
            return true;


        } catch (SQLException e) {
            System.err.printf("An error occurred deleting order with id: %s \n%s\n", orderId, e.getMessage());
            throw e;
        }
    }

    private Timestamp returnTimeStampFromDB(ResultSet rs, String columnLabel)
    {
        try
        {
            return rs.getTimestamp(columnLabel);
        }
        catch (SQLException e)
        {
            return null;
        }
    }


    public Map<Integer, Object> parseOrdersFromDBToMap(ResultSet rs) throws SQLException{
        Map<Integer, Object>  myOrders = new HashMap<>();
        if (rs == null){
            //System.err.println("No orders were read from db");
            return myOrders;
        }
        else {
            try {
                while (rs.next()) {
                    Order.OrderStatus _orderStatus = parseOrderStatus(rs.getString("OrderType"));
                    if (null == _orderStatus){
                        System.err.printf("Error occurred getting OrderStatus from table \"%s\"", "Orders");
                        return myOrders;
                    }
                    /*
                    Saving all orders in the server as "Order" and not differentiating preorders causes lots of problems.
                    Instead, from now on all maps will map to OBJECTS, which we will cast as needed:
                     */
                    switch (_orderStatus)
                    {
                        case PRE_ORDER:
                            PreOrder rowOrder = new PreOrder(
                                    rs.getInt("idCustomer"),
                                    rs.getInt("idCar"),
                                    returnTimeStampFromDB(rs, "exitTimeEstimated"),
                                    rs.getInt("idParkingLot"),
                                    rs.getDouble("price"),
                                    returnTimeStampFromDB(rs, "entryTimeEstimated")
                            );
                            rowOrder.setOrderID(rs.getInt("idOrders"));
                            myOrders.put(rowOrder.getOrderID(), rowOrder);
                            break;
                        case DELETED:
                            break; // should not ever go in here.
                        case IN_PROGRESS:
                            Order activeOrder = new Order(
                                    rs.getInt("idOrders"),
                                    rs.getInt("idCustomer"),
                                    rs.getInt("idCar"),
                                    rs.getInt("idParkingLot"),
                                    _orderStatus,
                                    returnTimeStampFromDB(rs, "entryTimeEstimated"),
                                    returnTimeStampFromDB(rs, "entryTimeActual"),
                                    returnTimeStampFromDB(rs, "exitTimeEstimated"),
                                    returnTimeStampFromDB(rs, "exitTimeActual"),
                                    rs.getDouble("price"),
                                    returnTimeStampFromDB(rs, "orderCreationTime")
                            );

                            // querying the Parking space table here, to make sure order is filled correctly and on time.
                            ArrayList<Integer> myParkingSpace = getParkingSpaceForOrder(rs.getInt("idOrders"));

                            /*if ((myParkingSpace == null)) { //TODO: pass this for debug -- not all "IN_PROGRESS" orders are actually parked.
                                System.err.printf("Error occurred getting parking space data for order %s. not parked?", rs.getInt("idOrders"));
                                throw new SQLException(); // car not parked.
                            }*/
                            if (myParkingSpace.size() == 3) {
                                activeOrder.setParkingSpaceHeight(myParkingSpace.get(0));
                                activeOrder.setParkingSpaceWidth(myParkingSpace.get(1));
                                activeOrder.setParkingSpaceDepth(myParkingSpace.get(2));
                            }
                            myOrders.put(activeOrder.getOrderID(), activeOrder);
                            break;
                    }
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "Orders", e.getMessage());
                throw e;
            }
        }
        return myOrders;
    }

    private ArrayList<Integer> getParkingSpaceForOrder(Integer idOrder) throws SQLException {
        ResultSet rs;
        ArrayList<Integer> myArrayList = new ArrayList<>(3);
        try {
            rs = queryTable("ParkingSpace", "idOccupyingOrder", idOrder);
            if(rs.next()){
                myArrayList.add(0, rs.getInt("height"));
                myArrayList.add(1, rs.getInt("width"));
                myArrayList.add(2, rs.getInt("depth"));
                return myArrayList;
            }else
                return myArrayList;

        }catch (SQLException e){
            System.err.println("Failed retrieving parking space of order "+ idOrder);
            throw e;
        }

    }

    /**
     * get orders from DB (all orders)
     * @return all orders in list
     */
    public Map<Integer, Object> getAllOrders() throws SQLException {
        return getOrdersByID(-1);
    }

    /**
     * get orders from DB
     * @param orderId specific order identifier, '-1' for all orders
     * @return specific / all orders , null if error
     */
    public Map<Integer, Object> getOrdersByID(int orderId) throws SQLException{
        ResultSet rs;

        if (orderId == -1) { // get all rows
            //rs = queryTable("Orders");
            rs = queryTable("Orders", "orderType", "'PRE_ORDER'", "OR", "orderType", "'IN_PROGRESS'");
            // TODO: (maybe). workaround Ma'afan i know.. but it works for now, to filter only active orders from db.

        } else { // get specific order
            rs = queryTable("Orders", "idOrders", orderId);
        }

        return parseOrdersFromDBToMap(rs);
    }

    /**
     * get orders from DB for a user
     * @param userID specific order identifier, '-1' for all orders
     * @return specific / all orders , null if error
     */
    public Map<Integer, Object> getOrdersByUserID(int userID) throws SQLException {
        ResultSet rs;

        if (userID == -1) { // get all rows
            rs = queryTable("Orders");

        } else { // get specific order
            rs = queryTable("Orders", "idCustomer", userID);
        }

        return parseOrdersFromDBToMap(rs);
    }

    /* Output Formatters */

    /**
     * Returns a list of available tables in the DB.
     * @return just that.
     */
    public String listTables() throws SQLException
    {
        String returnString = "Available tables:\n";
        for (String tableName : listTables) {
            returnString += "\t\t" + tableName + "\n";
        }
        return returnString;
    }

    public ArrayList<Object> getParkingLots() throws SQLException{
        return  getParkingLotsByID(-1);
    }


    public ArrayList<Object> getParkingLotsByID(Integer parkingLotID) throws SQLException{
        ResultSet rs;

        if (parkingLotID == -1) { // get all rows
            rs = queryTable("ParkingLots");

        } else { // get specific order
            rs = queryTable("ParkingLots", "idParkingLots", parkingLotID);
        }

        return parseParkingLotsFromDB(rs);

    }

    private ArrayList<Object> parseParkingLotsFromDB(ResultSet rs) throws SQLException{
        ArrayList<Object> myLots = new ArrayList<>();
        if (rs == null){
            return myLots;
        }
        else {
            try {
                while (rs.next()) {
                    ParkingLot rowLot = new ParkingLot(
                            rs.getInt("idParkingLots"),
                            desanitizeFromSQL(rs.getString("location")),
                            rs.getInt("rows"),
                            rs.getInt("columns"),
                            rs.getInt("depth"),
                            rs.getInt("parkingLotManagerId")
                    );
                    myLots.add(rowLot);
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "Parking lots", e.getMessage());
                throw e;
            }
        }
        return myLots;
    }

    /**
     * Returns all the Knowen! parkingSpaces of a parking lot (number)
     * @param parkingLotNumber
     * @return empty if none found, null on exception.
     */
    public ArrayList<ParkingSpace> getParkingSpaces(Integer parkingLotNumber) throws SQLException{
        ResultSet rs = queryTable("ParkingSpace", "idParkingLot", parkingLotNumber);
        return parseParkingSpacesFromDB(rs);
    }

    private ArrayList<ParkingSpace> parseParkingSpacesFromDB(ResultSet rs) throws SQLException{
        ArrayList<ParkingSpace> myParkingSpaces = new ArrayList<>();
        if (rs == null){
            return myParkingSpaces;
        }
        else {
            try {
                while (rs.next()) {
                    ParkingSpace rowParkingSpace = new ParkingSpace(getIntegerNullableFromRS(rs, "idOccupyingOrder"));
                    rowParkingSpace.setHeight(rs.getInt("height"));
                    rowParkingSpace.setWidth(rs.getInt("width"));
                    rowParkingSpace.setDepth(rs.getInt("depth"));
                    rowParkingSpace.setStatus(ParkingSpace.ParkingStatus.valueOf(rs.getString("status")));
                    myParkingSpaces.add(rowParkingSpace);
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "Parking lots", e.getMessage());
                throw e;
            }
        }
        return myParkingSpaces;
    }

    /**
     * This function will search for an existing space on the parkingLot...update it accordingly
     *  Or it will insert one of none exists yet.
     * @param parkingLotNumber the specific parking lot
     * @param parkingSpace the parking space that got his status changed.
     * @return True if successful, False otherwise.
     */
    public boolean updateParkingSpace(Integer parkingLotNumber, ParkingSpace parkingSpace) throws SQLException{
        ResultSet rs;
        String occupyingOrder = "";
        String query;
        try {
            Statement stmt = db_conn.createStatement();

            if (parkingSpace.getOccupyingOrderID() == null)
            {
                occupyingOrder ="NULL";
            }
            else
            {
                occupyingOrder = parkingSpace.getOccupyingOrderID().toString();
            }
            int result;
            //First we check if this parking space spot is in the database already:
            rs = stmt.executeQuery(String.format("SELECT * FROM ParkingSpace WHERE (idParkingLot = %s AND depth = %s AND width = %s AND height = %s )",
                    parkingLotNumber,
                    parkingSpace.getDepth(),
                    parkingSpace.getWidth(),
                    parkingSpace.getHeight()
            ));
            if (rs.next()){
                //then it is:
                Integer parkingIDNum = rs.getInt("idParkingSpace");
                query = String.format(
                        "UPDATE ParkingSpace SET idOccupyingOrder = %s , status = '%s'" +
                                " WHERE idParkingSpace = %s ",
                        occupyingOrder,
                        parkingSpace.getStatus(),
                        parkingIDNum
                );
            }
            else
            {// this is a new parking space!
                query = String.format("INSERT INTO ParkingSpace " +
                                "(idParkingLot, status, idOccupyingOrder, height, width, depth)" +
                                " VALUES ('%s', '%s', %s, '%s', '%s', '%s')",
                        parkingLotNumber, parkingSpace.getStatus(), occupyingOrder,
                        parkingSpace.getHeight(), parkingSpace.getWidth(), parkingSpace.getDepth()
                );
            }
            result = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            return result == 1;
        } catch (SQLException e)
        {
            System.err.printf("An error occurred during updating the car space of occupying order: %s\n%s ", occupyingOrder, e.getMessage());
            throw e;
        }
    }



    public ArrayList<User> getCustomers() throws SQLException
    {
        return getUserByID(-1, User.UserType.CUSTOMER);
    }

    public ArrayList<User> getEmployees() throws SQLException
    {
        return getUserByID(-1, User.UserType.EMPLOYEE);
    }


    public ArrayList<User> getUserByID(Integer userID, User.UserType userType) throws SQLException{
        ResultSet rs;

        switch (userType){
            case SUPERMAN:
            case CUSTOMER_SERVICE:
            case MANAGER:
            case EMPLOYEE:
                if (userID == -1) {
                    // get all rows of a specific UserType, will call a special query.
                    rs = queryTable("employees");
                }
                else {
                    rs = queryTable("employees", "UID", userID);
                }
                return parseEmployeeFromDB(rs);

            case CUSTOMER :
                if (userID == -1) { // get all rows of a specific UserType, will call a special query.
                    rs = queryUserTable(userType);

                } else { // get a specific user.
                    rs = queryTable("Users", "idUsers", userID);
                }
                return parseCustomerFromDB(rs);
        }
        //default is failure.
        return new ArrayList<>();
    }

    private  ArrayList<User> parseEmployeeFromDB(ResultSet rs) throws SQLException {
        ArrayList<User> myEmployees = new ArrayList<>();
        if (rs == null){
            return myEmployees;
        }
        else {
            try {
                while (rs.next())
                {
                    Employee rowEmployee = new Employee(
                            rs.getInt("UID"),
                            desanitizeFromSQL(rs.getString("name")),
                            desanitizeFromSQL(rs.getString("email")),
                            desanitizeFromSQL(rs.getString("password"))
                    );
                    myEmployees.add(rowEmployee);
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting Employees data from 'Users' table:\n%s", e.getMessage());
                throw e;
            }
        }
        return myEmployees;
    }

    /**
     * This Query gets the list of Users of userType.
     * @param userType
     * @return
     */
    private ResultSet queryUserTable(User.UserType userType) throws SQLException{
        String query = String.format("SELECT * FROM Users");

        if (userType != null) {
            query += String.format(" WHERE userType = '%s'", userType);
        }

        ResultSet result;
        try {
            Statement stmt = db_conn.createStatement();
            result = stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.printf(
                    "An error occurred querying table Users and getting userType: %s\n%s\n",
                    userType, e.getMessage());
            e.printStackTrace();
            throw e;
        }
        return result;
    }

    private ArrayList<User> parseCustomerFromDB(ResultSet rs) throws SQLException {
        ArrayList<User> myCustomers = new ArrayList<>();
        if (rs == null){
            return myCustomers;
        }
        else {
            try {
                while (rs.next())
                {
                    Integer userID = rs.getInt("idUsers");
                    String dateNow = _simpleDateFormatForDb.format(new java.util.Date());
                    ArrayList<Integer> myCars = new ArrayList<>();
                    Statement stmt = db_conn.createStatement();
                    // First we pull the cars of the user.
                    String carListQuery = String.format("SELECT * FROM CarToUser WHERE idUser = %s AND isActive = 1"
                            , userID);
                    ResultSet carList = stmt.executeQuery(carListQuery);
                    // Then make a list of the cars to use in customer c'tor.
                    while (carList.next())
                        myCars.add(carList.getInt("idCars"));

                    Customer rowCustomer = new Customer(
                            rs.getInt("idUsers"),
                            desanitizeFromSQL(rs.getString("userName")),
                            desanitizeFromSQL(rs.getString("password")),
                            desanitizeFromSQL(rs.getString("userEmail")),
                            myCars
                    );

                    // Now we will pull the list of active orders by using the DBs get order by id of user.
                    Map<Integer, Object> myOrders = new HashMap<>();
                    String orderListQuery = String.format("SELECT * FROM Orders WHERE idCustomer = %s AND" +
                                    " orderType != '%s' AND orderType != '%s' ", userID,
                            Order.OrderStatus.DELETED, Order.OrderStatus.FINISHED);
                    ResultSet orderList = stmt.executeQuery(orderListQuery);
                    myOrders = parseOrdersFromDBToMap(orderList);
                    rowCustomer.setActiveOrders(myOrders);

                    // Pull active subscription from DB by user ID
                    Map<Integer, Subscription> mySubscriptions = new HashMap<>();
                    String subsListQuery = String.format("SELECT * FROM SubscriptionToUser WHERE idUser = %s AND endDate > '%s'",
                            userID, dateNow);
                    ResultSet subsList = stmt.executeQuery(subsListQuery);

                    Map<Integer, Subscription> SubscriptionList = parseSubscriptions(subsList); // for readability

                    rowCustomer.setSubscriptionMap(SubscriptionList);

                    // after populating this customer object - add to list
                    myCustomers.add(rowCustomer);
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting Customers data from 'Users' table:\n%s", e.getMessage());
                throw e;
            }
        }
        return myCustomers;
    }

    /**
     * Insert new customer to DB
     * @param customer object to insert
     * @return True if successful, False otherwise.
     */
    public boolean insertCustomer(Customer customer) throws SQLException{
        if (this.isTest){
            customer.setUID(1);
            return true;
        }
        try {
            Statement stmt = db_conn.createStatement();
            int uid;
            stmt.executeUpdate(String.format("INSERT INTO Users (userName, userEmail, password, userType)"
                            + " VALUES ('%s', '%s', '%s', '%s')",
                    sanitizeForSQL(customer.getName()), sanitizeForSQL(customer.getEmail()),
                    sanitizeForSQL(customer.getPassword()), customer.getUserType()),
                    Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                uid = rs.getInt(1); //Get the UID from the DB.
            else
                throw new SQLException("Couldn't get auto-generated UID");

            customer.setUID(uid);
            // insert car list here into DB.
            for (Integer carID : customer.getCarIDList())
            { // For each car in Customer put into it's table.
                stmt.executeUpdate(String.format("INSERT INTO CarToUser (idCars, idUser)"
                                + " VALUES ('%s', '%s')",
                        carID, customer.getUID(),
                        Statement.RETURN_GENERATED_KEYS
                ));
            }
            return true;


        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", customer, e.getMessage());
            throw e;
        }
    }

    /**
     * Insert new car to DB
     * @param customerID customer owns the car
     * @param carID carId inserted
     * @return True if successful, False otherwise.
     */
    public boolean addCarToCustomer(Integer customerID, Integer carID) throws SQLException{

        //TODO: Check if user-car is already inserted and show msg / if not active Set to active.
        try {
            Statement stmt = db_conn.createStatement();
            //int uid;
            stmt.executeUpdate(String.format("INSERT INTO CarToUser (idCars, idUser)"
                            + " VALUES ('%s', '%s')",
                    carID, customerID),
                    Statement.RETURN_GENERATED_KEYS);
            return true;


        } catch (SQLException e) {
            System.err.printf("An error occurred inserting car: %s to customer: %s:\n%s\n", carID, customerID, e.getMessage());
            throw e;
        }
    }

    /**
     * Remove car from user
     * @param customerID customer owns the car
     * @param carID carId removed
     * @return True if successful, False otherwise.
     */
    public boolean removeCarFromCustomer(Integer customerID, Integer carID) throws SQLException{
        try {
            Statement stmt = db_conn.createStatement();
            stmt.executeUpdate(String.format("UPDATE CarToUser SET isActive=0 WHERE  idCars=%s AND idUser=%s",
                    carID, customerID),
                    Statement.RETURN_GENERATED_KEYS);
            return true;


        } catch (SQLException e) {
            System.err.printf("An error occurred inserting car: %s to customer: %s:\n%s\n", carID, customerID, e.getMessage());
            throw e;
        }
    }


    public boolean insertSubscription(Subscription subs) throws SQLException{
        if (this.isTest){
            subs.setSubscriptionID(1);
            return true;
        }
        String params = "idUser, endDate";
        Subscription.SubscriptionType subType = subs.getSubscriptionType();
        String values = String.format("%s, '%s'", subs.getUserID(), _simpleDateFormatForDb.format(subs.getExpiration()));
        switch (subType){
            case FULL:
                params += ", idCar";
                values += String.format(", %s", subs.getCarsID().get(0));
                break;
            case REGULAR_MULTIPLE:
                RegularSubscription rMultSubs = (RegularSubscription) subs;
                params += ", idParkingLot, regularExitTime, subscriptionType";
                values += String.format(", %s, '%s', '%s'" , rMultSubs.getParkingLotNumber(), rMultSubs.getRegularExitTime(), subType);
                break;
            case REGULAR:
                RegularSubscription rSubs = (RegularSubscription) subs;
                params += ", idParkingLot, regularExitTime, subscriptionType, idCar";
                values += String.format(", %s, '%s', '%s', '%s'" ,rSubs.getParkingLotNumber(), rSubs.getRegularExitTime(), subType, subs.getCarsID().get(0));
                break;

            default:
                return false;
        }
        String query = String.format("INSERT INTO SubscriptionToUser (%s) VALUES (%s)", params, values);
        try {
            Statement stmt = db_conn.createStatement();
            int subsId;
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                subsId = rs.getInt(1); //Get the UID from the DB.
            else
                throw new SQLException("Couldn't get auto-generated UID");
            if (subType == Subscription.SubscriptionType.REGULAR_MULTIPLE)
                if (!insertSubscriptionCarsToDB(subsId, subs.getCarsID())) {
                    System.err.printf("An error occurred inserting car list to subscription %s:\n", subs);
                    return false;
                }

            subs.setSubscriptionID(subsId);
            return true;


        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", subs, e.getMessage());
            throw e;
        }
    }

    private boolean insertSubscriptionCarsToDB(Integer idSubscription, ArrayList<Integer> carsID) throws SQLException{
        String query;
        int results;
        try {
            Statement stmt = db_conn.createStatement();
            for (Integer carID : carsID) {
                query = String.format("INSERT INTO CarToSubscription (idSubscription, idCar) VALUES (%s, %s)", idSubscription, carID);
                results = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
                if (results != 1)
                    return false;
            }
        }catch (SQLException e){
            System.err.printf("An error occurred inserting carList for subscription %s\n%s", idSubscription, e.getMessage());
            throw e;
        }

        return true;
    }


    /**
     * Get all Subscriptions from DB
     * @return hash map of subscriptionId and subscriptions
     */
    public Map<Integer, Subscription> getAllSubscriptions() throws SQLException {
        ResultSet rs = queryTable("SubscriptionToUser");
        Map<Integer, Subscription> mySubscriptions = parseSubscriptions(rs);
        return mySubscriptions;
    }

    /**
     * parses ResultSet of subscriptions into hash map
     * @param rs Result set with Subscription DATA
     * @return hash map of subscriptionId and subscriptions
     */
    public Map<Integer, Subscription> parseSubscriptions(ResultSet rs) throws SQLException {
        Map<Integer, Subscription> mySubscriptions = new HashMap<>();
        if (rs == null){
            return mySubscriptions;
        }
        else {
            try {
                while (rs.next()) {
                    ArrayList<Integer> myCarList = new ArrayList<>();
                    Subscription rowSubscription;
                    switch (rs.getString("subscriptionType")){
                        case "REGULAR":
                            myCarList.add(rs.getInt("idCar"));
                            rowSubscription = new RegularSubscription(rs.getInt("idSubscription"),
                                    myCarList,
                                    rs.getInt("idUser"),
                                    rs.getInt("idParkingLot"),
                                    rs.getDate("endDate"),
                                    rs.getString("regularExitTime"));
                            break;
                        case "REGULAR_MULTIPLE":
                            myCarList.addAll(getCarsForSubscriptionFromDB(rs.getInt("idSubscription")));
                            rowSubscription = new RegularSubscription(rs.getInt("idSubscription"),
                                    myCarList,
                                    rs.getInt("idUser"),
                                    rs.getInt("idParkingLot"),
                                    rs.getDate("endDate"),
                                    rs.getString("regularExitTime"));
                            break;
                        case "FULL":
                            myCarList.add(rs.getInt("idCar"));
                            rowSubscription = new FullSubscription(rs.getInt("idSubscription"),
                                    myCarList,
                                    rs.getInt("idUser"),
                                    rs.getDate("endDate"));
                            break;
                        default:
                            return mySubscriptions;
                    }
                    mySubscriptions.put(rowSubscription.getSubscriptionID(), rowSubscription);
                }
                return  mySubscriptions;
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "SubscriptionToUser", e.getMessage());
                throw e;
            }
        }
    }

    private ArrayList<Integer> getCarsForSubscriptionFromDB(Integer idSubscription) throws SQLException{
        ResultSet rs;
        ArrayList<Integer> carList = new ArrayList<>();
        rs = queryTable("CarToSubscription", "idSubscription", idSubscription.toString(), "isActiveInSubscription", "1");
        while (rs.next()){
            carList.add(rs.getInt("idCar"));
        }
        return carList;
    }


    /**
     * extend subscription in 28 days. update regular exit time if applicable.
     * @param renewedSubs
     * @return True if successful, false otherwise.
     */
    public boolean renewSubscription(Subscription renewedSubs) throws SQLException{
        if (this.isTest){
            return true;
        }
        try {
            Statement stmt = db_conn.createStatement();
            String query;
            if (renewedSubs.getSubscriptionType() == Subscription.SubscriptionType.REGULAR)
                query = String.format("UPDATE SubscriptionToUser SET endDate=ADDDATE(endDate, 28), regularExitTime = '%s' WHERE  idSubscription = %s",
                        ((RegularSubscription)renewedSubs).getRegularExitTime(), renewedSubs.getSubscriptionID());
            else
                query = String.format("UPDATE SubscriptionToUser SET endDate=ADDDATE(endDate, 28) WHERE  idSubscription = %s",
                        renewedSubs.getSubscriptionID());
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            return true;

        } catch (SQLException e) {
            System.err.printf("An error occurred renewing subscription: %s\n%s", renewedSubs.getSubscriptionID(), e.getMessage());
            throw e;
        }
    }

    private Order.OrderStatus parseOrderStatus(String str) throws SQLException{
        Order.OrderStatus ret;
        switch (str){
            case "PRE_ORDER":
                ret = Order.OrderStatus.PRE_ORDER;
                break;
            case "IN_PROGRESS":
                ret = Order.OrderStatus.IN_PROGRESS;
                break;
            case "FINISHED":
                ret = Order.OrderStatus.FINISHED;
                break;
            case "DELETED":
                ret = Order.OrderStatus.DELETED;
                break;
            default:
                return null;
        }
        return ret;
    }


    //region Complaints
    /**
     * Complaints
     */
    /**
     * Insert new complaint to DB
     * @param complaint object to insert
     * @return True if successful, False otherwise.
     */
    public boolean insertComplaint(Complaint complaint) throws SQLException{
        try {
            Statement stmt = db_conn.createStatement();
            int complaintId;
            String orderID = valueOrNull(complaint.getRelatedOrderID());
            String parkingLotID = valueOrNull(complaint.getParkingLotNumber());
            stmt.executeUpdate(String.format("INSERT INTO Complaints (idUser, idOrder," +
                            " status, description, refund, idParkingLot)" +
                            " VALUES ('%s', %s," +
                            "'%s', '%s', '%s', %s)",
                    complaint.getCustomerID(), orderID,
                    complaint.getStatus(), sanitizeForSQL(complaint.getDescription()), complaint.getRefund(), complaint.getParkingLotNumber()),
                    Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                complaintId = rs.getInt(1); //Get the UID from the DB.
            else
                throw new SQLException("Couldn't get auto-generated complaintID");

            complaint.setComplaintID(complaintId);
            return true;

        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", complaint, e.getMessage());
            throw e;
        }
    }
    /**
     * Update  complaint in DB
     * @param complaint object to insert
     * @return True if successful, False otherwise.
     */
    public boolean updateComplaint(Complaint complaint) throws SQLException{
        if (complaint.getComplaintID() == -1){
            System.err.printf("Can't update complaint without complaintID.\nComplaint: %s\n", complaint);
            return false;
        }
        try {
            Statement stmt = db_conn.createStatement();
            String parkingLotID = valueOrNull(complaint.getParkingLotNumber());
            stmt.executeUpdate(String.format("UPDATE Complaints SET idUser = '%s', idOrder =%s," +
                            " status = '%s', description = '%s', refund = '%s', idParkingLot=%s" +
                            "WHERE idComplaints = %s",
                    complaint.getCustomerID(), valueOrNull(complaint.getRelatedOrderID()),
                    complaint.getStatus(), sanitizeForSQL(complaint.getDescription()), complaint.getRefund(), parkingLotID, complaint.getComplaintID()),
                    Statement.RETURN_GENERATED_KEYS);
            return true;

        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", complaint, e.getMessage());
            throw e;
        }
    }

    /**
     * get Complaints from DB (all Complaints)
     * @return all Complaints in map
     */
    public Map<Integer, Object> getAllComplaints() throws SQLException{
        return getComplaintsByID(-1);
    }

    /**
     * get Complaints from DB
     * @param complaintId specific order identifier, '-1' for all orders
     * @return specific / all orders , null if error
     */
    public Map<Integer, Object> getComplaintsByID(int complaintId) throws SQLException{
        ResultSet rs;

        if (complaintId == -1) { // get all rows
            rs = queryTable("Complaints");

        } else { // get specific order
            rs = queryTable("Complaints", "idComplaints", complaintId);
        }

        return parseComplaintsFromDBToMap(rs);
    }


    public Map<Integer, Object> parseComplaintsFromDBToMap(ResultSet rs) throws SQLException{
        Map<Integer, Object>  myComplaints = new HashMap<>();
        if (rs == null){
            return myComplaints;
        }
        else {
            try {
                while (rs.next()) {
                    Complaint.ComplaintStatus _complaintStatus = parseComplaintStatus(rs.getString("status"));
                    if (null == _complaintStatus){
                        System.err.printf("Error occurred getting complaintStatus from table \"%s\"", "Complaints");
                        return myComplaints;
                    }
                    Complaint rowComplaint = new Complaint(
                            rs.getInt("idComplaints"),
                            rs.getInt("idUser"),
                            rs.getInt("idOrder"),
                            _complaintStatus,
                            desanitizeFromSQL(rs.getString("description")),
                            rs.getDouble("refund"),
                            rs.getInt("idParkingLot")
                    );
                    myComplaints.put(rs.getInt("idComplaints"), rowComplaint);
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "Complaints", e.getMessage());
                throw e;
            }
        }
        return myComplaints;
    }

    /**
     * Cancelling a complaint by setting its state to canceled
     * @param complaintID
     * @return True for success, false otherwise.
     */
    public boolean cancelComplaint(Integer complaintID) throws SQLException{
        try {
            Statement stmt = db_conn.createStatement();
            stmt.executeUpdate(String.format("UPDATE Complaints SET status = '%s'" +
                            "WHERE idComplaints = '%s'",
                    Complaint.ComplaintStatus.CANCELLED, complaintID),
                    Statement.RETURN_GENERATED_KEYS);
            return true;

        } catch (SQLException e) {
            System.err.printf("An error occurred cancelling complaint with id %s:\n%s\n", complaintID, e.getMessage());
            throw e;
        }
    }

    private Complaint.ComplaintStatus parseComplaintStatus(String status) throws SQLException{
        Complaint.ComplaintStatus ret;
        switch (status) {
            case "OPEN":
                ret = Complaint.ComplaintStatus.OPEN;
                break;
            case "ACCEPTED":
                ret = Complaint.ComplaintStatus.ACCEPTED;
                break;
            case "REJECTED":
                ret = Complaint.ComplaintStatus.REJECTED;
                break;
            case "CANCELLED":
                ret = Complaint.ComplaintStatus.CANCELLED;
                break;
            default:
                return null;
        }
        return ret;
    }
    //endregion


    //region Reports

    //Making Reports Section:
    /**
     * WIP will add one report at a time prob so we can test it with Rami.
     * @param reportType the type of report to be returned to the Manager/Tasker.
     * @return the report. (null if wrong type or not implementd yet.
     */
    public String makeReportFromDB(Report.ReportType reportType, Integer managerID, Integer parkingLotID) throws SQLException
    {
        String manager = managerID == 999 ? "Created by CPS's Tasker:" : "Created by Manager:";
        String reportToReturn ="\n\n\t\t\t\t";
        switch(reportType)
        {
            case DAILY_FINISHED_ORDERS:
            {
                reportToReturn += "Daily finished orders report of parking lot number: " + parkingLotID + ", "
                        + manager + ":\n" + makeOrdersReport(reportType, Order.OrderStatus.FINISHED, 1, parkingLotID);
                return reportToReturn;
            }
            case DAILY_CANCELED_ORDERS:
            {
                reportToReturn += "Daily canceled orders report of parking lot number: " + parkingLotID + ", "
                        + manager + "\n" + makeOrdersReport(reportType, Order.OrderStatus.DELETED, 1, parkingLotID);
                return reportToReturn;
            }
            case DAILY_LATED_ORDERS:
            {
                reportToReturn += "Daily late customer's orders report of parking lot number: " + parkingLotID + ", "
                        + manager + "\n" + makeOrdersReport(reportType, null, 1, parkingLotID);
                return reportToReturn;
            }

            case QUARTERLY_ORDERS:
            {
                reportToReturn += "Quarterly orders report of parking lot number: " + parkingLotID + ", "
                        + manager + "\n" + makeQuarterlyOrdersReport(parkingLotID);
                return reportToReturn;
            }
            default:
                throw new NotImplementedException();
        }
    }

    private String makeQuarterlyOrdersReport(Integer parkingLotID) throws SQLException {
        Integer countRows = 0;
        String rowLine = "|___________________________________________________"
                + "___________________________________________________"
                + "____________________________________________________|";;
        String columns = "|OrderID | customer ID | status | car ID | price | parking lot number |"
                + " | actual entry time | actual exit time | estimated entry time | estimated exit time|";
        StringBuilder report = new StringBuilder(
                " _________________________________________________________________________________________________________"
                        +"_________________________________________________"
                        +"\n").append(columns)
                ;

        double daysInOneQuarter = 91.25;
        ResultSet rs;

        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM Orders WHERE idParkingLot=").append(parkingLotID)
                .append(" AND clientType='");
        //First we grab the list of the relevant price type.
        //Then we check if it validates with this Quarter if so we add it to the report.
        //Adding one-time-parking.
        report.append("\n").append(rowLine).append("\n")
                .append("|\t\t\t\tOne Time Parking Clients:\n")
                .append(columns);
        rs = callStatement(String.valueOf(queryBuilder
                        + parsePriceTypeToColumnName(Billing.priceList.ONE_TIME_PARKING) + "'")
                , "Orders"
        );
        Collection<Object> objArray = parseOrdersFromDBToMap(rs).values();
        countRows = addRowsToQuarterlyReport(countRows, rowLine, report, daysInOneQuarter, objArray);
        //Adding pre-orders:

        report.append("\n").append(rowLine).append("\n")
                .append("|\t\t\t\tPre Order One Time Parking Clients:\n").append(columns);
        rs = callStatement(String.valueOf(queryBuilder
                        + parsePriceTypeToColumnName(Billing.priceList.PRE_ORDER_ONE_TIME_PARKING) + "'")
                , "Orders"
        );
        objArray = parseOrdersFromDBToMap(rs).values();
        countRows = addRowsToQuarterlyReport(countRows, rowLine, report, daysInOneQuarter, objArray);

        //Adding Subscriptions:
        report.append("\n").append(rowLine).append("\n")
                .append("|\t\t\t\tSubscriptions Parking Clients:\n")
                .append(columns);
        rs = callStatement(String.valueOf(queryBuilder
                        + parsePriceTypeToColumnName(Billing.priceList.NO_CHARGE_DUE_TO_SUBSCRIPTION) + "'")
                , "Orders"
        );
        objArray = parseOrdersFromDBToMap(rs).values();
        countRows = addRowsToQuarterlyReport(countRows, rowLine, report, daysInOneQuarter, objArray);

        report.append("\n").append(rowLine);

        return String.valueOf(report + "\n\t\t" + "Total of " + countRows + " Rows.");
    }

    private Integer addRowsToQuarterlyReport(Integer countRows, String rowLine, StringBuilder report, double daysInOneQuarter, Collection<Object> objArray) throws SQLException {
        for (Object orderObj : objArray)
        {
            Order order = (Order) orderObj;
            if (TimeUtils.timeDifference(order.getActualEntryTime(), new Date(),
                    TimeUtils.Units.DAYS) <= daysInOneQuarter)

            {
                countRows++;
                report.append(addOrderRow(order, rowLine));
            }

        }
        return countRows;
    }

    /**
     * Generic Report maker from Order's table.
     *
     * @param reportType
     * @param dayToValidate - The amount of days from the actualy entry to today this report is valid.
     * @param parkingLotID
     * @return the wanted report.
     */
    private String makeOrdersReport(Report.ReportType reportType, Order.OrderStatus orderType,
                                    Integer dayToValidate, Integer parkingLotID) throws SQLException
    {
        ResultSet rs;
        String rowLine = "|___________________________________________________"
                + "___________________________________________________"
                + "____________________________________________________|";;
        StringBuilder report = new StringBuilder(
                " _________________________________________________________________________________________________________"
                        +"_________________________________________________"
                        +"\n"
                        +"|OrderID | customer ID | status | car ID | price | parking lot number |"
                        + " | actual entry time | actual exit time | estimated entry time | estimated exit time|");
        ;

        //First we get the Orders OF TYPE ORDER-TYPE from the DB.
        if (orderType != null)
        {
            rs = queryTable("Orders", "orderType", orderType.toString());
        }
        else
        {//Then we are checking late Cars.
            rs = queryTable("Orders");
        }
        //Then we start going through the orders and decided if this order should be inserted into the Report,
        // according to the dayToValidate.
        Collection<Object> objArray = parseOrdersFromDBToMap(rs).values();
        Integer countRows = 0;
        for (Object orderObj : objArray)
        {
            Order order = (Order) orderObj;
            if (orderType == null)
            { //Then we are just checking for the cars who got late!
                if ((TimeUtils.timeDifference(order.getActualEntryTime(), new Date(),
                        TimeUtils.Units.DAYS) <= dayToValidate) &&
                        (TimeUtils.timeDifference(order.getActualEntryTime(), order.getEstimatedEntryTime(),
                                TimeUtils.Units.MINUTES) >= 30))
                {
                    countRows++;
                    report.append(addOrderRow(order, rowLine));
                }
            }
            else
            {
                if ((TimeUtils.timeDifference(order.getActualEntryTime(), new Date(),
                        TimeUtils.Units.DAYS)) <= dayToValidate)
                {//Then this Order fits the description...we will add it! :
                    countRows++;
                    report.append(addOrderRow(order, rowLine));
                }
            }
        }
        report.append("\n")
                .append(rowLine)
        ;
        //Adding this report count to the general count table on the DB
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1 ;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        updateDailyReportToDB(day, month, year, reportType, parkingLotID, countRows);

        // Adding another flair!
        return String.valueOf(report + "\n\t\t" + "Total of " + countRows + " Rows.");
    }

    private String addOrderRow(Order order, String rowLine) throws SQLException
    {
        Order.OrderStatus status = order.getOrderStatus();
        StringBuilder orderRow = new StringBuilder();
        return String.valueOf(orderRow
                .append("\n")
                .append(rowLine)
                .append(" \n| ").append(order.getOrderID())
                .append(" | ").append(order.getCostumerID())
                .append(" | ").append(status.toString())
                .append(" | ").append(order.getCarID().toString())
                .append(" | ").append(order.getPrice())
                .append(" | ").append(order.getParkingLotNumber())
                .append(" | ").append(makeSimpleDateOrNull(order.getActualEntryTime()))
                .append(" | ").append(makeSimpleDateOrNull(order.getActualExitTime()))
                .append(" | ").append(makeSimpleDateOrNull(order.getEstimatedEntryTime()))
                .append(" | ").append(makeSimpleDateOrNull(order.getEstimatedExitTime()))
                .append(" | ")
        );
    }

    /**
     * used only above^
     * @param dateToCheck
     * @return
     */
    private String makeSimpleDateOrNull(Date dateToCheck)
    {
        if (dateToCheck == null) return "N/A";
        return _simpleDateFormatForDb.format(dateToCheck);
    }


    /**
     * REPORTS - DB Stuff
     */
    /**
     * update or insert report for a specific date and parking lot
     * @param day
     * @param month
     * @param year
     * @param reportType
     * @param parkingLotID
     * @param count
     * @return
     */
    private boolean updateDailyReportToDB(Integer day, Integer month, Integer year, Report.ReportType reportType, Integer parkingLotID, Integer count) throws SQLException{

        // set reportType column
        String reportCol = parseReportTypeToColumnName(reportType);
        if (reportCol == null) {
            System.err.printf("An error occurred during updating the daily report of: %s%s%s ", day, month, year);
            return false;
        }

        ResultSet rs;
        try {
            Statement stmt = db_conn.createStatement();
            String condition = String.format(" WHERE day = %s AND month = %s AND year = %s AND parkingLotID = %s",
                    day, month, year, parkingLotID);
            String query = "SELECT * FROM DailyReports";
            rs = stmt.executeQuery(query+condition);
            if(rs.next()){ // entry available
                query = String.format("UPDATE DailyReports SET %s = %s", reportCol, count);
                query += condition;
            }else{ // new entry
                query = String.format("INSERT INTO DailyReports (day, month, year, parkingLotId, %s) " +
                                "VALUES (%s, %s, %s, %s, %s)",
                        reportCol, day, month, year, parkingLotID, count);
            }
            int result = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            return result == 1;
        }catch (SQLException e)
        {
            System.err.printf("An error occurred during updating the daily report of: %s/%s/%s/\n%s", day, month, year, e.getMessage());
            throw e;
        }
    }

    /**
     *
     * @param day
     * @param month
     * @param year
     * @return
     */
    //TODO: thats just a starter func
    private Map<Integer, Object> getDailyReportFromDbByDate(Integer day, Integer month, Integer year) throws SQLException {
        Map<Integer, Object> map = new HashMap<>();
        ResultSet rs;
        try {
            Statement stmt = db_conn.createStatement();
            String condition = String.format(" WHERE day = %s AND month = %s AND year = %s",
                    day, month, year);
            String query = "SELECT * FROM DailyReports";
            rs = stmt.executeQuery(query + condition);
            while (rs.next()) { // entries available
                map.put(rs.getInt("parkingLotID"), null);
            }

            return map;

        }catch(SQLException e) {
            System.err.printf("An error occurred during querying the daily report of: %s%s%s\n%s", day, month, year, e.getMessage());
            throw e;
        }
    }

    /**
     * translated ReportType enum to DB column name
     * @param reportType ReportType to check
     * @return db column name
     */
    private String parseReportTypeToColumnName (Report.ReportType reportType){
        switch (reportType){
            case DAILY_CANCELED_ORDERS:
                return "numberOfCancelledOrders";
            case DAILY_FINISHED_ORDERS:
                return "numberOfCompletedOrders";
            case DAILY_LATED_ORDERS:
                return "numberOfLateEntranceOrders";
            default:
                return null;
        }
    }

    /**
     * Helps parsing nullable ints from database.
     * @param rs ResultSet to parse
     * @param columnName Column Name to search for
     * @return The integer - with value of null
     * @throws SQLException If SQL exception obviously.
     */
    private Integer getIntegerNullableFromRS(ResultSet rs, String columnName) throws SQLException {
        Integer resultInt = rs.getInt(columnName);
        if (rs.wasNull())
            resultInt = null;
        return resultInt;
    }

    /**
     * Checks if the value of an Integer is null (or -1) and sets it to "NULL" for SQL injections
     * @param number The Integer to check
     * @return "NULL" if null, string of value otherwise
     */
    private String valueOrNull(Integer number)
    {
        if (number == null || number.equals(-1))
        {
            return "NULL";
        }
        else
        {
            return "'" + number.toString() + "'";
        }
    }


    /**
     * translated PriceList of inserted orders to DB clientType value
     * @param priceType priceType to check
     * @return clientType column's value
     */
    private String parsePriceTypeToColumnName (Billing.priceList priceType)
    {
        switch (priceType)
        {
            case ONE_TIME_PARKING:
                return "OneTimeParking";
            case PRE_ORDER_ONE_TIME_PARKING:
                return "PreOrderOneTimeParking";
            case NO_CHARGE_DUE_TO_SUBSCRIPTION:
                return "SubscriptionParking";
            default:
                return null;
        }
    }
    //endregion

    //region car parked stuff
    public boolean setCarAsParked(Integer carID, Integer orderID, Integer parkingLotID) throws SQLException{
        try {
            Statement stmt = db_conn.createStatement();
            String query = String.format("INSERT INTO ParkedCars (idParkedCar, idOrder, idParkingLot) " +
                    "VALUES (%s, %s, %s)", carID, orderID, parkingLotID);
            int res = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
            return res == 1;
        }catch (SQLException e){
            System.err.println("failed to mark car with id " + carID +" as parked");
            throw e;
        }
    }

    public ArrayList<Integer> getAllParkedCars() throws SQLException{
        ArrayList<Integer> parkedCarList = new ArrayList<>();
        try {
            Statement stmt = db_conn.createStatement();
            String query = "SELECT idParkedCar FROM ParkedCars";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()){
                parkedCarList.add(rs.getInt("idParkedCar"));
            }
        }catch (SQLException e){
            System.err.println("failed to get all parked cars");
            throw e;
        }
        return parkedCarList;

    }

    /**
     * removes the actual parked car from DB
     * @param carID
     * @return
     * @throws SQLException
     */
    public boolean unsetCarAsParked(Integer carID)throws SQLException{
        try {
            Statement stmt = db_conn.createStatement();
            String query = String.format("DELETE FROM ParkedCars WHERE idParkedCar = %s", carID);
            int res = stmt.executeUpdate(query);
            return res == 1;
        }catch (SQLException e){
            System.err.println("failed to mark car with id " + carID +" as parked");
            throw e;
        }
    }
    //endregion

}