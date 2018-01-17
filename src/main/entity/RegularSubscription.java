package entity;

import java.util.ArrayList;
import java.util.Date;

import static entity.Subscription.SubscriptionType.REGULAR;
import static entity.Subscription.SubscriptionType.REGULAR_MULTIPLE;

/**
 * This Class represents a regular subscription of a customer's car.
 */
public class RegularSubscription extends Subscription {
    private String _regularExitTime; // if applicable. else "00:00". now as *String* for simplicity
    private Integer _parkingLotNumber;

    /**
     * Class Constructor.(toDb)
     *
     * @param userID    user id
     * @param carsIDList      The subscription's cars id list
     * @param parkingLotNumber the Subscription is only valid for that parking lot
     * @param regularExitTime   the car can only stay until it's regular exit time
     */
    public RegularSubscription(Integer userID, ArrayList<Integer> carsIDList, String regularExitTime, Integer parkingLotNumber) {
        super(userID, carsIDList, (carsIDList.size()>1) ? REGULAR_MULTIPLE : REGULAR);
        this._parkingLotNumber  = parkingLotNumber;
        this._regularExitTime   = regularExitTime;
    }

    /**
     * Db c'tor (from Db)
     * @param subsId
     * @param userId
     * @param parkingLotNumber
     * @param endDate
     * @param regularExitTime
     */
    public RegularSubscription(Integer subsId, ArrayList<Integer> carsIDList, Integer userId, Integer parkingLotNumber, Date endDate,
                               String regularExitTime) {
        super(subsId,  carsIDList,  userId, endDate, (carsIDList.size()>1) ? REGULAR_MULTIPLE : REGULAR);
        this._parkingLotNumber = parkingLotNumber;
        this._regularExitTime = (regularExitTime == null) ? "00:00" : regularExitTime; // default when not presented
    }

    /**
     * Empty constructor for Jackson
     */
    public RegularSubscription() {
    }

    public void setParkingLotNumber(Integer parkingLotNumber) {
        this._parkingLotNumber = parkingLotNumber;
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
