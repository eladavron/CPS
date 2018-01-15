package entity;

import java.util.Date;
import java.util.LinkedHashMap;

import static entity.Subscription.SubscriptionType.REGULAR;

/**
 * This Class represents a regular subscription of a customer's car.
 */
public class RegularSubscription extends Subscription {
    private String _regularExitTime; // if applicable. else "00:00". now as *String* for simplicity
    private final Integer _parkingLotNumber;

    /**
     * Class Constructor.(toDb)
     *
     * @param userID    user id
     * @param carID      The subscription's car id.
     * @param parkingLotNumber the Subscription is only valid for that parking lot
     * @param regularExitTime   the car can only stay until it's regular exit time
     */
    public RegularSubscription(Integer userID, Integer carID, String regularExitTime, Integer parkingLotNumber) {
        super(userID, carID, SubscriptionType.REGULAR);
        this._parkingLotNumber = parkingLotNumber;
        this._regularExitTime = regularExitTime;
    }

    /**
     * Db c'tor (from Db)
     * @param subsId
     * @param carId
     * @param userId
     * @param parkingLotNumber
     * @param endDate
     * @param regularExitTime
     */
    public RegularSubscription(Integer subsId, Integer carId, Integer userId, Integer parkingLotNumber, Date endDate,
                               String regularExitTime) {
        super(subsId,  carId,  userId, endDate, SubscriptionType.REGULAR);
        this._parkingLotNumber = parkingLotNumber;
        this._regularExitTime = (regularExitTime == null) ? "00:00" : regularExitTime; // default when not presented
    }

    /**
     * A custom constructor for manual JSON Deserialization.
     * @param deserialize Json deserialized LinkedHashMap
     */
    public RegularSubscription(LinkedHashMap deserialize)
    {
        super((Integer) deserialize.get("userID"), (Integer) deserialize.get("carID"), REGULAR);
        this._parkingLotNumber = (Integer) deserialize.get("parkingLotNumber");
        this._regularExitTime = deserialize.containsKey("regularExitTime") ? (String) deserialize.get("regularExitTime") : "00:00";
    }

    public String getRegularExitTime() {
        return _regularExitTime;
    }

    public void setRegularExitTime(String exitTime){
        _regularExitTime = exitTime;
    }

    public Integer getParkingLotNumber() {
        return _parkingLotNumber;
    }

    @Override
    public String toString() {
        return "Regular Subscription" +
                ", regular Exit Time=" + _regularExitTime +
                ", parking Lot Number=" + _parkingLotNumber +
                "," + super.toString();
    }

}
