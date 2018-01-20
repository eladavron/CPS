package entity;

import java.util.Date;
import java.util.Objects;

/**
 * A sample class of how an Order will look in the system.
 */

public class Order {
    /**
     * Private properties
     */

    public enum OrderStatus {PRE_ORDER,IN_PROGRESS,FINISHED,DELETED}

    private OrderStatus _orderStatus;
    private Integer _orderID;
    private Integer _carID;
    private Date _actualEntryTime;
    private Date    _estimatedEntryTime;
    private Date _estimatedExitTime;
    private Date _actualExitTime;
    private Integer _parkingLotNumber;
    private Integer _customerID;
    private Date _creationTime;
    private double _price;
    private double _charge;

    private Integer _parkingSpaceHeight = -1;
    private Integer _parkingSpaceWidth = -1;
    private Integer _parkingSpaceDepth = -1;


    /**
     * Empty constructor for use with Jackson
     */
    public Order(){}

    /**
     * Constructor
     * @param customerID Order's customer ID.
     * @param carID Order's car plate
     * @param estimatedExitTime Order's estimated exit time
     * @param parkingLotNumber parking lot for order
     */
    public Order(int customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber) {
        this._customerID = customerID;
        this._carID = carID;
        this._estimatedExitTime = estimatedExitTime;
        this._actualEntryTime = new Date();
        this._estimatedEntryTime = this._actualEntryTime;
        this._parkingLotNumber = parkingLotNumber;
        this._orderStatus = OrderStatus.PRE_ORDER;

    }

    /**
     * Ctor from db entry
     * @param orderID
     * @param customerID
     * @param carID
     * @param parkingLotNumber
     * @param _orderStatus
     * @param entryTimeEstimated
     * @param entryTimeActual
     * @param estimatedExitTime
     * @param actualExitTime
     * @param price
     * @param creationTime
     */
    public Order(int orderID, int customerID, Integer carID, Integer parkingLotNumber, OrderStatus _orderStatus,
                 Date entryTimeEstimated, Date entryTimeActual, Date estimatedExitTime, Date actualExitTime,
                 double price, Date creationTime) {
        this._orderID = orderID;
        this._customerID = customerID;
        this._carID = carID;
        this._orderStatus = _orderStatus;
        this._estimatedEntryTime = entryTimeEstimated;
        this._actualEntryTime = entryTimeActual;
        this._estimatedExitTime = estimatedExitTime;
        this._actualExitTime = actualExitTime;
        this._parkingLotNumber = parkingLotNumber;
        this._price = price;
        this._creationTime = creationTime;
        // why is that?
        //this._orderStatus = OrderStatus.PRE_ORDER;

    }

    public Order(Order other) {
        this._customerID = other._customerID;
        this._parkingLotNumber = other._parkingLotNumber;
        this._orderStatus = other._orderStatus;
        this._estimatedEntryTime = other._estimatedEntryTime;
        this._actualEntryTime = other._actualEntryTime;
        this._actualExitTime = other._actualExitTime;
        this._carID = other._carID;
        this._estimatedExitTime = other._estimatedExitTime;
        this._price = other._price;
        this._orderID = other._orderID;
        // why is that?
        //this._orderStatus = OrderStatus.PRE_ORDER;
    }

    /**
     * Getters and Setters
     */

    public int getCostumerID() {
        return _customerID;
    }

    public void setCostumerID(int customerID) {
        this._customerID = customerID;
    }

    public Integer getCarID() {
        return _carID;
    }

    public void setCarID(Integer carID) {
        this._carID = carID;
    }

    public Date getActualEntryTime() {
        return _actualEntryTime;
    }

    public void setActualEntryTime(Date entryTime) {
        this._actualEntryTime = entryTime;
    }

    public Date getEstimatedEntryTime() {
        return _estimatedEntryTime;
    }

    public void setEstimatedEntryTime(Date estimatedEntryTime) {
        this._estimatedEntryTime = estimatedEntryTime;
    }

    public Date getEstimatedExitTime() { return _estimatedExitTime; }

    public void setEstimatedExitTime(Date estimatedExitTime) { this._estimatedExitTime = estimatedExitTime; }

    public Date getActualExitTime() {
        return _actualExitTime;
    }

    public void setActualExitTime(Date actualExitTime) {
        this._actualExitTime = actualExitTime;
    }

    public Integer getParkingLotNumber() {
        return _parkingLotNumber;
    }

    public void setParkingLotNumber(Integer parkingLotNumber) {
        this._parkingLotNumber = parkingLotNumber;
    }

    public Integer getParkingSpaceHeight() {
        return _parkingSpaceHeight;
    }

    public void setParkingSpaceHeight(Integer parkingSpaceHeight) {
        this._parkingSpaceHeight = parkingSpaceHeight;
    }

    public Integer getParkingSpaceWidth() {
        return _parkingSpaceWidth;
    }

    public void setParkingSpaceWidth(Integer parkingSpaceWidth) {
        this._parkingSpaceWidth = parkingSpaceWidth;
    }

    public Integer getParkingSpaceDepth() {
        return _parkingSpaceDepth;
    }

    public void setParkingSpaceDepth(Integer parkingSpaceDepth) {
        this._parkingSpaceDepth = parkingSpaceDepth;
    }

    public double getPrice() {
        return _price;
    }

    public void setPrice(double price) {
        this._price = price;
    }

    public int getOrderID() {
        return _orderID;
    }

    public void setOrderID(int orderID) {
        this._orderID = orderID;
    }

    public Date getCreationTime() {
        return _creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this._creationTime = creationTime;
    }

    public OrderStatus getOrderStatus() {
        return _orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this._orderStatus = orderStatus;
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
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return _customerID == order._customerID &&
                Double.compare(order._price, _price) == 0 &&
                Objects.equals(_carID, order._carID) &&
                Objects.equals(_actualEntryTime, order._actualEntryTime) &&
                Objects.equals(_estimatedEntryTime, order._estimatedEntryTime) &&
                Objects.equals(_estimatedExitTime, order._estimatedExitTime) &&
                Objects.equals(_actualExitTime, order._actualExitTime) &&
                Objects.equals(_parkingLotNumber, order._parkingLotNumber) &&
                Objects.equals(_orderStatus, order._orderStatus) &&
                Objects.equals(_creationTime, order._creationTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_carID, _actualEntryTime, _estimatedEntryTime, _estimatedExitTime, _actualExitTime, _parkingLotNumber, _customerID, _price);
    }

    @Override
    public String toString() {
        return "Order's details are:\n" +
                "user's Id=" + _customerID +
                ", car's ID=" + _carID +
                ", Actual entry time=" + _actualEntryTime +
                ", Estimated entry time=" + _estimatedEntryTime +
                ", estimated exit time=" + _estimatedExitTime +
                ", actual exit time=" + _actualExitTime +
                ", parking lot number=" + _parkingLotNumber +
                ", price=" + _price +
                ", creation time=" + _creationTime +
                ", current status="+ _orderStatus
                ;
    }

    public String toGUIString()
    {
        return "Order details:\n" +
                "\tCustomer ID: " + this.getCostumerID() + "\n" +
                "\tOrder ID: " + this.getOrderID() +"\n" +
                "\tParking Lot ID: " + this.getParkingLotNumber() +"\n" +
                "\tParking Start: " + this.getEstimatedEntryTime() + "\n" +
                "\tEstimated Exit: " + this.getEstimatedExitTime();
    }

}