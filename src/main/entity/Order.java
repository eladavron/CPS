package entity;

import java.util.Date;

/**
 * A sample class of how an Order will look in the system.
 */

public class Order {
    /**
     * Private properties
     */
    private static int _orderUIDCounter = 0;

    private int _uId;

    public int getUId() {
        return _uId;
    }

    public void setUId(int UId) {
        this._uId = UId;
    }

    private String _customerName;

    public static int getOrderUIDCounter() {
        return _orderUIDCounter;
    }

    public static void setOrderUIDCounter(int orderUIDCounter) {
        Order._orderUIDCounter = orderUIDCounter;
    }

    public String getCustomerName() {
        return _customerName;
    }

    public void setCustomerName(String customerName) {
        this._customerName = customerName;
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

    private Integer _carID;
    private Date _entryTime;

    public static int get_orderUIDCounter() {
        return _orderUIDCounter;
    }

    public static void set_orderUIDCounter(int _orderUIDCounter) {
        Order._orderUIDCounter = _orderUIDCounter;
    }

    private Date _estimatedExitTime;
    private Date _actualExitTime;

    /**
     * Constructor
     * @param Name Order's Name
     * @param carID Order's car plate
     * @param estimatedExitTime Order's estimated exit time
     */
    public Order(String Name, Integer carID, Date estimatedExitTime) {
        this._uId = _orderUIDCounter++;
        this._customerName = Name;
        this._carID = carID;
        this._estimatedExitTime = estimatedExitTime;
        this._entryTime = new Date();
    }


    // TODO : Add compare using equalTo. (for testing).
}
