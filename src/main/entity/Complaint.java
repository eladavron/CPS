package entity;

import java.util.Objects;

public class Complaint {
    private Integer _customerServiceRepresentativeID = -1;
    private Integer _complaintID = -1;
    private Integer _customerID;
    private Integer _relatedOrderID;
    private double _refund = 0.0;
    private ComplaintStatus _status;
    private String _description;

    public enum ComplaintStatus{
        NEW,
        OPEN,
        ACCEPTED,
        REJECTED,
        CANCELLED
    }

    /**
     * Empty constructor for Jackson
     */
    public Complaint(){}


    /**
     * New complaint creation (to be pushed to DB)
     * Use this when you create complaint.
     * Later set the representative ID -> state to OPEN, refund, etc.
     * @param customerID
     * @param relatedOrderID
     * @param description
     */
    public Complaint(Integer customerID, Integer relatedOrderID, String description) {
        this._status = ComplaintStatus.NEW;
        this._customerID = customerID;
        this._relatedOrderID = relatedOrderID;
        this._description = description;
    }

    /**
     * New complaint obj (originated in DB)
     * @param complaintID
     * @param customerID
     * @param relatedOrderID
     * @param customerServiceRepresentativeID
     * @param status
     * @param description
     * @param refund
     */
    public Complaint(Integer complaintID, Integer customerID, Integer relatedOrderID, Integer customerServiceRepresentativeID, ComplaintStatus status, String description, Double refund) {
        this._complaintID = complaintID;
        this._customerID = customerID;
        this._relatedOrderID = relatedOrderID;
        this._customerServiceRepresentativeID = customerServiceRepresentativeID;
        this._status = status;
        this._description = description;
        this._refund = refund;
    }




    /**
     * Getters and Setters.
     */

    public Integer getCustomerServiceRepresentativeID() {
        return _customerServiceRepresentativeID;
    }

    public void setCustomerServiceRepresentativeID(Integer customerServiceRepresentativeID) {
        this._customerServiceRepresentativeID = customerServiceRepresentativeID;
    }

    public ComplaintStatus getStatus() {
        return _status;
    }

    public void setStatus(ComplaintStatus status) {
        this._status = status;
    }

    public Integer getComplaintID() {
        return _complaintID;
    }

    public void setComplaintID(Integer complaintID) {
        this._complaintID = complaintID;
    }

    public Integer getCustomerID() {
        return _customerID;
    }

    public void setCustomerID(Integer customerID) {
        this._customerID = customerID;
    }

    public Integer getRelatedOrderID() {
        return _relatedOrderID;
    }

    public void setRelatedOrderID(Integer relatedOrderID) {
        this._relatedOrderID = relatedOrderID;
    }

    public double getRefund() {
        return _refund;
    }

    public void setRefund(double refund) {
        this._refund = refund;
    }
    public String getDescription() {
        return _description;
    }

    public void setDescription(String description) {
        this._description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Complaint complaint = (Complaint) o;
        return Objects.equals(_customerServiceRepresentativeID, complaint._customerServiceRepresentativeID) &&
                Objects.equals(_customerID, complaint._customerID) &&
                Objects.equals(_relatedOrderID, complaint._relatedOrderID) &&
                Objects.equals(_description, complaint._description) &&
                Objects.equals(_refund, complaint._refund);
    }

    @Override
    public String toString() {  //TODO: elaborate
        return "Complaint" +
                "No.: " + this._complaintID +
                ", handled by: " + this._customerServiceRepresentativeID;
    }

    /**
     * A "To String" friendly for GUI purposes.
     * @return a usable string
     */
    public String getGUIString()
    {
        return
        "Complaint No. " + this.getComplaintID()
                + (this.getRelatedOrderID().equals(-1) ? "" : "\nRegarding Order No. " + this.getRelatedOrderID())
                + "\nComplaint Status: " + this.getStatus()
                + "\nDetailes: " + this.getDescription()
                + (this.getStatus().equals(Complaint.ComplaintStatus.ACCEPTED) ? "\nRefund given: " + this.getRefund() + " NIS (The cheque is in the mail)." : "")
                + (this.getCustomerServiceRepresentativeID().equals(-1) ? "" : "\nAssigned Representative: " + this.getCustomerServiceRepresentativeID());
    }
}
