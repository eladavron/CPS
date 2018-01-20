package controller;

import Exceptions.CustomerNotificationFailureException;
import entity.Billing;
import entity.Billing.priceList;
import entity.Customer;
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

/**
 * An Order Controller for Orders and Preorders.
 * This is a Singleton class.
 */
public class OrderController {

    /**
     * This holds either Order or Preorder, but we had to use "Object" as value because polymorphism doesn't work well.
     * To resolve this, only ever use the supplied "getOrder()" or "getPreOrder()" methods and never access this directly!
     */
    private Map<Integer, Object> _ordersList;

    private static OrderController instance;

    /**
     * Returns the map of orders mapped by their IDs.
     * @return
     */
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

    /**
     * Gets a preorder by its ID.
     * @param orderID
     * @return
     */
    public PreOrder getPreOrder(Integer orderID)
    {
        Order thisOrder = (Order) _ordersList.get(orderID);
        if (thisOrder.getOrderStatus().equals(PRE_ORDER))
            return (PreOrder) thisOrder;
        return null;
    }

    /**
     * Returns a list of ALL Preorders.
     * @return
     */
    public ArrayList<PreOrder> getAllPreOrders()
    {
        ArrayList<PreOrder> allPreOrdersList = new ArrayList<PreOrder>();
        for (Object orderObj:_ordersList.values()){
            if (((Order) orderObj).getOrderStatus().equals(PRE_ORDER))
                allPreOrdersList.add((PreOrder) orderObj);
        }
        return allPreOrdersList;
    }

    /**
     * Creates a new "Simple" order (A Parking Session).
     * @param customerID The ID of the customer creating the order.
     * @param carID The car ID.
     * @param estimatedExitTime The time they estimate the will leave.
     * @param parkingLotNumber The parking lot ID it's in.
     * @return
     * @throws SQLException
     * @throws CustomerNotificationFailureException
     */
    public Order makeNewSimpleOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber) throws SQLException, CustomerNotificationFailureException {
        Order newOrder = new Order(customerID, carID, estimatedExitTime, parkingLotNumber);
        newOrder.setEstimatedEntryTime(newOrder.getActualEntryTime());
        //First we check if the Customer can actually park this car in the parking in the wanted hours:
        //UID is select within the dbController and then set in it as well.
        newOrder.setOrderStatus(Order.OrderStatus.IN_PROGRESS);
        if (!checkIfAvailable(newOrder))
        {
            throw new CustomerNotificationFailureException("Sorry, we can not park this car at this time on this parking lot," +
                    "due to lack of space.\n" +
                    "you should try some other time or a different parking-lot.");
        }
        dbController.insertOrder(newOrder, priceList.ONE_TIME_PARKING);
        _ordersList.put(newOrder.getOrderID(), newOrder);
        return newOrder;
    }

    /**
     * function will validate that there is indeed a space for this order
     * or if someone tries to enter with an order of his for this car.
     * @param orderToValidate to validate.
     * @return true, can continue...false otherwise.
     */
    private boolean checkIfAvailable(Order orderToValidate)
    {
        Order currentOrder;
        System.out.println("is order to validate is:" + orderToValidate);

       //First we check if this car has an order already. (trying to enter right now!:
        if (isThereAnOrderForThisEntrance(orderToValidate))
        {
            return true;
        }
        else
        { //Trying to make a future Order.
            Integer takenSpaces = 0;
            for (Object obj : _ordersList.values())
            {
                currentOrder = (Order) obj;
                if (currentOrder.getParkingLotNumber().equals(orderToValidate.getParkingLotNumber()))
                {//Then this order on the order list belong to the the wanted parking lot.
                    //now we need to check if the Time collides.
                    if (currentOrder.getOrderStatus().equals(Order.OrderStatus.IN_PROGRESS))
                    {//order is in use and has actual entry.
                        if(currentOrder.getEstimatedExitTime().getTime() >= (orderToValidate.getEstimatedEntryTime().getTime()))
                        { //Since we are always after the actual time, if we enter before his exit time...we collide otherwise we defently dont!.
                            takenSpaces++;
                        }
                    }
                    else
                    {
                        if (currentOrder.getOrderStatus().equals(Order.OrderStatus.PRE_ORDER))
                        {//Order is also a PreOrder.
                             if (validateWithPreOrder(currentOrder, orderToValidate))
                            {
                                takenSpaces++;
                            }
                        }
                    }
                }
            }
            //Reaching here means we have all the takenSpaces of orders that collide with us:
            if (parkingController.getParkingLotSize(orderToValidate.getParkingLotNumber()) > takenSpaces)
                return true;
        }
        return  false;
    }

    /**
     * private method to validate that the incoming Order is just a little adjustment to an existing one.
     * so we can change it to progress and tell the man he can go on and enter our lot.
     * @param orderToValidate - the order.
     * @return true- if changed to IN_PROGRESS and can continue, false - this order does not fit our existing orders.
     */
    private Boolean isThereAnOrderForThisEntrance(Order orderToValidate)
    {
        //We will only make sure the order is here and change his status,
        //If the time doesn't fit we will let the ParkingLotController decide if he actually has space,
        //Since we know for sure he does on his right times (from the function that uses this).
        if ( _ordersList.containsKey(orderToValidate.getOrderID()))
        {
            Order thisOrder = (Order)_ordersList.get(orderToValidate.getOrderID());
            if (thisOrder.getOrderStatus().equals(Order.OrderStatus.PRE_ORDER))
            {
                thisOrder.setOrderStatus(Order.OrderStatus.IN_PROGRESS);
                return true;
            }
        }
        return false;
    }

    /**
     * private method to shorten the length of the whole validate of incoming order.
     * @param currentOrder - an existing  pre-order in our list.
     * @param orderToValidate - the new "wanted" order.
     * @return true if collides.
     */
    private Boolean validateWithPreOrder(Order currentOrder, Order orderToValidate)
    {
        if(
                // Enters before but, also Exits after I enter.
                    currentOrder.getEstimatedEntryTime().getTime() <= (orderToValidate.getEstimatedEntryTime().getTime())
                    && currentOrder.getEstimatedExitTime().getTime() >= (orderToValidate.getEstimatedEntryTime().getTime())
                || // Enters after but, also Exits after I enter.
                    currentOrder.getEstimatedEntryTime().getTime() >= (orderToValidate.getEstimatedEntryTime().getTime())
                            && currentOrder.getEstimatedExitTime().getTime() <= (orderToValidate.getEstimatedExitTime().getTime())

                || //Enters after, exits after ,but also I enter before he exits.
                    currentOrder.getEstimatedEntryTime().getTime() >= (orderToValidate.getEstimatedEntryTime().getTime())
                            && currentOrder.getEstimatedExitTime().getTime() >= (orderToValidate.getEstimatedExitTime().getTime())
                            && currentOrder.getEstimatedEntryTime().getTime() <= orderToValidate.getEstimatedExitTime().getTime())
            return true;
        else
            return false;
    }


    /**
     * Places a new preorder by using data from param
     * @param preOrder preorder object which includes needed params
     * @return A new order
     */
    public Order makeNewPreOrder(PreOrder preOrder) throws SQLException, CustomerNotificationFailureException {
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
    public Order makeNewPreOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber, Date estimatedEntryTime) throws SQLException, CustomerNotificationFailureException {
        PreOrder newPreOrder = new PreOrder(customerID, carID, estimatedExitTime ,parkingLotNumber, 0, estimatedEntryTime);
        //First we check if the Customer can acctualy park this car in the parking in the wanted hours:
        if (!checkIfAvailable(newPreOrder))
        {
            throw new CustomerNotificationFailureException("Sorry, we can not Pre-Order at this time on this parking lot," +
                    "due to lack of space.\n" +
                    "you should try some other time or a different parking-lot.");
        }
        //Then we check with the CustomerController if this customer has some special price for this parking.
        priceList priceType = customerController.getHourlyParkingCost(customerID, newPreOrder);
        // Then we calculate the amount to be payed using the billingController and add it to the order.
        Double charge = billingController.calculateParkingCharge(estimatedEntryTime, estimatedExitTime, priceType);
        newPreOrder.setCharge(charge);
        //UID is select within the dbController and then set in it as well.
        dbController.insertOrder(newPreOrder, priceType);
        _ordersList.put(newPreOrder.getOrderID(), newPreOrder);
        return newPreOrder;
    }

    /**
     *  When a customer wants to finish his order (and move the car out) this function is called,
     *  with the current time as the exitTime of the customer and the order's price is calculated.
     * @param orderID : the customer's order to be finished
     * @param finalPrice : The price this order will charge per hour for.
     * @return order : the finished order with the final price updated
     */
    public Order finishOrder(Integer orderID, double finalPrice) throws SQLException {
        Order orderToFinish = getOrder(orderID);
        orderToFinish.setActualExitTime(new Date());

        if (dbController.finishOrder(orderToFinish.getOrderID(),orderToFinish.getActualExitTime(), finalPrice))
        { //If finishUpdate successful
            _ordersList.remove(orderToFinish.getOrderID());
        }
        return orderToFinish;
    }

    /**
     * handles estimated billing for charing upon placing a future order
     * @param customerID customer ID
     * @param preorder preorder
     * @return estimated parking price
     */
    public Double handleBillingUponPreOrderPlacement(Integer customerID, PreOrder preorder)
    {
        Billing.priceList priceType = customerController.getHourlyParkingCost(customerID, preorder);
        return billingController.calculateParkingCharge(preorder.getEstimatedEntryTime(), preorder.getEstimatedExitTime(), priceType);
    }

    /**
     * Handles final billing upon car departure
     * @param customer Customer object
     * @param orderToFinish order object
     * @return final order price
     */
    public Double handleFinalBillingUponDeparture(Customer customer, Order orderToFinish) throws SQLException {
        Billing.priceList priceType = customerController.getHourlyParkingCost(customer.getUID(), orderToFinish);
        double finalPrice = billingController.calculateParkingCharge(orderToFinish.getActualEntryTime(), orderToFinish.getActualExitTime(), priceType);
        if(orderToFinish.getEstimatedEntryTime().compareTo(orderToFinish.getActualEntryTime()) < 0 )
        {
            finalPrice = finalPrice*1.2;
        }
        orderToFinish.setPrice(finalPrice);
        return finalPrice - orderToFinish.getCharge();
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
        PreOrder orderToDelete      = getPreOrder(orderID);
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

    /**
     * Puts all orders from a given map to the general Order List.
     * @param activeOrders
     */
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
     * Sets the Order's parking coordinates in the parking lot.
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
     * Get an order's parking coordinates in the lot.
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