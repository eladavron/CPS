package controller;

import entity.Complaint;
import entity.Order;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.dbController;
import static controller.Controllers.orderController;

/**
 * A server-side controller for handling anything related to {@link Complaint}s.<br>
 * This class is a Singleton.
 */
public class ComplaintController {
    private Map<Integer, Complaint> _complaintsList;

    private static ComplaintController instance;

    /*
      The Static initializer constructs the instance at class
      loading time; this is to simulate a more involved
      construction process (it it were really simple, you'd just
      use an initializer)
     */
    static {
        try {
            instance = new ComplaintController();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQLException on complaints init");
        }
    }

    /**
     * Gets the instance of the Singleton controller.
     * @return the instance of the Singleton.
     */
    public static ComplaintController getInstance() {
        return instance;
    }


    /**
     * A private Constructor prevents any other class from
     * instantiating.<br>
     * Assumes there is a live connection to the DB.
     */
    private ComplaintController()throws SQLException {
        this._complaintsList = new HashMap<>();
        getComplaintsFromDb();
    }

    /**
     * Handle complaint by setting it with a representative id and changing it to OPEN
     * @param complaintID The complaint to handle.
     * @return True if successful, false otherwise.
     * @throws SQLException if an exception occurs during the SQL operation.
     */
    public boolean fileComplaint(Integer complaintID)throws SQLException {
        Complaint myComplaint = getComplaintByID(complaintID);
        if (myComplaint == null)
            return false;
        myComplaint.setStatus(Complaint.ComplaintStatus.OPEN);
        return dbController.updateComplaint(myComplaint);
    }

    /**
     * Accept complaint by setting a full refund.
     * @param complaintID The complaint to handle.
     * @return True if successful, false otherwise.
     * @throws SQLException if an exception occurs during the SQL operation.
     */
    public boolean acceptComplaint(Integer complaintID) throws SQLException{
        Complaint myComplaint = getComplaintByID(complaintID);
        if (myComplaint == null)
            return false;
        Integer orderID = myComplaint.getRelatedOrderID();
        Order relatedOrder = null;
        if (orderID != null && !orderID.equals(-1))
        {
            relatedOrder = orderController.getOrder(orderID);
        }
        myComplaint.setRefund(relatedOrder == null ? 0.0 : relatedOrder.getPrice());
        myComplaint.setStatus(Complaint.ComplaintStatus.ACCEPTED);
        return dbController.updateComplaint(myComplaint);
    }

    /**
     * Reject complaint by setting its state to REJECTED
     * @param complaintID The complaint to handle.
     * @return True if successful, false otherwise.
     * @throws SQLException if an exception occurs during the SQL operation.
     */
    public boolean rejectComplaint(Integer complaintID) throws SQLException{
        Complaint myComplaint = getComplaintByID(complaintID);
        if (myComplaint == null)
            return false;
        myComplaint.setStatus(Complaint.ComplaintStatus.REJECTED);
        boolean ret = dbController.updateComplaint(myComplaint);
        return ret;

    }

    /**
     * Cancel complaint by setting its state to CANCELLED
     * @param complaintID The complaint to handle.
     * @return True if successful, false otherwise.
     * @throws SQLException if an exception occurs during the SQL operation.
     */
    public boolean cancelComplaint(Integer complaintID) throws SQLException{
        Complaint myComplaint = getComplaintByID(complaintID);
        if (myComplaint == null) // complaint with that id no found;
            return false;
        if(dbController.cancelComplaint(complaintID))
        {
            // remove complaint from active list (after DB just as a precaution.
            removeComplaintFromListByID(complaintID);
            return true;
        };
        return false;
    }

    /**
     * Creates a new Complaint.
     * @param customerID The Customer ID of the customer filing the complaint. Can NOT be null.
     * @param orderID The ID of the order related to the complaint. Can be null.
     * @param Description The description of the complaint. Will be sanitized for SQL injections later.
     * @param parkingLotNumber The Parking Lot related to the complaint. CAN'T be null (Unless an Order is selected,
     *                         in which case it should automatically transfer it.
     * @return The FILED complaint with an updated Complaint ID and status.
     * @throws SQLException if an exception occurs during the SQL operation.
     */
    public Complaint createComplaint(Integer customerID, Integer orderID, String Description, Integer parkingLotNumber) throws SQLException{
        Complaint complaint = new Complaint(customerID, orderID, Description, parkingLotNumber);
        complaint.setStatus(Complaint.ComplaintStatus.OPEN);
        if (dbController.insertComplaint(complaint)){
            _complaintsList.put(complaint.getComplaintID(), complaint);
            return complaint;
        }
        else
        {
            return null;
        }

    }

    /**
     * Creates a new Complaint.<br>
     * Extracts all the information from the {@link Complaint} object to use in {@link #createComplaint(Integer, Integer, String, Integer)}
     * @param complaint The Complaint to file.
     * @return The FILED complaint with an updated Complaint ID and status.
     * @throws SQLException if an exception occurs during the SQL operation.
     */
    public Complaint createComplaint(Complaint complaint) throws SQLException{
        return createComplaint(complaint.getCustomerID(), complaint.getRelatedOrderID(),
                complaint.getDescription(), complaint.getParkingLotNumber());
    }

    /**
     * Get complaint from complaintList by complaint id
     * @param complaintID The ID of the complaint to retrieve.
     * @return The complaint. If not found, throws an exception.
     */
    public Complaint getComplaintByID(Integer complaintID){
        return _complaintsList.get(complaintID);
    }

    /**
     * Return a list of complaints opened by a specific user.
     * @param uID User ID of the user we want to query.
     * @return The list. Can be empty, can't be null.
     */
    public ArrayList<Complaint> getComplaintsByUserID(Integer uID)
    {
        ArrayList<Complaint> returnList = new ArrayList<>();
        for (Complaint complaint : _complaintsList.values())
        {
            if (complaint.getCustomerID().equals(uID))
            {
                returnList.add(complaint);
            }
        }
        return returnList;
    }

    /**
     * Remove complaint from complaintList based on Complaint ID.
     * @param complaintID The ID of the complaint to remove.
     */
    private void removeComplaintFromListByID(Integer complaintID){
        _complaintsList.remove(complaintID);
    }

    /**
     * Returns a list of ALL complaints currently open.
     * @return A list of all the complaints currently open. Returns it as an ArrayList for use with the {@link entity.Message} class.
     * @throws SQLException if an exception occurs during the SQL operation.
     */
    public ArrayList<Object> getAllComplaint() throws SQLException {
        return new ArrayList<Object>(dbController.getAllComplaints().values());
    }

    /**
     * get all complaints from db (occurs on init)
     * @throws SQLException if something goes wrong with the SQL operations.
     */
    public void getComplaintsFromDb() throws SQLException {

        putAllComplaints(dbController.getAllComplaints());
    }

    /**
     * Given a map of complaints, puts them all in the Complaint list.
     * @param objectsMap A map of Complaints keyed by their IDs.
     */
    private void putAllComplaints(Map<Integer, Object> objectsMap)
    {
        for (Object obj : objectsMap.values())
        {
            Complaint comp;
            comp = (Complaint) obj;
            this._complaintsList.put(comp.getComplaintID(), comp);

        }
    }
}
