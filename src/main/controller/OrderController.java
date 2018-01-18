package controller;

import entity.Billing.priceList;
import entity.Order;
import entity.PreOrder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.*;
import static entity.Order.OrderStatus.PRE_ORDER;
import static utils.TimeUtils.HOURS_IN_MS;

public class OrderController {

    //TODO: remove this or change thing to be taken from DB once its added to our system.
    /**
     * This holds either Order or Preorder, but we had to use "Object" as value because polymorphism doesn't work well.
     * To resolve this, only ever use the supplied "getOrder()" or "getPreOrder()" methods and never access this directly!
     */
    private Map<Integer, Object> _ordersList;

    private static OrderController instance;

    public Map<Integer, Object> getOrdersMap() {
        return _ordersList;
    }

    /**
     * A private Constructor prevents any other class from
     * instantiating.
     * we assume here there is a live connection to the DB.
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
        return (Order) _ordersList.get(orderID);
    }

    public PreOrder getPreorder(Integer orderID)
    {
        Order thisOrder = (Order) _ordersList.get(orderID);
        if (thisOrder.getOrderStatus().equals(PRE_ORDER))
            return (PreOrder) thisOrder;
        return null;
    }

    public ArrayList<PreOrder> getAllPreOrders()
    {
        ArrayList<PreOrder> allPreOrdersList = new ArrayList<PreOrder>();
        for (Object orderObj:_ordersList.values()){
            if (((Order) orderObj).getOrderStatus().equals(PRE_ORDER))
                allPreOrdersList.add((PreOrder) orderObj);
        }
        return allPreOrdersList;
    }

    // TODO: for testing purposes makeNewSimpleOrder will send back the order...needs to be a void function once there is a database.
    public Order makeNewSimpleOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber)throws SQLException {
        Order newOrder = new Order(customerID, carID, estimatedExitTime, parkingLotNumber);
        newOrder.setEstimatedEntryTime(newOrder.getActualEntryTime());
        //UID is select within the dbController and then set in it as well.
        newOrder.setOrderStatus(Order.OrderStatus.IN_PROGRESS);
        dbController.insertOrder(newOrder, priceList.ONE_TIME_PARKING);
        _ordersList.put(newOrder.getOrderID(), newOrder);
        return newOrder;
    }


    /**
     * Places a new preorder by using data from param
     * @param preOrder preorder object which includes needed params
     * @return A new order
     */
    public Order makeNewPreOrder(PreOrder preOrder) throws SQLException {
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
    public Order makeNewPreOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber, Date estimatedEntryTime)throws SQLException{
        PreOrder newPreOrder = new PreOrder(customerID, carID, estimatedExitTime ,parkingLotNumber, 0, estimatedEntryTime);
        //First we check with the CustomerController if this customer has some special price for this parking.
        priceList priceType = customerController.getHourlyParkingCost(customerID, newPreOrder);
        // Then we calculate the amount to be payed using the billingController and add it to the order.
        Double charge = billingController.calculateParkingCharge(estimatedEntryTime, estimatedExitTime, priceType);
        newPreOrder.setCharge(charge);
        //UID is select within the dbController and then set in it as well.
        dbController.insertOrder(newPreOrder, priceType);
        _ordersList.put(newPreOrder.getOrderID(), newPreOrder);
        return newPreOrder;
    }

    // TODO: someone uses this?
    public Order makeOrderFromDb(int orderID, int customerID, Integer carID, Integer parkingLotNumber, Order.OrderStatus orderStatus, Date entryTimeEstimated, Date entryTimeActual, Date estimatedExitTime, Date actualExitTime, double price, Date creationTime){
        Order orderFromDb = new Order(orderID, customerID,  carID,  parkingLotNumber, orderStatus, entryTimeEstimated,  entryTimeActual,  estimatedExitTime,  actualExitTime,  price,  creationTime);
        _ordersList.put(orderFromDb.getOrderID(), orderFromDb);
        return orderFromDb;
    }

    public void getOrdersFromDb() throws SQLException {
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
        Order order = getOrder(orderID);
        order.setActualExitTime(new Date());
        double finalPrice = billingController.calculateParkingCharge(order.getActualEntryTime(), order.getActualExitTime(), priceType);
        if(order.getEstimatedEntryTime().compareTo(order.getActualEntryTime()) < 0 )
        {
            finalPrice = finalPrice*1.2;
        }
        if (order instanceof PreOrder)
        {
            order.setPrice(finalPrice - ((PreOrder) order).getCharge());
        }


        order.setOrderStatus(Order.OrderStatus.FINISHED);
        //TODO : update order params to match final order. ->DBController
        //TODO: _ordersList.remove(order.getOrderID()); will stay on the list (for today) in order to make sure Regulars arent used twice.
        return order;
    }

    /**
     *
     * @param orderID Order ID
     * @return Order that was deleted, or null if failed.
     */
    public Order deleteOrder(Integer orderID) throws SQLException
    {
        final long THREE_HOURS      = 3*HOURS_IN_MS;
        final long ONE_HOUR         = 1*HOURS_IN_MS;
        PreOrder orderToDelete      = getPreorder(orderID);
        final long REMAINING_TIME   = orderToDelete.getEstimatedEntryTime().getTime() - new Date().getTime() ;
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
        orderToDelete.setCharge(refund); // charge here is the ammount inially charged for.
        orderToDelete.setPrice(actualPayment); //price is Order's param of the finished cost of the order.
        orderToDelete.setOrderStatus(Order.OrderStatus.DELETED);
        if (dbController.deleteOrder(orderToDelete.getOrderID(), actualPayment))
        { //If deletion successful
            _ordersList.remove(orderID);
            return orderToDelete;
        }
        return null;
    }

    public void putAll(Map<Integer, Object> activeOrders)
    {
        for (Object obj : activeOrders.values())
        {
            Order order;
            if (obj instanceof PreOrder)
                order = (PreOrder) obj;
            else
                order = (Order) obj;
            this._ordersList.put(order.getOrderID(), order);
        }
        this._ordersList.putAll(activeOrders);
    }

    /**
     *
     * @param orderID
     * @param height
     * @param width
     * @param depth
     */
    public void setOrderHeightWidthDepth (Integer orderID,Integer height, Integer width, Integer depth){
        getOrder(orderID).setParkingSpaceHeight(height);
        getOrder(orderID).setParkingSpaceWidth(width);
        getOrder(orderID).setParkingSpaceDepth(depth);

    }

    /**
     *
     * @param orderID
     * @return
     */
    public ArrayList<Integer> getOrderHeightWidthDepth (Integer orderID){
        ArrayList<Integer> heightWidthDepth = new ArrayList<>();
        heightWidthDepth.add(getOrder(orderID).getParkingSpaceHeight());
        heightWidthDepth.add(getOrder(orderID).getParkingSpaceWidth());
        heightWidthDepth.add(getOrder(orderID).getParkingSpaceDepth());
        return heightWidthDepth;
    }

}