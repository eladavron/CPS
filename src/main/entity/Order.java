package entity;


import com.sun.xml.internal.bind.v2.TODO;

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
    private String _customerName;
    private Integer _carID;
    private Date _entryTime;
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

    // TODO: Add Lombok!
    // TODO : Add compare using equalTo. (for testing).
}
