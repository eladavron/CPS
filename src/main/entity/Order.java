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
    private static int _orderUIDCounter = 0;

    private int _orderID;
    private Integer _carID;
    private Date _entryTime;
    private Date _estimatedExitTime;
    private Date _actualExitTime;
    private ParkingLotNumber _parkingLotNumber;
    private int _customerID;
    private double _price;

    /**
     * Constructor
     * @param costumerID Order's Name
     * @param carID Order's car plate
     * @param estimatedExitTime Order's estimated exit time
     * @param parkingLotNumber
     */
    public Order(int costumerID, Integer carID, Date estimatedExitTime, ParkingLotNumber parkingLotNumber) {
        this._orderID = _orderUIDCounter++;
        this._customerID = costumerID;
        this._carID = carID;
        this._estimatedExitTime = estimatedExitTime;
        this._entryTime = new Date();
        this._parkingLotNumber = parkingLotNumber;
    }

    public Order(Order other) {
        this._customerID = other._customerID;
        this._parkingLotNumber = other._parkingLotNumber;
        this._entryTime = other._entryTime;
        this._actualExitTime = other._actualExitTime;
        this._carID = other._carID;
        this._estimatedExitTime = other._estimatedExitTime;
        this._price = other._price;
        this._orderID = other._orderID;
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

    public static int getOrderUIDCounter() {
        return _orderUIDCounter;
    }

    public static void setOrderUIDCounter(int orderUIDCounter) {
        Order._orderUIDCounter = orderUIDCounter;
    }

    public Integer getCarID() {
        return _carID;
    }

    public void setCarID(Integer carID) {
        this._carID = carID;
    }

    public Date getEntryTime() {
        return _entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this._entryTime = entryTime;
    }

    public Date getEstimatedExitTime() {
        return _estimatedExitTime;
    }

    public void setEstimatedExitTime(Date estimatedExitTime) {
        this._estimatedExitTime = estimatedExitTime;
    }

    public Date getActualExitTime() {
        return _actualExitTime;
    }

    public void setActualExitTime(Date actualExitTime) {
        this._actualExitTime = actualExitTime;
    }

    public Integer getParkingLotNumber() {
        return _parkingLotNumber.getParkingLotNumber();
    }

    public void setParkingLotNumber(ParkingLotNumber parkingLotNumber) {
        this._parkingLotNumber = parkingLotNumber;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return _customerID == order._customerID &&
                Double.compare(order._price, _price) == 0 &&
                Objects.equals(_carID, order._carID) &&
                Objects.equals(_entryTime, order._entryTime) &&
                Objects.equals(_estimatedExitTime, order._estimatedExitTime) &&
                Objects.equals(_actualExitTime, order._actualExitTime) &&
                Objects.equals(_parkingLotNumber, order._parkingLotNumber);
    }

    @Override
    public int hashCode() {

        return Objects.hash(_carID, _entryTime, _estimatedExitTime, _actualExitTime, _parkingLotNumber, _customerID, _price);
    }

    @Override
    public String toString() {
        return "Order's details are:\n" +
                "user's Id=" + _customerID +
                ", car's ID=" + _carID +
                ", entry time=" + _entryTime +
                ", estimated exit time=" + _estimatedExitTime +
                ", actual exit time=" + _actualExitTime +
                ", parking lot number=" + _parkingLotNumber.toString()+
                ", price=" + _price
                ;
    }

}
