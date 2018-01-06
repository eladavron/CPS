package controller;

import entity.Billing.priceList;
import entity.Order;
import entity.PreOrder;

import java.util.ArrayList;
import java.util.Date;

public class OrderController {

    //TODO: remove this or change thing to be taken from DB once its added to our system.
    private ArrayList<Order> _ordersList;

    private static OrderController instance;

    /**
     * A private Constructor prevents any other class from
     * instantiating.
     */
    private OrderController() {
        this._ordersList = new ArrayList<>();
    }

    /**
     * The Static initializer constructs the instance at class
     * loading time; this is to simulate a more involved
     * construction process (it it were really simple, you'd just
     * use an initializer)
     */
    static {
        instance = new OrderController();
    }

    /** Static 'instance' method */
    public static OrderController getInstance() {
        return instance;
    }

    // TODO: for testing purposes makeNewSimpleOrder will send back the order...needs to be a void function once there is a database.
    public Order makeNewSimpleOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber){
        Order newOrder = new Order(customerID, carID, estimatedExitTime, parkingLotNumber);
        _ordersList.add(newOrder);
        return newOrder;
    }

    //TODO : After entering with the car into the parking lot the entry time of Order (super) should be set!)
    public Order makeNewPreOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber, Date estimatedEntryTime){
        Double charge = BillingController.getInstance().calculateParkingCharge(estimatedEntryTime, estimatedExitTime, priceList.PRE_ORDER_ONE_TIME_PARKING);
        Order newPreOrder = new PreOrder(customerID, carID, estimatedExitTime ,parkingLotNumber, charge, estimatedEntryTime);
        _ordersList.add(newPreOrder);
        return newPreOrder;
    }

    /**
     * Given a new estimated entry time, this func will update the order's entry.
     * assuming here the only PreOrder class has estimated entry time.
     * @param order
     * @param estimatedEntryTime : the new entry time.
     * @return order with the new entry time updated.
     */
    public Order changeEstimatedEntryTimeOfOrder(PreOrder order, Date estimatedEntryTime){
        order.setEstimatedEntryTime(estimatedEntryTime);
        return order;
    }

    //TODO : Add different options to reach the specific order maybe using the customer's profile or so.
    /**
     *  When a customer wants to finish his order (and move the car out) this function is called,
     *  with the current time as the exitTime of the customer and the order's price is calculated.
     * @param order : the customer's order to be finished
     * @return order : the finished order with the final price updated
     */
    public Order finishOrder(Order order, priceList priceType){
        order.setActualExitTime(new Date());
        order.setPrice(BillingController.getInstance().calculateParkingCharge(order.getEntryTime(), order.getActualExitTime(), priceType));
        return order;
    }
}
