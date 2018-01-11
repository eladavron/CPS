package controller;

import entity.Billing.priceList;
import entity.Order;
import entity.PreOrder;

import java.util.Date;
import java.util.Random; //TODO: remove after setting proper order ID from DB
import java.util.HashMap;
import java.util.Map;
public class OrderController {

    //TODO: remove this or change thing to be taken from DB once its added to our system.
    private Map<Integer,Order> _ordersList;

    private static OrderController instance;

    /**
     * A private Constructor prevents any other class from
     * instantiating.
     */
    private OrderController() {
        this._ordersList = new HashMap<>();
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

    /**
     * will temporarly search in _ordersList TODO: move the search to the DB once implemented
     * @param orderID
     * @return
     */
    public Order getOrder(Integer orderID)
    {
        return _ordersList.get(orderID);
    }

    // TODO: for testing purposes makeNewSimpleOrder will send back the order...needs to be a void function once there is a database.
    public Order makeNewSimpleOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber){
        Order newOrder = new Order(customerID, carID, estimatedExitTime, parkingLotNumber);
        _ordersList.put(newOrder.getOrderID(), newOrder);
        return newOrder;
    }


    /**
     * Places a new preorder by using data from param
     * @param preOrder preorder object which includes needed params
     * @return A new order
     */
    public Order makeNewPreOrder(PreOrder preOrder) {
        return makeNewPreOrder(preOrder.getCostumerID(),
                preOrder.getCarID(),
                preOrder.getEstimatedExitTime(),
                preOrder.getParkingLotNumber(),
                preOrder.getEstimatedEntryTime());
    }

    /**
     * Places a new preorder according to params
     * @param customerID Customer's ID
     * @param carID Car ID
     * @param estimatedExitTime Estimated exit time from parking lot
     * @param parkingLotNumber Number of Parking Lot to enter/exit
     * @param estimatedEntryTime Estimated entry time to parking lot
     * @return A new order
     */
    //TODO : After entering with the car into the parking lot the entry time of Order (super) should be set!)
    public Order makeNewPreOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber, Date estimatedEntryTime){
        Double charge = BillingController.getInstance().calculateParkingCharge(estimatedEntryTime, estimatedExitTime, priceList.PRE_ORDER_ONE_TIME_PARKING);
        Order newPreOrder = new PreOrder(customerID, carID, estimatedExitTime ,parkingLotNumber, charge, estimatedEntryTime);
        newPreOrder.setOrderID(new Random().nextInt());
        _ordersList.put(newPreOrder.getOrderID(), newPreOrder);
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