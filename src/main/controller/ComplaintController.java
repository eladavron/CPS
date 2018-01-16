package controller;

import entity.Complaint;

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
        instance = new ComplaintController();
    }

    public static ComplaintController getInstance() {
        return instance;
    }


    /**
     * A private Constructor prevents any other class from
     * instantiating.
     * we assume here there is a live connection to the DB.
     */
    private ComplaintController() {
        this._complaintsList = new HashMap<>();
        getComplaintsFromDb();
    }

    /**
     * Handle complaint by setting it with a representative id and changing it to OPEN
     * @param complaintID The complaint to handle.
     * @param representativeID
     */
    public boolean fileComplaint(Integer complaintID, Integer representativeID) {
        Complaint myComplaint = getComplaintByID(complaintID);
        if (myComplaint == null)
            return false;
        myComplaint.setStatus(Complaint.ComplaintStatus.OPEN);
        myComplaint.setCustomerServiceRepresentativeID(representativeID);
        boolean ret = dbController.updateComplaint(myComplaint);
        return ret;
    }

    /**
     * Accept complaint by setting a full refund.
     * @param complaintID The complaint to handle.
     */
    public boolean AcceptComplaint(Integer complaintID) {
        Complaint myComplaint = getComplaintByID(complaintID);
        if (myComplaint == null)
            return false;
        Integer orderID = myComplaint.getRelatedOrderID();
        myComplaint.setRefund(orderController.getOrder(orderID).getPrice());
        myComplaint.setStatus(Complaint.ComplaintStatus.ACCEPTED);
        boolean ret = dbController.updateComplaint(myComplaint);
        return ret;

    }

    /**
     * Reject complaint by setting its state to REJECTED
     * @param complaintID The complaint to handle.
     */
    public boolean rejectComplaint(Integer complaintID) {
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
    public boolean cancelComplaint(Integer complaintID) {
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
    public Integer createComplaint(Integer customerID, Integer orderID, String Description){
        Complaint complaint = new Complaint(customerID, orderID, Description);
        if (dbController.insertComplaint(complaint)){
            _complaintsList.put(complaint.getComplaintID(), complaint);
            return complaint.getComplaintID();
        }
        else{
            return -1;
        }

    }

    /**
     * Create a customer complaint. also from obj
     * "HACHANA LEMAZGAN"
     */
    public Integer createComplaint(Complaint complaint){
        return createComplaint(complaint.getCustomerID(),complaint.getRelatedOrderID(), complaint.getDescription());

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
    public void getComplaintsFromDb() {

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
