package controller;

import Exceptions.LastCarRemovalException;
import Exceptions.OrderNotFoundException;
import entity.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import entity.Billing;

/**
 * Singleton Customer controller to be responsible over the methods of customer.
 */
public class CustomerController {
    private static CustomerController instance = new CustomerController();

    public static CustomerController getInstance() {
        return instance;
    }

    private CustomerController() {
    }

    //TODO: remove this or change thing to be taken from DB once its added to our system.
    private Map<Integer, Customer> _customersList = new HashMap<>();


    /**
     * will temporarly search in _customersList TODO: move the search to the DB once implemented
     * @param customerID
     * @return
     */
    public Customer getCustomer(Integer customerID)
    {
        return _customersList.get(customerID);
    }


    public Customer addNewCustomer(Integer uID, String name, String email, ArrayList<Integer> carIDList){
        Customer newCustomer = new Customer(uID, name, email, carIDList);
        _customersList.put(newCustomer.getUID(),newCustomer);
        return newCustomer;
    }

    /**
     *  Given the the right params needed for a new Order...controller will add this new order into the active orders list.
     * @param customerID
     * @param carID
     * @param estimatedExitTime
     * @param parkingLotNumber
     */
    public Order addNewOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber){
        Customer customer = getCustomer(customerID);
        Order newOrder = OrderController.getInstance().makeNewSimpleOrder(customerID, carID, estimatedExitTime,  parkingLotNumber);
        Map<Integer, Order> activeOrders = customer.getActiveOrders();
        activeOrders.put(newOrder.getOrderID(), newOrder);
        customer.setActiveOrders(activeOrders);
        return newOrder;
    }

    /**
     * OverLoading function for a given template of an Order.
     * @param newOrder
     */
    public Order addNewOrder(Order newOrder){
        return addNewOrder(newOrder.getCostumerID(), newOrder.getCarID(), newOrder.getEstimatedExitTime(), newOrder.getParkingLotNumber());
    }

    /**
     *  Overloaded function of the above just for PreOrder making.
     * @param customerID
     * @param carID
     * @param estimatedExitTime
     * @param parkingLotNumber
     * @param estimatedEntryTime
     * @return order
     */
    public Order addNewOrder(Integer customerID, Integer carID, Date estimatedExitTime, Integer parkingLotNumber, Date estimatedEntryTime){
        Customer customer = getCustomer(customerID);
        Order newOrder = OrderController.getInstance().makeNewPreOrder(customerID, carID, estimatedExitTime,  parkingLotNumber, estimatedEntryTime);
        Map<Integer, Order> activeOrders = customer.getActiveOrders();
        activeOrders.put(newOrder.getOrderID(),newOrder);
        customer.setActiveOrders(activeOrders);
        return newOrder;
    }

    /**
     * OverLoading function for a given template of a PreOrder.
     * @param newPreOrder
     */
    public Order addNewOrder(PreOrder newPreOrder){
        return addNewOrder(newPreOrder.getCostumerID(), newPreOrder.getCarID(), newPreOrder.getEstimatedExitTime(), newPreOrder.getParkingLotNumber(), newPreOrder.getEstimatedEntryTime());
    }

    /**
     *  Delete an un-wanted order.
     */
    public void removeOrder(Customer customer, Integer orderID){
        Map<Integer, Order> activeOrders = customer.getActiveOrders();
        if (activeOrders.containsKey(orderID)) {
                activeOrders.remove(orderID);
                customer.setActiveOrders(activeOrders);
        }
    }

    /**
     * Finish an order (exiting with the car)
     * Throws order not found if we cant find it.
     * Return: the price for the Customer to pay (by calling BillingController)
     */
    public double finishOrder(Customer customer,Integer orderID) throws OrderNotFoundException {
        Map<Integer, Order> activeOrders = customer.getActiveOrders();
        Order orderToFinish;
        if (activeOrders.containsKey(orderID)) {
            orderToFinish = activeOrders.get(orderID);
            // The order was found...need to check the client's cost for this order.
            Integer carID = orderToFinish.getCarID();
            Billing.priceList checkedPrice = getHourlyParkingCost(carID, customer.getSubscriptionList(), orderToFinish);
            OrderController.getInstance().finishOrder(orderToFinish,checkedPrice);
            return orderToFinish.getPrice();
        }
        else{ throw new OrderNotFoundException(orderID);}
    }

    /**
     *  Private method to be used in finishOrder method, will check if the user has any subscription THAT MATCHES this order.
     * @param carID
     * @param subscriptionsList
     * @param orderToFinish
     * @return
     */
    private Billing.priceList getHourlyParkingCost(Integer carID, Map<Integer, Subscription> subscriptionsList, Order orderToFinish){
        Billing.priceList price;
        price = Billing.priceList.ONE_TIME_PARKING;
        Integer existingSubscriptionID = SubscriptionController.getInstance().findSubscriptionByCarID(subscriptionsList, carID);

        if (existingSubscriptionID > 0)
        {
            Subscription current = subscriptionsList.get(existingSubscriptionID);
            if (current.getSubscriptionType() == Subscription.SubscriptionType.REGULAR)
            { // There is a a subscription listed on this car but need to check if its of the same parkingLot as the order's.
                RegularSubscription regularSubscription = (RegularSubscription) current;
                if (orderToFinish.getParkingLotNumber().equals(regularSubscription.getParkingLotNumber()) && validateRegularTimes(regularSubscription, orderToFinish))
                { // The we have a RegularSubscription!
                    price = Billing.priceList.NO_CHARGE_DUE_TO_SUBSCRIPTION;
                }
                else{ price = Billing.priceList.PRE_ORDER_ONE_TIME_PARKING;}
            }
            else
            { // Full subscription is listed on this car.
                price = Billing.priceList.NO_CHARGE_DUE_TO_SUBSCRIPTION;
            }
        }

        return price;
    }

    /**
     * private method used in order to make sure this order actualy fits the subscription regularly hours.
     * Assuming here that we DO NOT calculate for him some of the regular and some outside...
     * if he is somewhat outside he is all outside in our account.
     * @param regularSubscription
     * @param orderToFinish
     * @return true if it does, false otherwise.
     */
    private boolean validateRegularTimes(RegularSubscription regularSubscription, Order orderToFinish) {
        return (regularSubscription.getRegularEntryTime().compareTo(orderToFinish.getEntryTime()) <= 0)
                && regularSubscription.getRegularExitTime().compareTo(orderToFinish.getActualExitTime()) >= 0;
    }

    /**
     *  Add another car to this customer.
     * @param customer
     * @param carID
     */
    public void addCar(Customer customer, Integer carID)
    {
        customer.getCarIDList().add(carID);
    }

    /**
     * @param carID     * Remove carID from this customer, will fail if this is the last car on his list!
     * @param customer

     */
    public void removeCar(Customer customer, Integer carID) throws LastCarRemovalException {
        ArrayList<Integer> carList = customer.getCarIDList();
        if (carList.contains(carID)) {
            if (carList.size() > 1) {
                customer.getCarIDList().remove(carID);
            }
            else { // Trying to remove the customer's last car!
                throw new LastCarRemovalException("Customer : " + customer.getName() + "Car ID:" + carID);
            }
        }
    }

    /**
     *   Given the the right params needed for a new Regular Subscription
     *   the controller will add subscribe it and put it into the the customer's subscriptionList.
     * @param customer
     * @param carID
     * @param expiration
     * @param regularEntryTime
     * @param regularExitTime
     * @param parkingLotNumber
     * @return 0 = Customer has his car subscribed on Full already, 1 = Added/Success, 2 = Had Regular
     */
    public Integer addNewRegularSubscription(Customer customer, Integer carID, Date expiration, Date regularEntryTime, Date regularExitTime, Integer parkingLotNumber)
    {
        Map<Integer, Subscription> subscriptionList = customer.getSubscriptionList();
        switch (this.addSubscription(customer, carID, parkingLotNumber)){
            case HAS_FULL :
            {// Customer is already has a full subscription we will do nothing.
                return 0;
            }
            case CAN_ADD_THIS :
            {// Customer doesn't have any for this car...add it!
                Subscription newRegularSubscription = SubscriptionController.getInstance()
                    .addRegularSubscription(carID, expiration, regularEntryTime, regularExitTime, parkingLotNumber);
                subscriptionList.put(newRegularSubscription.getSubscriptionID(), newRegularSubscription);
                return 1;
            }
            case CHANGED_TO_FULL:
            {// Customer had regular on a different parkingLot and was changed to full (to reduce costs), still has to pay the remaining differences
                return 2;
            }
        }

        return 0;
    }

    /**
     *  Same as addNewRegularSubscription just on FullSubscription this time.
     * @param customer
     * @param carID
     * @param expiration
     * @param parkingLotNumber
     * @return
     */
    public Integer addNewFullSubscription(Customer customer, Integer carID, Date expiration, Integer parkingLotNumber)
    {
        Map<Integer, Subscription> subscriptionList = customer.getSubscriptionList();
        switch (this.addSubscription(customer, carID, parkingLotNumber)){
            case HAS_FULL :
            {// Customer is already has a full subscription we will do nothing.
                return 0;
            }
            case CAN_ADD_THIS :
            {// Customer doesn't have any for this car...add it!
                Subscription newFullSubscription = SubscriptionController.getInstance()
                    .addFullSubscription(carID, expiration);
                subscriptionList.put(newFullSubscription.getSubscriptionID(), newFullSubscription);
                return 1;
            }
            case CHANGED_TO_FULL:
            {// Customer had regular and was changed to full!
                return 2;
            }
        }

        return 0;
    }

    private enum SubscriptionStates{HAS_FULL, CAN_ADD_THIS, CHANGED_TO_FULL}

    /**
     * private method used by addNewFullSubscription and addNewRegularSubscription in order to decide what to do
     * with the subscription they want to create.
     * return HAS_FULL = Customer has his car subscribed on Full already ( meaning no change is needed ), CAN_ADD_THIS = Add this to list, CHANGED_TO_FULL = Had Regular Changing to full here!.
     */
    private SubscriptionStates addSubscription( Customer customer, Integer carID, Integer parkingLotNumber) {
        SubscriptionStates state;
        Map<Integer, Subscription> subscriptionList = customer.getSubscriptionList();
        // Since subscriptionList is mapped by subscriptionID we will have to search each one for carID to see if we have a match.
        Integer existingSubscriptionID = SubscriptionController.getInstance().findSubscriptionByCarID(subscriptionList, carID);
        if (existingSubscriptionID > 0) {// then there is a subscription on this car already.
            Subscription current = subscriptionList.get(existingSubscriptionID);
            if (current.getSubscriptionType() == Subscription.SubscriptionType.REGULAR) {
                RegularSubscription currentRegular = (RegularSubscription) current;
                if (!currentRegular.getParkingLotNumber().equals(parkingLotNumber))
                    //TODO: replace to Full? or suggest doing so?...will currently just replace to full and charge for the remaining cost.
                    subscriptionList.remove(existingSubscriptionID);
                    Subscription newFullSubscription = SubscriptionController.getInstance().addFullSubscription(current.getCarID(),new Date());
                    SubscriptionController.getInstance().renewSubscription(newFullSubscription);
                    subscriptionList.put(newFullSubscription.getSubscriptionID(), newFullSubscription);
                    state = SubscriptionStates.CHANGED_TO_FULL;
            } else {
                //already on Full subscription with this car.
                // TODO : can also suggest Renewal or ignore?
                state = SubscriptionStates.HAS_FULL;
            }
        }
        else
        {//there isn't any subscription on this car already. so we just add it.
            state = SubscriptionStates.CAN_ADD_THIS;
        }
        return state;
    }

}