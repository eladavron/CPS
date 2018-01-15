package controller;

import entity.Billing.priceList;
import entity.Order;
import entity.PreOrder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.*;

public class OrderController {

    //TODO: remove this or change thing to be taken from DB once its added to our system.
    private Map<Integer,Order> _ordersList;

    private static OrderController instance;

    /**
     * A private Constructor prevents any other class from
     * instantiating.
     * we assume here there is a live connection to the DB.
     */
    private OrderController() {
        this._ordersList = new HashMap<>();
        System.out.print("\tLooking for all the Orders...");
        getOrdersFromDb();
        System.out.println("Found them!");
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
        //UID is select within the dbController and then set in it as well.
        newOrder.setOrderStatus(Order.OrderStatus.IN_PROGRESS);
        dbController.insertOrder(newOrder);
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
     * Places a new pre order according to params
     * @param customerID Customer's ID
     * @param carID Car ID
     * @param estimatedExitTime Estimated exit time from parking lot
     * @param parkingLotNumber Number of Parking Lot to enter/exit
     * @param estimatedEntryTime Estimated entry time to parking lot
     * @return A new order
     */
    //TODO : After entering with the car into the parking lot the entry time of Order (super) should be set!)
    public Order makeNewPreOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber, Date estimatedEntryTime){
        Order newPreOrder = new PreOrder(customerID, carID, estimatedExitTime ,parkingLotNumber, 0, estimatedEntryTime);
        //First we check with the CustomerController if this customer has some special price for this parking.
        priceList priceType = customerController.getHourlyParkingCost(customerID, newPreOrder);
        // Then we calculate the amount to be payed using the billingController and add it to the order.
        Double charge = billingController.calculateParkingCharge(estimatedEntryTime, estimatedExitTime, priceType);
        newPreOrder.setPrice(charge);
        //UID is select within the dbController and then set in it as well.
        dbController.insertOrder(newPreOrder);
        _ordersList.put(newPreOrder.getOrderID(), newPreOrder);
        return newPreOrder;
    }

    // TODO: someone uses this?
    public Order makeOrderFromDb(int orderID, int customerID, Integer carID, Integer parkingLotNumber, Order.OrderStatus orderStatus, Date entryTimeEstimated, Date entryTimeActual, Date estimatedExitTime, Date actualExitTime, double price, Date creationTime){
        Order orderFromDb = new Order(orderID, customerID,  carID,  parkingLotNumber, orderStatus, entryTimeEstimated,  entryTimeActual,  estimatedExitTime,  actualExitTime,  price,  creationTime);
        _ordersList.put(orderFromDb.getOrderID(), orderFromDb);
        return orderFromDb;
    }

    public void getOrdersFromDb() {
        this._ordersList.putAll(dbController.getAllOrders());
    }

    //TODO : Add different options to reach the specific order maybe using the customer's profile or so.
    /**
     *  When a customer wants to finish his order (and move the car out) this function is called,
     *  with the current time as the exitTime of the customer and the order's price is calculated.
     * @param orderID : the customer's order to be finished
     * @param priceType : The price this order will charge per hour for.
     * @return order : the finished order with the final price updated
     */
    public Order finishOrder(Integer orderID, priceList priceType){
        Order order = _ordersList.get(orderID);
        order.setActualExitTime(new Date());
        order.setPrice(billingController.calculateParkingCharge(
                order.getActualEntryTime(), order.getActualExitTime(), priceType)
                - order.getPrice()
        );
        order.setOrderStatus(Order.OrderStatus.FINISHED);
        //TODO : update order params to match final order. ->DBController
        //TODO: _ordersList.remove(order.getOrderID()); will stay on the list (for today) in order to make sure Regulars arent used twice.
        return order;
    }

    /**
     * removes the Order from the list AFTER updating it as deleted in the DB.
     * @param orderID
     */
    public Order deleteOrder(Integer orderID)
    {
        final Integer THREE_HOURS = 10800000;
        final Integer ONE_HOUR = 3600000;
        PreOrder orderToDelete = (PreOrder) _ordersList.get(orderID);
        final Integer REMAINING_TIME = orderToDelete.getEstimatedEntryTime().compareTo(new Date()) ;
        double refund;
        double actualPayment;
        //First we will check how long until the order is set to be used.
        if (REMAINING_TIME > THREE_HOURS)
        {
            // Pay 10% of the price meaning she will get back 90%
            refund = (orderToDelete.getCharge() / 10) * 9;
            actualPayment = orderToDelete.getCharge() / 10;
        }
        else
        { //Under 3 hours!
            if (REMAINING_TIME > ONE_HOUR)
            {
                // Pay 50% of the price meaning she will get back 50%
                refund = orderToDelete.getCharge() / 2;
                actualPayment = orderToDelete.getCharge() / 2;
            }
            else
            {
                //Pay 100% of the price meaning she will get nothing back!
                refund = 0;
                actualPayment = orderToDelete.getCharge();
            }
        }
        orderToDelete.setCharge(refund);
        orderToDelete.setOrderStatus(Order.OrderStatus.DELETED);
        dbController.deleteOrder(orderToDelete.getOrderID(), actualPayment);
        _ordersList.remove(orderID);
        return orderToDelete;
    }
}