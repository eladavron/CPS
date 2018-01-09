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

    private double _charge;
    private Date _estimatedEntryTime;

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
        this._charge = charge;
        this._estimatedEntryTime =  estimatedEntryTime;
    }

    /**
     * Copy Constructor
     * @param other Other PreOrder to copy
     */
    public PreOrder(PreOrder other) {
        super(other.getCostumerID(), other.getCarID(), other.getEstimatedExitTime(), other.getParkingLotNumber());
        this._charge = other.getCharge();
        this._estimatedEntryTime = other.getEstimatedEntryTime();
    }


    @Override
    public String toString() {
        return super.toString() +
                ", " +
                "charge=" + _charge +
                ", estimated entry time=" + _estimatedEntryTime;
    }

    public Date getEstimatedEntryTime() {
        return _estimatedEntryTime;
    }

    public void setEstimatedEntryTime(Date estimatedEntryTime) {
        this._estimatedEntryTime = estimatedEntryTime;
    }

    public double getCharge() {
        return _charge;
    }

    public void setCharge(double charge) {
        this._charge = charge;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PreOrder)) return false;
        if (!super.equals(o)) return false;
        PreOrder preOrder = (PreOrder) o;
        return Double.compare(preOrder._charge, _charge) == 0 &&
                Objects.equals(_estimatedEntryTime, preOrder._estimatedEntryTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), _charge, _estimatedEntryTime);
    }

}
