package entity;

import java.util.Date;
import java.util.Objects;

/**
 * An extension to Order's class with new an entry time and pre-charge.
 */

public class PreOrder extends Order {
    /**
     * Private properties
     */

    private boolean _isMarkedLate;
    private boolean _isLateArrivalConfirmedByCustomer;

    /**
     * Empty constructor for use with Jackson.
     */
    public PreOrder() {}

    /**
     * Constructor
     * @param customerID customer's ID
     * @param carID Order's car plate
     * @param estimatedExitTime Order's estimated exit time
     * @param charge Order's estimated price
     * @param estimatedEntryTime Order's estimated entry time
     */
    public PreOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber, double charge, Date estimatedEntryTime) {
        super(customerID, carID, estimatedExitTime, parkingLotNumber);
        super.setEstimatedEntryTime(estimatedEntryTime);
        this.setCharge(charge);
        this._isMarkedLate = false;
        this._isLateArrivalConfirmedByCustomer = false;
    }

    /**
     * Copy Constructor
     * @param other Other PreOrder to copy
     */
    public PreOrder(PreOrder other) {
        super(other.getCostumerID(), other.getCarID(), other.getEstimatedExitTime(), other.getParkingLotNumber());
        super.setEstimatedEntryTime(other.getEstimatedEntryTime());
        this.setCharge(other.getCharge());
    }


    @Override
    public String toString() {
        return super.toString() +
                ", " +
                "charge=" + getCharge();
    }

    public boolean isLateArrivalConfirmedByCustomer() { return _isLateArrivalConfirmedByCustomer; }

    public void setLateArrivalConfirmedByCustomer() { this._isLateArrivalConfirmedByCustomer = true; }

    public boolean isMarkedLate() { return _isMarkedLate; }

    public void setMarkedLate() { this._isMarkedLate = true; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreOrder)) return false;
        if (!super.equals(o)) return false;
        PreOrder preOrder = (PreOrder) o;
        return Double.compare(preOrder.getCharge(), this.getCharge()) == 0;
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), getCharge());
    }
}
