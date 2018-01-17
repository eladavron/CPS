package controller;

import entity.*;
import utils.StringUtils;
import utils.TimeUtils;

import java.sql.*;
import java.util.*;
import java.util.Date;

import static utils.StringUtils.desanitizeFromSQL;
import static utils.StringUtils.sanitizeForSQL;

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
     * @throws SQLException in case of sql error
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
            return "Error occurred!\nSee server output for details.";
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
    private ResultSet queryTable(String tableName)
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
    private ResultSet queryTable(String tableName, String field, int value) {
        String query = String.format("SELECT * FROM %s", tableName);

        if ((field != null) && (value != 0)) {
            query += String.format(" WHERE %s = %s", field, value);
        }

        return callStatement(query, tableName);
    }

    /*
        Overloading function of the function above with field of String type.
     */
    private ResultSet queryTable(String tableName, String field, String value) {
        String query = String.format("SELECT * FROM %s", tableName);

        if ((field != null) && (!value.equals(""))) {
            query += String.format(" WHERE %s = '%s'", field, value);
        }

        return  callStatement(query, tableName);
    }

    /**
     * private calling Statement of query (prevent code duplication.
     * @param query
     * @return
     */
    private ResultSet callStatement(String query, String tableName)
    {
        ResultSet result;
        try {
            Statement stmt = db_conn.createStatement();
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
            return false;
        }
    }

    /**
     * Insert new order / preOrder to DB
     * @param order order object to insert
     * @return True if successful, False otherwise.
     */
    public boolean insertOrder(Order order){
        if (this.isTest){
            order.setOrderID(1);
            return true;
        }
        try {
            Statement stmt = db_conn.createStatement();
            Date creationDate;
            int uid = -1;
            String _actualExitTime = (order.getActualExitTime() == null)
                    ? "NULL"
                    :  "'" + _simpleDateFormatForDb.format(order.getActualExitTime()) + "'";
            stmt.executeUpdate(String.format("INSERT INTO Orders (idCar, idCustomer, idParkingLot, orderType," +
                            " entryTimeEstimated, entryTimeActual, exitTimeEstimated, exitTimeActual, price)" +
                            " VALUES ('%s', '%s', '%s', '%s', '%s', '%s', '%s', %s, '%s')",
                    order.getCarID(), order.getCostumerID(), order.getParkingLotNumber(),
                    order.getOrderStatus(),
                    _simpleDateFormatForDb.format(order.getEstimatedEntryTime()),
                    _simpleDateFormatForDb.format(order.getActualEntryTime()),
                    _simpleDateFormatForDb.format(order.getEstimatedExitTime()),
                    _actualExitTime, order.getPrice()),
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
            return false;
        }
    }

    /**
     * "Delete" order by setting its type to 'DELETED'
     * @param orderId
     * @return True upon success, false otherwise
     */
    public boolean deleteOrder (int orderId, double charged)
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
            return false;
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


    public Map<Integer, Object> parseOrdersFromDBToMap(ResultSet rs){
        Map<Integer, Object>  myOrders = new HashMap<>();
        if (rs == null){
            return null;
        }
        else {
            try {
                while (rs.next()) {
                    Order.OrderStatus _orderStatus = parseOrderStatus(rs.getString("OrderType"));
                    if (null == _orderStatus){
                        System.err.printf("Error occurred getting OrderStatus from table \"%s\"", "Orders");
                        return null;
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
                            myOrders.put(activeOrder.getOrderID(), activeOrder);
                            break;
                    }
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "Orders", e.getMessage());
                return null;
            }
        }
        return myOrders;
    }

    /**
     * get orders from DB (all orders)
     * @return all orders in list
     */
    public Map<Integer, Object> getAllOrders() {
        return getOrdersByID(-1);
    }

    /**
     * get orders from DB
     * @param orderId specific order identifier, '-1' for all orders
     * @return specific / all orders , null if error
     */
    public Map<Integer, Object> getOrdersByID(int orderId) {
        ResultSet rs;

        if (orderId == -1) { // get all rows
            rs = queryTable("Orders");

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
    public Map<Integer, Object> getOrdersByUserID(int userID) {
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
    public String listTables()
    {
        String returnString = "Available tables:\n";
        for (String tableName : listTables) {
            returnString += "\t\t" + tableName + "\n";
        }
        return returnString;
    }

    public ArrayList<Object> getParkingLots() {
        return  getParkingLotsByID(-1);
    }


    public ArrayList<Object> getParkingLotsByID(Integer parkingLotID) {
        ResultSet rs;

        if (parkingLotID == -1) { // get all rows
            rs = queryTable("ParkingLots");

        } else { // get specific order
            rs = queryTable("ParkingLots", "idParkingLots", parkingLotID);
        }

        return parseParkingLotsFromDB(rs);

    }

    private ArrayList<Object> parseParkingLotsFromDB(ResultSet rs) {
        ArrayList<Object> myLots = new ArrayList<>();
        if (rs == null){
            return null;
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
                return null;
            }
        }
        return myLots;
    }

    /**
     * Returns all the Knowen! parkingSpaces of a parking lot (number)
     * @param parkingLotNumber
     * @return empty if none found, null on exception.
     */
    public ArrayList<ParkingSpace> getParkingSpaces(Integer parkingLotNumber){
        ResultSet rs = queryTable("ParkingSpace", "idParkingLot", parkingLotNumber);
        return parseParkingSpacesFromDB(rs);
    }

    private ArrayList<ParkingSpace> parseParkingSpacesFromDB(ResultSet rs) {
        ArrayList<ParkingSpace> myParkingSpaces = new ArrayList<>();
        if (rs == null){
            return null;
        }
        else {
            try {
                while (rs.next()) {
                    ParkingSpace rowParkingSpace = new ParkingSpace(
                            rs.getInt("idOccupyingOrder")
                    );
                    rowParkingSpace.setHeight(rs.getInt("height"));
                    rowParkingSpace.setWidth(rs.getInt("width"));
                    rowParkingSpace.setDepth(rs.getInt("depth"));
                    rowParkingSpace.setStatus(ParkingSpace.ParkingStatus.valueOf(rs.getString("status")));
                    myParkingSpaces.add(rowParkingSpace);
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "Parking lots", e.getMessage());
                return null;
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
    public boolean updateParkingSpace(Integer parkingLotNumber, ParkingSpace parkingSpace){
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
            System.err.printf("An error occurred inserting during updating the car space of occupying order: %s ", occupyingOrder, e.getMessage());
            return false;
        }
    }



   public ArrayList<User> getCustomers()
   {
       return getUserByID(-1, User.UserType.CUSTOMER);
   }

    public ArrayList<User> getEmployees()
    {
        return getUserByID(-1, User.UserType.EMPLOYEE);
    }


    public ArrayList<User> getUserByID(Integer userID, User.UserType userType) {
        ResultSet rs;

        switch (userType){
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
        return null;
    }

    private  ArrayList<User> parseEmployeeFromDB(ResultSet rs) {
        ArrayList<User> myEmployees = new ArrayList<>();
        if (rs == null){
            return null;
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
                return null;
            }
        }
        return myEmployees;
    }

    /**
     * This Query gets the list of Users of userType.
     * @param userType
     * @return
     */
    private ResultSet queryUserTable(User.UserType userType) {
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
            return null;
        }
        return result;
    }

    private ArrayList<User> parseCustomerFromDB(ResultSet rs) {
        ArrayList<User> myCustomers = new ArrayList<>();
        if (rs == null){
            return null;
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
                return null;
            }
        }
        return myCustomers;
    }

    /**
     * Insert new customer to DB
     * @param customer object to insert
     * @return True if successful, False otherwise.
     */
    public boolean insertCustomer(Customer customer){
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
            return false;
        }
    }

    /**
     * Insert new car to DB
     * @param customerID customer owns the car
     * @param carID carId inserted
     * @return True if successful, False otherwise.
     */
    public boolean addCarToCustomer(Integer customerID, Integer carID){

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
            return false;
        }
    }

    /**
     * Remove car from user
     * @param customerID customer owns the car
     * @param carID carId removed
     * @return True if successful, False otherwise.
     */
    public boolean removeCarFromCustomer(Integer customerID, Integer carID){
        try {
            Statement stmt = db_conn.createStatement();
            stmt.executeUpdate(String.format("UPDATE CarToUser SET isActive=0 WHERE  idCars=%s AND idUser=%s",
                    carID, customerID),
                    Statement.RETURN_GENERATED_KEYS);
            return true;


        } catch (SQLException e) {
            System.err.printf("An error occurred inserting car: %s to customer: %s:\n%s\n", carID, customerID, e.getMessage());
            return false;
        }
    }


    public boolean insertSubscription(Subscription subs){
        String params = "idUser, idCar, endDate";
        Subscription.SubscriptionType subType = subs.getSubscriptionType();
        String values = String.format("%s, %s, '%s'", subs.getUserID(), subs.getCarID(), _simpleDateFormatForDb.format(subs.getExpiration()));
        switch (subType){
            case FULL:
                break;
            case REGULAR_MULTIPLE:
            case REGULAR:
                RegularSubscription rSubs = (RegularSubscription) subs;
                params += ", idParkingLot, regularExitTime, subscriptionType";
                values += String.format(", %s, '%s', '%s'" ,rSubs.getParkingLotNumber(), rSubs.getRegularExitTime(), subType);
                break;

            default:
                return false;
        }
        String query = String.format("INSERT INTO SubscriptionToUser (%s) VALUES (%s)", params, values);
        try {
            Statement stmt = db_conn.createStatement();
            int subsId;
            //stmt.executeQuery(query);
            stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                subsId = rs.getInt(1); //Get the UID from the DB.
            else
                throw new SQLException("Couldn't get auto-generated UID");

            subs.setSubscriptionID(subsId);
            return true;


        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", subs, e.getMessage());
            return false;
        }
    }


    /**
     * Get all Subscriptions from DB
     * @return hash map of subscriptionId and subscriptions
     */
    public Map<Integer, Subscription> getAllSubscriptions(){
        ResultSet rs = queryTable("SubscriptionToUser");
        Map<Integer, Subscription> mySubscriptions = parseSubscriptions(rs);
        return mySubscriptions;
    }

    /**
     * parses ResultSet of subscriptions into hash map
     * @param rs Result set with Subscription DATA
     * @return hash map of subscriptionId and subscriptions
     */
    public Map<Integer, Subscription> parseSubscriptions(ResultSet rs) {
        Map<Integer, Subscription> mySubscriptions = new HashMap<>();
        if (rs == null){
            return null;
        }
        else {
            try {
                while (rs.next()) {
                    Subscription rowSubscription;
                    switch (rs.getString("subscriptionType")){
                        case "REGULAR":
                            rowSubscription = new RegularSubscription(rs.getInt("idSubscription"),
                                    rs.getInt("idCar"),
                                    rs.getInt("idUser"),
                                    rs.getInt("idParkingLot"),
                                    rs.getDate("endDate"),
                                    rs.getString("regularExitTime"));
                            break;

                        case "FULL":
                            rowSubscription = new FullSubscription(rs.getInt("idSubscription"),
                                    rs.getInt("idCar"),
                                    rs.getInt("idUser"),
                                    rs.getDate("endDate"));
                            break;
                        default:
                            return null;

                    }
                    mySubscriptions.put(rowSubscription.getSubscriptionID(), rowSubscription);
                }
                return  mySubscriptions;
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "SubscriptionToUser", e.getMessage());
                return null;
            }
        }
    }



    /**
     * extend subscription in 28 days. update regular exit time if applicable.
     * @param renewedSubs
     * @return True if successful, false otherwise.
     */
    public boolean renewSubscription(Subscription renewedSubs){
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
            System.err.printf("An error occurred renewing subscription: %s\n", renewedSubs.getSubscriptionID(), e.getMessage());
            return false;
        }
    }

    private Order.OrderStatus parseOrderStatus(String str){
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


    /**
     * Complaints
     */
    /**
     * Insert new complaint to DB
     * @param complaint object to insert
     * @return True if successful, False otherwise.
     */
    public boolean insertComplaint(Complaint complaint){
        try {
            Statement stmt = db_conn.createStatement();
            int complaintId;
            String representativeID = (complaint.getCustomerServiceRepresentativeID().equals(-1) ? "NULL" :
                    complaint.getCustomerServiceRepresentativeID().toString());
            String orderID = (complaint.getRelatedOrderID().equals(-1) ? "NULL" :
                    "'" + complaint.getRelatedOrderID().toString() + "'");
            stmt.executeUpdate(String.format("INSERT INTO Complaints (idUser, idOrder, idRepresentative," +
                            " status, description, refund)" +
                            " VALUES ('%s', %s, %s," +
                            "'%s', '%s', '%s')",
                    complaint.getCustomerID(), orderID, representativeID,
                    complaint.getStatus(), sanitizeForSQL(complaint.getDescription()), complaint.getRefund()),
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
            return false;
        }
    }
    /**
     * Update  complaint in DB
     * @param complaint object to insert
     * @return True if successful, False otherwise.
     */
    public boolean updateComplaint(Complaint complaint){
        if (complaint.getComplaintID() == -1){
            System.err.printf("Can't update complaint without complaintID.\nComplaint: %s\n", complaint);
            return false;
        }
        try {
            Statement stmt = db_conn.createStatement();
            String representativeID = (complaint.getCustomerServiceRepresentativeID() == -1) ? "NULL" :
                    complaint.getCustomerServiceRepresentativeID().toString();
            stmt.executeUpdate(String.format("UPDATE Complaints SET idUser = '%s', idOrder ='%s', idRepresentative = %s," +
                            " status = '%s', description = '%s', refund = '%s'" +
                            "WHERE idComplaints = '%s'",
                    complaint.getCustomerID(), complaint.getRelatedOrderID(), representativeID,
                    complaint.getStatus(), sanitizeForSQL(complaint.getDescription()), complaint.getRefund(), complaint.getComplaintID()),
                    Statement.RETURN_GENERATED_KEYS);
            return true;

        } catch (SQLException e) {
            System.err.printf("An error occurred inserting %s:\n%s\n", complaint, e.getMessage());
            return false;
        }
    }

    /**
     * get Complaints from DB (all Complaints)
     * @return all Complaints in map
     */
    public Map<Integer, Object> getAllComplaints() {
        return getComplaintsByID(-1);
    }

    /**
     * get Complaints from DB
     * @param complaintId specific order identifier, '-1' for all orders
     * @return specific / all orders , null if error
     */
    public Map<Integer, Object> getComplaintsByID(int complaintId) {
        ResultSet rs;

        if (complaintId == -1) { // get all rows
            rs = queryTable("Complaints");

        } else { // get specific order
            rs = queryTable("Complaints", "idComplaints", complaintId);
        }

        return parseComplaintsFromDBToMap(rs);
    }


    public Map<Integer, Object> parseComplaintsFromDBToMap(ResultSet rs){
        Map<Integer, Object>  myComplaints = new HashMap<>();
        if (rs == null){
            return null;
        }
        else {
            try {
                while (rs.next()) {
                    Complaint.ComplaintStatus _complaintStatus = parseComplaintStatus(rs.getString("status"));
                    if (null == _complaintStatus){
                        System.err.printf("Error occurred getting complaintStatus from table \"%s\"", "Complaints");
                        return null;
                    }
                    Integer rep = (rs.getInt("idRepresentative") == 0)? -1 : rs.getInt("idRepresentative");
                    Complaint rowComplaint = new Complaint(
                            rs.getInt("idComplaints"),
                            rs.getInt("idUser"),
                            rs.getInt("idOrder"),
                            rep,
                            _complaintStatus,
                            desanitizeFromSQL(rs.getString("description")),
                            rs.getDouble("refund")
                    );
                    myComplaints.put(rs.getInt("idComplaints"), rowComplaint);
                }
            } catch (SQLException e) {
                System.err.printf("Error occurred getting data from table \"%s\":\n%s", "Complaints", e.getMessage());
                return null;
            }
        }
        return myComplaints;
    }

    /**
     * Cancelling a complaint by setting its state to canceled
     * @param complaintID
     * @return True for success, false otherwise.
     */
    public boolean cancelComplaint(Integer complaintID){
        try {
            Statement stmt = db_conn.createStatement();
            stmt.executeUpdate(String.format("UPDATE Complaints SET status = '%s'" +
                            "WHERE idComplaints = '%s'",
                    Complaint.ComplaintStatus.CANCELLED, complaintID),
                    Statement.RETURN_GENERATED_KEYS);
            return true;

        } catch (SQLException e) {
            System.err.printf("An error occurred cancelling complaint with id %s:\n%s\n", complaintID, e.getMessage());
            return false;
        }
    }

    private Complaint.ComplaintStatus parseComplaintStatus(String status) {
        Complaint.ComplaintStatus ret;
        switch (status) {
            case "NEW":
                ret = Complaint.ComplaintStatus.NEW;
                break;
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

    //Making Reports Section:

    /**
     * WIP will add one report at a time prob so we can test it with Rami.
     * @param reportType the type of report to be returned to the Manager/Tasker.
     * @return the report. (null if wrong type or not implementd yet.
     */
    public String makeReportFromDB(Report.ReportType reportType, Integer managerID)
    {
        String manager = managerID == 999 ? "Created by CPS's Tasker:" : "Created by Manager:";
        String reportToReturn ="\n\n\t\t\t\t";
        switch(reportType)
        {
            case DAILY_FINISHED_ORDERS:
            {
                reportToReturn += "Daily finished orders report, "
                        + manager + ":\n" + makeOrdersReport(Order.OrderStatus.FINISHED, 1);
                return reportToReturn;
            }
            case DAILY_CANCELED_ORDERS:
            {
                reportToReturn += "Daily canceled orders report,  "
                        + manager + "\n" + makeOrdersReport(Order.OrderStatus.DELETED, 1);
                return reportToReturn;
            }
            case DAILY_LATED_ORDERS:
            {
                reportToReturn += "Daily late customer's orders report, "
                        + manager + "\n" + makeOrdersReport(null, 1);
                return reportToReturn;
            }

            default:
                return null;
        }
    }

    /**
     * Generic Report maker from Order's table.
     * @param dayToValidate - The amount of days from the actualy entry to today this report is valid.
     * @return the wanted report.
     */
    private String makeOrdersReport(Order.OrderStatus orderType, Integer dayToValidate)
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
        // Adding another flair!
        return String.valueOf(report + "\n\t\t" + "Total of " + countRows + " Rows.");
    }

    private String addOrderRow(Order order, String rowLine)
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


}