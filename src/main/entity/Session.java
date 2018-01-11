package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a session between the server and a single client.
 */
@JsonIgnoreProperties
public class Session {
    private int _sid;
    private User _user;
    private ParkingLot _parkingLot;

    /**
     * Empty constructor for Jackson
     */
    public Session() {
    }

    /**
     * Full constructor
     * @param sid Session ID
     * @param user Logged in user.
     * @param parkingLot Logged in parking lot. Can be null if logged in remotely.
     */
    public Session(int sid, User user, ParkingLot parkingLot) {
        this._sid = sid;
        this._user = user;
        this._parkingLot = parkingLot;
    }

    public int getSid() {
        return _sid;
    }

    public void setSid(int sid) {
        this._sid = sid;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        this._user = user;
    }

    public ParkingLot getParkingLot() {
        return _parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this._parkingLot = parkingLot;
    }
}
