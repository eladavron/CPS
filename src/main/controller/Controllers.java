package controller;

import java.sql.SQLException;

public class Controllers {
    public static DBController              dbController;
    public static OrderController           orderController;
    public static BillingController         billingController;
    public static CustomerController        customerController;
    public static SubscriptionController    subscriptionController;
    public static ParkingController         parkingController;
    public static EmployeeController        employeeController;
    public static CustomerServiceController customerServiceController;
    public static ReportController          reportController;
    public static ComplaintController       complaintController;
    public static RobotController           robotController;

    /**
     * Since all other Controllers need the dbcontroller to first set a connection
     * we will call this function first and then init the others.
     * @param dbUrl
     * @param dbUsername
     * @param dbPwd
     * @throws SQLException
     */
    public  static void initDb(String dbUrl, String dbUsername, String dbPwd) throws SQLException {
        dbController = DBController.getInstance();
        dbController.init(dbUrl, dbUsername, dbPwd);
    }

    public static void init() throws SQLException{
        dbController              = DBController.getInstance();
        billingController         = BillingController.getInstance();
        employeeController        = EmployeeController.getInstance();
        customerServiceController = CustomerServiceController.getInstance();
        parkingController         = ParkingController.getInstance();
        orderController           = OrderController.getInstance();
        subscriptionController    = SubscriptionController.getInstance();
        customerController        = CustomerController.getInstance();
        reportController          = ReportController.getInstance();
        complaintController       = ComplaintController.getInstance();
        robotController           = RobotController.getInstance();
    }
}