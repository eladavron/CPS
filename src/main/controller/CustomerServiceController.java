package controller;

import Exceptions.CustomerNotificationFailureException;

import java.sql.SQLException;
import java.util.Date;


import static controller.Controllers.orderController;
import static controller.Controllers.parkingController;


/**
 * Customer service controller which is responsible for all the functionality of CSR.
 */
public class CustomerServiceController {
    private static CustomerServiceController ourInstance = new CustomerServiceController();
    public static ComplaintController complaintController; //child controller

    public static CustomerServiceController getInstance() {
        return ourInstance;
    }

    private CustomerServiceController() {
        complaintController = ComplaintController.getInstance();
    }


    /**
     * Reserve a parking lot space and change its status to RESERVED for the specific order ID.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     * @param orderID The order ID of the reserved parking space.
     * @param x The parking space coordinate in width axes
     * @param y The parking space coordinate in height axes.
     * @param z The parking space coordinate in depth axes.
     * @param customerID Order's customer ID.
     * @param carID Order's car ID.
     * @param estimatedExitTime Order's estimated exit time.
     * @param estimatedEntryTime Order's estimated entry time.
     */
    public void reserveParking(Integer parkingLotNumber, Integer orderID, Integer x,
                               Integer y, Integer z, Integer customerID,
                               Integer carID, Date estimatedExitTime,
                               Date estimatedEntryTime) throws SQLException, CustomerNotificationFailureException {
        orderController.makeNewPreOrder(customerID,carID,estimatedExitTime,parkingLotNumber,estimatedEntryTime);
        parkingController.reserveParkingSpace(parkingLotNumber,orderID,x,y,z);

    }



}
