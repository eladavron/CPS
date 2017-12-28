package entity;

import java.util.Date;
import java.util.Objects;

/**
 * A sample class of how an Order will look in the system.
 */

public class PreOrder extends Order {
    /**
     * Private properties
     */

    private double _charge;
    private Date _estimatedEntryTime;

    /**
     * Constructor
     * @param customerID customer's ID
     * @param carID Order's car plate
     * @param estimatedExitTime Order's estimated exit time
     * @param charge Order's estimated price
     * @param estimatedEntryTime Order's estimated entry time
     */
    public PreOrder(int customerID, Integer carID, Date estimatedExitTime, ParkingLotNumber parkingLotNumber, double charge, Date estimatedEntryTime) {
        super(customerID, carID, estimatedExitTime, parkingLotNumber);
        this._charge = charge;
        this._estimatedEntryTime =  estimatedEntryTime;
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
