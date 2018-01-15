package controller;

import entity.Complaint;
import entity.Employee;
import entity.ParkingSpace;
import entity.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.customerServiceController;
import static controller.Controllers.dbController;
import static controller.Controllers.parkingController;

public class EmployeeController {
    private Map<Integer, Employee> _employeeList = new HashMap<>();
    private static EmployeeController ourInstance = new EmployeeController();

    public static EmployeeController getInstance() {
        return ourInstance;
    }

    private EmployeeController() {
        System.out.print("\tWaking up the employees...");
        getEmployeesFromDB();
        System.out.println("Awake!");;
    }

    /**
     * Initiate parking lot.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
    public void initiateParkingLot(Integer parkingLotNumber){
        parkingController.initiateParkingLot(parkingLotNumber);
    }

    /**
     * Private function that retrieves the Employees list form the DB, on startup.
     */
    private void getEmployeesFromDB() {
        setEmployeesList(dbController.getEmployees());
    }

    /**
     * Private function used in order to convert the general user array into employee
     * and then map it into our employee list.
     * @param list - the employee list taken from the DB.
     */
    private void setEmployeesList(ArrayList<User> list)
    {
        list.forEach(user ->
        {
            Employee employee = (Employee) user;
            _employeeList.put(employee.getUID(), employee);
        });
    }


    /**
     * This function is responsible for setting a parking space status due to the employee's requirement.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     * @param status The parking space status to set.
     * @param x The parking space coordinate in width axes.
     * @param y The parking space coordinate in height axes.
     * @param z The parking space coordinate in depth axes.
     */
    public void setSpaceStatus(Integer parkingLotNumber, ParkingSpace.ParkingStatus status, Integer x, Integer y, Integer z){
        parkingController.setParkingSpaceStatus(parkingLotNumber,status, x,y,z);
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
        customerServiceController.handleComplaint(complaint);
    }

}
