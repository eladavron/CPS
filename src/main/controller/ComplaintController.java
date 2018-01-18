package controller;

import entity.Complaint;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.dbController;
import static controller.Controllers.orderController;

public class ComplaintController {
    private Map<Integer, Complaint> _complaintsList;

    private static ComplaintController instance;
    /**
     * The Static initializer constructs the instance at class
     * loading time; this is to simulate a more involved
     * construction process (it it were really simple, you'd just
     * use an initializer)
     */
    static {
        try {
            instance = new ComplaintController();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQLException on complaints init");
        }
    }

    public static ComplaintController getInstance() {
        return instance;
    }


    /**
     * A private Constructor prevents any other class from
     * instantiating.
     * we assume here there is a live connection to the DB.
     */
    private ComplaintController()throws SQLException {
        this._complaintsList = new HashMap<>();
        getComplaintsFromDb();
    }

    /**
     * Handle complaint by setting it with a representative id and changing it to OPEN
     * @param complaintID The complaint to handle.
     * @param representativeID
     */
    public boolean fileComplaint(Integer complaintID, Integer representativeID)throws SQLException {
        Complaint myComplaint = getComplaintByID(complaintID);
        if (myComplaint == null)
            return false;
        myComplaint.setStatus(Complaint.ComplaintStatus.OPEN);
        myComplaint.setCustomerServiceRepresentativeID(representativeID);
        return dbController.updateComplaint(myComplaint);
    }

    /**
     * Accept complaint by setting a full refund.
     * @param complaintID The complaint to handle.
     */
    public boolean acceptComplaint(Integer complaintID) throws SQLException{
        Complaint myComplaint = getComplaintByID(complaintID);
        if (myComplaint == null)
            return false;
        Integer orderID = myComplaint.getRelatedOrderID();
        myComplaint.setRefund(orderController.getOrder(orderID).getPrice());
        myComplaint.setStatus(Complaint.ComplaintStatus.ACCEPTED);
        return dbController.updateComplaint(myComplaint);

    }

    /**
     * Reject complaint by setting its state to REJECTED
     * @param complaintID The complaint to handle.
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
     */
    public boolean cancelComplaint(Integer complaintID) throws SQLException{
        Complaint myComplaint = getComplaintByID(complaintID);
        if (myComplaint == null) // complaint with that id no found;
            return false;
        boolean ret = dbController.cancelComplaint(complaintID);
        // remove complaint from active list (after DB just as a precaution.
        removeComplaintFromListByID(complaintID);
        return ret;

    }

    /**
     * Create a customer complaint.
     */
    public Complaint createComplaint(Integer customerID, Integer orderID, String Description, Integer parkingLotNumber) throws SQLException{
        Complaint complaint = new Complaint(customerID, orderID, Description, parkingLotNumber);
        complaint.setStatus(Complaint.ComplaintStatus.NEW);
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
     * Create a customer complaint. also from obj
     * "HACHANA LEMAZGAN"
     */
    public Complaint createComplaint(Complaint complaint) throws SQLException{
        return createComplaint(complaint.getCustomerID(), complaint.getRelatedOrderID(),
                complaint.getDescription(), complaint.getParkingLotNumber());
    }

    /**
     * Get complaint from complaintList by complaint id
     * @param complaintID
     * @return
     */
    public Complaint getComplaintByID(Integer complaintID){
        return _complaintsList.get(complaintID);
    }

    /**
     * Return a list of complaints opened by a user.
     * @param uID User ID
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
     * remove  complaint from complaintList by complaint id
     * @param complaintID
     * @return
     */
    private void removeComplaintFromListByID(Integer complaintID){
        _complaintsList.remove(complaintID);
    }

    /**
     * get all complaints from db (occurs on init)
     */
    public void getComplaintsFromDb() throws SQLException {

        putAllComplaints(dbController.getAllComplaints());
    }

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
