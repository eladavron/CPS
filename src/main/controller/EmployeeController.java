package controller;

import entity.Complaint;
import entity.Employee;
import entity.ParkingSpace;

import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.dbController;
import static controller.Controllers.parkingController;

public class EmployeeController {
    private Map<Integer, Employee> _employeeList;
    private static EmployeeController ourInstance = new EmployeeController();

    public static EmployeeController getInstance() {
        return ourInstance;
    }

    //TODO: Get employees from DB and load them to the employeeList.
    private EmployeeController() {
        this._employeeList = new HashMap<>();
    }

    /**
     * Initiate parking lot.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
    public void initiateParkingLot(Integer parkingLotNumber){
        ParkingController.getInstance().initiateParkingLot(parkingLotNumber);
    }
    //TODO: Check this function and specifically check whether the DB supports getting employees data
//    public Employee getEmployee(Integer employeeID)
//    {
//        return _employeeList.get(employeeID);
//    }

    //TODO: Check whether DBController.GetDAta really returns an employees list.
//    public void getEmployeesFromDB() {
//        this._employeeList.putAll(dbController.GetData("employees"));
//    }

    /**
     * This function is responsible for setting a parking space status due to the employee's requirement.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     * @param status The parking space status to set.
     * @param x The parking space coordinate in width axes.
     * @param y The parking space coordinate in height axes.
     * @param z The parking space coordinate in depth axes.
     */
    public void setSpaceStatus(Integer parkingLotNumber, ParkingSpace.ParkingStatus status, Integer x, Integer y, Integer z){
        ParkingController.getInstance().setParkingSpaceStatus(parkingLotNumber,status, x,y,z);
    }

    /**
     * Setting the specific parking lot as full.
     * TODO: direct the user to another parking lot (think about something nice).
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
    public void setFullParkingLot(Integer parkingLotNumber){
        parkingController.toggleParkingLotStatus(parkingLotNumber);
    }

    /**
     * Employee Controller calls the handleComplaint function which is implemented in CustomerServiceController.
     * @param complaint The complaint to handle/manage.
     */
    public void manageComplaint(Complaint complaint){
        CustomerServiceController.getInstance().handleComplaint(complaint);
    }

}
