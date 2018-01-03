package entity;

import java.util.Date;


/**
 * A class for preorders.
 * Extends the "Order" class with estimated entry time and charged amound.
 * @author Elad Avron
 */
public class Preorder extends Order {
    private Date _estimatedEntryTime;
    private double _chargedAmount;

    /**
     * A constructor for the preorder
     * @param Name Name of the costumer
     * @param carID Car license plate
     * @param estimatedExitTime Time costumer estimates they will leave the parking lot
     * @param estimatedEntryTime Time costumer estimates they will arrive at the parking lot
     * @param chargedAmount Amount already charged from customer
     */
    public Preorder(String Name, Integer carID, Date estimatedExitTime, Date estimatedEntryTime, Double chargedAmount) {
        super(Name, carID, estimatedExitTime);
        _estimatedEntryTime = estimatedEntryTime;
        _chargedAmount = chargedAmount;
    }

    public Date getEstimatedEntryTime() {
        return _estimatedEntryTime;
    }

    public void setEstimatedEntryTime(Date estimatedEntryTime) {
        this._estimatedEntryTime = estimatedEntryTime;
    }

    public double getChargedAmount() {
        return _chargedAmount;
    }

    public void setChargedAmount(double chargedAmount) {
        this._chargedAmount = chargedAmount;
    }
}
