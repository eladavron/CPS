package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.net.InetAddress;

/**
 * Represents a session between the server and a single client.
 */
@JsonIgnoreProperties
public class Session {
    private int _sid;
    private User _user; //Generic user morphism
    private String _email; // user email
    private ParkingLot _parkingLot;
    private Order _orderInNeedOfPayment = null;
    private long lastTransID = 0;


    public Order getOrderInNeedOfPayment() {
        return _orderInNeedOfPayment;
    }

    public void setOrderInNeedOfPayment(Order _orderInNeedOfPayment) {
        this._orderInNeedOfPayment = _orderInNeedOfPayment;
    }

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
    public Session(int sid, User user, User.UserType type, String email, ParkingLot parkingLot) {
        this._sid = sid;
        this._user = user;
        this._email = email;
        this._parkingLot = parkingLot;
    }

    public User.UserType getUserType()
    {
        if (_user != null)
            return _user.getUserType();
        else
            return null;
    }

    public int getSid() {
        return _sid;
    }

    public void setSid(int sid) {
        this._sid = sid;
    }

    public Customer getCustomer()
    {
        if (_user.getUserType().equals(User.UserType.CUSTOMER))
            return (Customer)_user;
        else
            return null; //Dangerous - maybe throw an exception instead?
    }

    public User getUser() {
        return _user;
    }

    public Integer getUserId()
    {
        if (_user != null)
            return _user.getUID();
        else
            return null;
    }

    public void setUser(User user) {
        this._user = user;
    }

    public String getEmail() { return _email; }

    public void setEmail(String _email) { this._email = _email; };

    public ParkingLot getParkingLot() {
        return _parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this._parkingLot = parkingLot;
    }

    public long getLastTransID() { return lastTransID; }

    public void setLastTransID(long lastTransID) { this.lastTransID = lastTransID; }

    @Override
    public String toString() {
        return "Session #" + _sid + ": " + _user.getName() + "(" + _user.getUserType() + "), Parking Lot: " + _parkingLot;
    }
}