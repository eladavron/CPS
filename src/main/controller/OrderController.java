package controller;

import entity.Billing.priceList;
import entity.Order;
import entity.PreOrder;

import java.util.*;
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
        getOrdersFromDb();
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
        newOrder.setOrderStatus(Order.orderStatus.IN_PROGRESS);
        //UID is select within the dbController and then set in it as well.
        dbController.InsertOrder(newOrder);
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
        Double charge = BillingController.getInstance().calculateParkingCharge(estimatedEntryTime, estimatedExitTime, priceList.PRE_ORDER_ONE_TIME_PARKING);
        Order newPreOrder = new PreOrder(customerID, carID, estimatedExitTime ,parkingLotNumber, charge, estimatedEntryTime);
        //UID is select within the dbController and then set in it as well.
        dbController.InsertOrder(newPreOrder);
        _ordersList.put(newPreOrder.getOrderID(), newPreOrder);
        return newPreOrder;
    }

    public Order makeOrderFromDb(int orderID, int customerID, Integer carID, Integer parkingLotNumber, Date entryTime, Date estimatedExitTime, Date actualExitTime, double price, Date creationTime){
        Order orderFromDb = new Order(orderID, customerID,  carID,  parkingLotNumber,  entryTime,  estimatedExitTime,  actualExitTime,  price,  creationTime);
        _ordersList.put(orderFromDb.getOrderID(), orderFromDb);
        return orderFromDb;
    }

    public void getOrdersFromDb(){
        setOrdersList(dbController.getOrdersByID(-1));
    }

    //TODO: is this needed along with getOrdersFromDb()?
    public ArrayList<Order> getOrdersList() {
        return (ArrayList<Order>) _ordersList.values();
    }

    public void setOrdersList(ArrayList<Order> list) {
        list.forEach(order -> _ordersList.put(order.getOrderID(), order));
    }

//    TODO: decide later on if to keep or not since its unused...will stay here for now.
//    /**
//     * Given a new estimated entry time, this func will update the order's entry.
//     * assuming here the only PreOrder class has estimated entry time.
//     * @param order : to change its time
//     * @param estimatedEntryTime : the new entry time.
//     * @return order with the new entry time updated.
//     */
//    public Order changeEstimatedEntryTimeOfOrder(PreOrder order, Date estimatedEntryTime){
//        order.setEstimatedEntryTime(estimatedEntryTime);
//        return order;
//    }

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
        order.setPrice(BillingController.getInstance().calculateParkingCharge(order.getEntryTime(), order.getActualExitTime(), priceType));
        order.setOrderStatus(Order.orderStatus.FINISHED);
        //TODO: dbcontroller.removeOrder(orderID). and then dbcontroller.insertOrder(order) with its final stats as we discussed.
        _ordersList.remove(order.getOrderID());
        return order;
    }

    /**
     * removes the Order from the list AFTER updating it as deleted in the DB.
     * @param orderID
     */
    public void deleteOrder(Integer orderID)
    {
        _ordersList.get(orderID).setOrderStatus(Order.orderStatus.DELETED);
        //TODO: dbcontroller.removeOrder(orderID). and then dbcontroller.insertOrder(order) with its final stats as we discussed.
        _ordersList.remove(orderID);
    }
}