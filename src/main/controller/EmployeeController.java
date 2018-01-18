package controller;

import com.itextpdf.text.DocumentException;
import entity.Complaint;
import entity.Employee;
import entity.ParkingSpace;
import entity.User;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.*;

public class EmployeeController {
    private Map<Integer, Employee> _employeeList = new HashMap<>();
    private static EmployeeController ourInstance;

    static {
        try {
            ourInstance = new EmployeeController();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQLException on employee init");
        }
    }

    public static EmployeeController getInstance() {
        return ourInstance;
    }

    private EmployeeController()throws SQLException {
        getEmployeesFromDB();
    }

    /**
     * Initiate parking lot.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
    public void initiateParkingLot(Integer parkingLotNumber) throws SQLException {
        parkingController.initiateParkingLot(parkingLotNumber);
    }

    /**
     * Private function that retrieves the Employees list form the DB, on startup.
     */
    private void getEmployeesFromDB() throws SQLException{
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
     * @param complaintID The complaintID to handle/manage.
     * @param representativeToHandleComplaint set representative to handle the complaint.
     */
    public void manageComplaint(Integer complaintID, Integer representativeToHandleComplaint) throws SQLException{
        CustomerServiceController.complaintController.fileComplaint(complaintID, representativeToHandleComplaint);
    }


    // These functions belong to **MANAGER FUNCTIONS** and are implemented in EmployeeController since
    // manager is an Employee.
    /**
     * Get the current status map of the parking lot as a pdf file.
     * TODO: Maybe show the pdf after creation (idk maybe).
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
    public void getParkingLotSpacesImage(Integer parkingLotNumber) throws DocumentException {
        try {
            parkingController.createPDF(parkingLotNumber);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //TODO: This function will work when we add the ReportController... Unitl then it will be commented.
//    public Report getReport(Integer reportID){
//        return reportController.getReport(reportID);
//    }

    public boolean isGeneralManager(Integer employeeID ){
        if(employeeID == 0){
            return true;
        }
        return false;
    }

    public boolean isParkingLotManager(Integer parkingLotNumber, Integer employeeID ){
        if(parkingController.getParkingLotByID(parkingLotNumber).getParkingLotManagerID().equals(employeeID)){
            return true;
        }
        return false;
    }

    public Employee getEmployeeByID(Integer employeeID){
        return  this._employeeList.get(employeeID);
    }

    /**
     * Gets the employee by it's email.
     * @param email
     * @return the employee of this email or null if isnt found.
     */
    public Employee getEmployeeByEmail(String email){
        for (Employee employee : this._employeeList.values())
        {
            if (employee.getEmail().equals(email)) return employee;
        }
        return  null;
    }

}


