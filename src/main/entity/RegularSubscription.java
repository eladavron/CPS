package entity;

import java.util.Date;

/**
 * This Class represents a regular subscription of a customer's car.
 */
public class RegularSubscription extends Subscription {
    private final Date _regularEntryTime;
    private final Date _regularExitTime;
    private final Integer _parkingLotNumber;

    /**
     * Class Constructor.
     *
     * @param carID      The subscription's car id.
     * @param expiration The expiration of the subscription.
     * @param parkingLotNumber the Subscription is only valid for that parking lot
     * @param regularEntryTime the car can only enter on it's regular time
     * @param regularExitTime   the car can only stay until it's regular exit time
     */
    public RegularSubscription(Integer carID, Date expiration, Date regularEntryTime, Date regularExitTime, Integer parkingLotNumber) {
        super(carID, SubscriptionType.REGULAR);
        this._parkingLotNumber = parkingLotNumber;
        this._regularEntryTime = regularEntryTime;
        this._regularExitTime = regularExitTime;
    }

    public Date getRegularEntryTime() {
        return _regularEntryTime;
    }

    public Date getRegularExitTime() {
        return _regularExitTime;
    }

    public Integer getParkingLotNumber() {
        return _parkingLotNumber;
    }

    @Override
    public String toString() {
        return "Regular Subscription" +
                "regular Entry Time=" + _regularEntryTime +
                ", regular Exit Time=" + _regularExitTime +
                ", parking Lot Number=" + _parkingLotNumber +
                "," + super.toString();
    }

}
