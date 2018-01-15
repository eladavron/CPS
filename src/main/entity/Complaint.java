package entity;

import java.util.Objects;

public class Complaint {
    private Employee _customerServiceRepresentive;
    private Integer _complaintID = 0;
    private Customer _customer;
    private Integer _relatedOrderID;
    private double _refund;
    private ComplaintStatus _status;

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

    public Complaint(Employee customerServiceRepresentive, Customer customer, Integer relatedOrderID, Integer refund) {
        this._status = ComplaintStatus.NEW;
        this._customerServiceRepresentive = customerServiceRepresentive;
        this._complaintID++;
        this._customer = customer;
        this._relatedOrderID = relatedOrderID;
        this._refund = refund;
    }

    /**
     * Getters and Setters.
     */
    public Employee getCustomeServiceRepresentive() {
        return _customerServiceRepresentive;
    }

    public void setCustomeServiceRepresentive(Employee customeServiceRepresentive) {
        this._customerServiceRepresentive = customeServiceRepresentive;
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

    public Customer getCustomer() {
        return _customer;
    }

    public void setCustomer(Customer customer) {
        this._customer = customer;
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

    public void setRefund(Integer refund) {
        this._refund = refund;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Complaint complaint = (Complaint) o;
        return Objects.equals(_customerServiceRepresentive, complaint._customerServiceRepresentive) &&
                Objects.equals(_customer, complaint._customer) &&
                Objects.equals(_relatedOrderID, complaint._relatedOrderID) &&
                Objects.equals(_refund, complaint._refund);
    }

    @Override
    public String toString() {
        return "Complaint" +
                "No.: " + this._complaintID +
                ", handled by: " + this._customerServiceRepresentive._email;
    }
}
