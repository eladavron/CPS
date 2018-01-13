package controller;

import Exceptions.LastCarRemovalException;
import Exceptions.OrderNotFoundException;
import entity.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import entity.Billing;

import static controller.Controllers.*;

/**
 * Singleton Customer controller to be responsible over the methods of customer.
 */
public class CustomerController {
    private static CustomerController instance;

    static {
        instance = new CustomerController();
    }

    public static CustomerController getInstance() {
        return instance;
    }

    private CustomerController(){
        getCustomersFromDb();
    }

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

    public Customer getCustomerByEmail(String email) {
        for (Customer customer : _customersList.values()) {
            if (customer.getEmail().equals(email)) return customer;
        }
        return null;
    }

    public void getCustomersFromDb() {
        setCustomersList(dbController.getCustomers());
    }

    //TODO: is this needed along with getOrdersFromDb()?
    public ArrayList<Customer> getCustomersList() {
        return (ArrayList<Customer>) _customersList.values();
    }
    
    public void setCustomersList(ArrayList<Customer> list) {
        list.forEach(customer -> _customersList.put(customer.getUID(), customer));
    }


    public Customer addNewCustomer(Customer customer) {
        return addNewCustomer(customer.getUID(),
                              customer.getName(),
                              customer.getPassword(),
                              customer.getEmail(),
                              customer.getCarIDList());
    }
    public Customer addNewCustomer(Integer uID, String name, String password, String email, ArrayList<Integer> carIDList){
        Customer newCustomer = new Customer(uID, name, password, email, carIDList);
        dbController.InsertCustomer(newCustomer);
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
        Order newOrder = orderController.makeNewSimpleOrder(customerID, carID, estimatedExitTime,  parkingLotNumber);
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
        Order newOrder = orderController.makeNewPreOrder(customerID, carID, estimatedExitTime,  parkingLotNumber, estimatedEntryTime);
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
        Order orderToRemove = customer.getActiveOrders().get(orderID);
        orderToRemove.setOrderStatus(Order.orderStatus.DELETED);
        customer.getActiveOrders().remove(orderID);
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
            orderController.finishOrder(orderToFinish.getOrderID(), checkedPrice);
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
        price = Billing.priceList.PRE_ORDER_ONE_TIME_PARKING;
        if (!(orderToFinish instanceof PreOrder)){
            price = Billing.priceList.ONE_TIME_PARKING;
        }
        ArrayList<Integer> existingSubscriptionIDs = SubscriptionController.getInstance().findSubscriptionsByCarID(subscriptionsList, carID);

        if (existingSubscriptionIDs.size() > 0) {
            for (Integer subscriptionID : existingSubscriptionIDs) {
                Subscription current = subscriptionsList.get(subscriptionID);
                if (current.getSubscriptionType() == Subscription.SubscriptionType.FULL)
                    return Billing.priceList.NO_CHARGE_DUE_TO_SUBSCRIPTION;
                else
                // There is a a subscription listed on this car but need to check if its of the same parkingLot as the order's.
                {
                    RegularSubscription regularSubscription = (RegularSubscription) current;
                    if (orderToFinish.getParkingLotNumber().equals(regularSubscription.getParkingLotNumber())
                            && validateRegularTimes(regularSubscription, orderToFinish)) { // Then we have a RegularSubscription!
                        price = Billing.priceList.NO_CHARGE_DUE_TO_SUBSCRIPTION;
                    }
                }
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
     * @param regularEntryTime
     * @param regularExitTime
     * @param parkingLotNumber
     * @return 0 = Customer has his car subscribed on Full already, 1 = Added/Success, 2 = Had Regular
     */
    public Integer addNewRegularSubscription(Customer customer, Integer carID, Date regularEntryTime, Date regularExitTime, Integer parkingLotNumber)
    {
        Map<Integer, Subscription> subscriptionList = customer.getSubscriptionList();
        for (Subscription subscription : subscriptionList.values()) {
            if (subscription.getSubscriptionType() == Subscription.SubscriptionType.REGULAR && subscription.getCarID() == carID){
                RegularSubscription regularSub = (RegularSubscription) subscription;
                if (regularSub.getRegularExitTime() == regularEntryTime && regularSub.getRegularExitTime() == regularExitTime
                        && regularSub == regularSub) //then this is just a renewal of an existing subscription!
                    subscriptionController.renewSubscription(regularSub); //just renewing it's expiration date does the job.
            }
        }
        //doesn't have this exact regualr sub so making it! :)
        //making a new full subscription for the user.
        Subscription regularSubscription = subscriptionController
                .addRegularSubscription(carID, regularEntryTime, regularExitTime, parkingLotNumber);
        customer.getSubscriptionList().put(regularSubscription.getSubscriptionID(), regularSubscription);

        return 0;
    }

    /**
     *  Same as addNewRegularSubscription just on FullSubscription this time.
     * @param customer
     * @param carID
     * @return
     */
    public Integer addNewFullSubscription(Customer customer, Integer carID)
    {
        Map<Integer, Subscription> subscriptionList = customer.getSubscriptionList();
        for (Subscription subscription : subscriptionList.values()) {
            if (subscription.getSubscriptionType() == Subscription.SubscriptionType.FULL && subscription.getCarID() == carID){
                subscriptionController.renewSubscription(subscription); //just renewing it's expiration date does the job.
            }
        }
        //Has no Full Subscription over this car
        Subscription fullSubscription = subscriptionController.addFullSubscription(carID); //making a new full subscription for the user.
        customer.getSubscriptionList().put(fullSubscription.getSubscriptionID(), fullSubscription);

        return 0;
    }

    private enum SubscriptionStates{HAS_FULL, CAN_ADD_THIS, CHANGED_TO_FULL}


//    TODO: so after our discussion i assume we dont really care what subsciptions the user already has with this car.
//    TODO :making this function useless right?
//    /**
//     * private method used by addNewFullSubscription and addNewRegularSubscription in order to decide what to do
//     * with the subscription they want to create.
//     * return HAS_FULL = Customer has his car subscribed on Full already ( meaning no change is needed ), CAN_ADD_THIS = Add this to list, CHANGED_TO_FULL = Had Regular Changing to full here!.
//     */
//    private SubscriptionStates addSubscription( Customer customer, Integer carID, Integer parkingLotNumber) {
//        SubscriptionStates state;
//        Map<Integer, Subscription> subscriptionList = customer.getSubscriptionList();
//        // Since subscriptionList is mapped by subscriptionID we will have to search each one for carID to see if we have a match.
//        ArrayList<Integer> existingSubscriptionsID = subscriptionController.findSubscriptionsByCarID(subscriptionList, carID);
//        if (existingSubscriptionsID.size() > 0) {// then there is a subscription on this car already.
//            for (Integer subscriptionID : existingSubscriptionsID) {
//                Subscription current = subscriptionList.get(subscriptionID);
//                if (current.getSubscriptionType() == Subscription.SubscriptionType.REGULAR) {
//                    RegularSubscription currentRegular = (RegularSubscription) current;
//                    if (!currentRegular.getParkingLotNumber().equals(parkingLotNumber))
//                        //TODO: replace to Full? or suggest doing so?...will currently just replace to full and charge for the remaining cost.
//                        subscriptionList.remove(existingSubscriptionsID);
//                    Subscription newFullSubscription = SubscriptionController.getInstance().addFullSubscription(current.getCarID(), new Date());
//                    SubscriptionController.getInstance().renewSubscription(newFullSubscription);
//                    subscriptionList.put(newFullSubscription.getSubscriptionID(), newFullSubscription);
//                    state = SubscriptionStates.CHANGED_TO_FULL;
//                } else {
//                    //already on Full subscription with this car.
//                    // TODO : can also suggest Renewal or ignore?
//                    state = SubscriptionStates.HAS_FULL;
//                }
//            }
//
//        }
//        else
//        {//there isn't any subscription on this car already. so we just add it.
//            state = SubscriptionStates.CAN_ADD_THIS;
//        }
//        return state;
//    }

}