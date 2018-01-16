package entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a session between the server and a single client.
 */
@JsonIgnoreProperties
public class Session {
    private int _sid;
    private User.UserType _userType;
    private Customer _customer; //Temporary to make things work for now - will be changed later.
    private User _user; //Generic user morphism
    private String _email; // user email
    private ParkingLot _parkingLot;
    private Order _orderInNeedOfPayment = null;

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
        this._userType = type;
        switch (type)
        {
            case CUSTOMER:
                this._customer = (Customer) user;
                break;
        }
        this._email = email;
        this._parkingLot = parkingLot;
    }

    public User.UserType getUserType() {
        return _userType;
    }

    public void setUserType(User.UserType userType) {
        this._userType = userType;
    }

    public int getSid() {
        return _sid;
    }

    public void setSid(int sid) {
        this._sid = sid;
    }

    public Customer getCustomer() {
        return _customer;
    }

    public void setCustomer(Customer customer) {
        this._customer = customer;
    }

    public User getUser() {
        return _user;
    }

    public void setUser(User user) {
        this._user = user;
        this._userType = user.getUserType();
        switch (_userType)
        {
            case CUSTOMER:
                _customer = (Customer) user;
        }
    }

    public String getEmail() { return _email; }

    public void setEmail(String _email) { this._email = _email; };

        public ParkingLot getParkingLot() {
            return _parkingLot;
        }

        public void setParkingLot(ParkingLot parkingLot) {
            this._parkingLot = parkingLot;
        }
    }