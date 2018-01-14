package controller;

import Exceptions.LastCarRemovalException;
import Exceptions.OrderNotFoundException;
import entity.*;

import java.util.*;

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

    public enum SubscriptionOperationReturnCodes {
        FAILED,
        SUCCESS_ADDED,
        RENEWED,
    }

    private CustomerController(){
        System.out.print("\tLoading customers...");
        getCustomersFromDb();
        System.out.println("Done!");

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
        dbController.insertCustomer(newCustomer);
        _customersList.put(newCustomer.getUID(),newCustomer);
        return newCustomer;
    }

    public ArrayList<Object> getCustomersPreOrders(int customerID){
        return new ArrayList<Object>(dbController.getOrdersByUserID(customerID).values());
    }

    public ArrayList<Object> getCustomersActiveOrders(int customerID){
        return new ArrayList<>(_customersList.get(customerID).getActiveOrders().values());
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
            Billing.priceList checkedPrice = getHourlyParkingCost(customer.getUID(), orderToFinish);
            orderController.finishOrder(orderToFinish.getOrderID(), checkedPrice);
            return orderToFinish.getPrice();
        }
        else{ throw new OrderNotFoundException(orderID);}
    }

    /**
     *  Private method to be used in finishOrder method, will check if the user has any subscription THAT MATCHES this order.
     * @param customerID
     * @param orderToFinish
     * @return
     */
    public Billing.priceList getHourlyParkingCost(Integer customerID, Order orderToFinish){
        Billing.priceList price;
        Map<Integer, Subscription> subscriptionsList = customerController.getCustomer(customerID).getSubscriptionMap();
        price = Billing.priceList.PRE_ORDER_ONE_TIME_PARKING;
        if (!(orderToFinish instanceof PreOrder))
        {
            price = Billing.priceList.ONE_TIME_PARKING;
        }
        ArrayList<Integer> existingSubscriptionIDs = SubscriptionController.getInstance()
                .findSubscriptionsByCarID(subscriptionsList, orderToFinish.getCarID());

        if (existingSubscriptionIDs.size() > 0)
        {
            for (Integer subscriptionID : existingSubscriptionIDs)
            {
                Subscription current = subscriptionsList.get(subscriptionID);
                if (current.getSubscriptionType() == Subscription.SubscriptionType.FULL)
                    return Billing.priceList.NO_CHARGE_DUE_TO_SUBSCRIPTION;
                else
                // There is a a subscription listed on this car but need to check if its of the same parkingLot as the order's.
                {
                    RegularSubscription regularSubscription = (RegularSubscription) current;
                    if (orderToFinish.getParkingLotNumber().equals(regularSubscription.getParkingLotNumber())
                            && validateRegularTimes(orderToFinish))
                    {
                        // Then we have a RegularSubscription!
                        price = Billing.priceList.NO_CHARGE_DUE_TO_SUBSCRIPTION;
                    }
                }
            }
        }
        return price;
    }

    /**
     * private method used in order to make sure this order actualy fits the subscription regularly 1 time entrance.
     * @param currentOrder
     * @return true if it does, false otherwise.
     */
    private boolean validateRegularTimes(Order currentOrder) {
        //First we check if this Subscription has not been used today.
        Customer customer = _customersList.get(currentOrder.getCostumerID());
        for (Order order : customer.getActiveOrders().values()){
            if (order.getCarID().equals(currentOrder.getCarID())
                    && order.getOrderStatus().equals(Order.orderStatus.FINISHED))
            { //Then this car has entered the parking Lot today!
                return false;
            }
        }
        //Then we check if this order is within working days.
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentOrder.getEntryTime());
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek <= 5;
    }

    /**
     *  Add another car to this customer.
     * @param customer
     * @param carID
     */
    public void addCar(Customer customer, Integer carID)
    {
        customer.getCarIDList().add(carID);
        dbController.addCarToCustomer(customer.getUID(), carID);
    }

    /**
     * @param carID     * Remove carID from this customer, will fail if this is the last car on his list!
     * @param customer

     */
    public boolean removeCar(Customer customer, Integer carID) throws LastCarRemovalException {
        ArrayList<Integer> carList = customer.getCarIDList();
        if (carList.contains(carID)) {
            if (carList.size() > 1) {
                //TODO ADD THIS AFTER SUPPORT OF CAR REMOVAL.
                if(dbController.removeCarFromCustomer(customer.getUID(), carID)){
                    customer.getCarIDList().remove(carID);
                    return true;
                }else{
                    return false;
                }
            }
            else { // Trying to remove the customer's last car!
                throw new LastCarRemovalException("Customer : " + customer.getName() + "Car ID:" + carID);
            }
        }else{
            return false;
        }
    }

    /**
     *   Given the the right params needed for a new Regular Subscription
     *   the controller will add subscribe it and put it into the the customer's subscriptionList.
     * @param subs - Regular Subscription
     * @return CustomerController.SubscriptionOperationReturnCodes Return Code
     */
    public SubscriptionOperationReturnCodes addNewRegularSubscription(RegularSubscription subs)
    {
        Customer cust = customerController.getCustomer(subs.getUserID());
        Map<Integer, Subscription> subscriptionList = cust.getSubscriptionMap();
        for (Subscription subscription : subscriptionList.values()) {
            if (subscription.getSubscriptionType() == Subscription.SubscriptionType.REGULAR && subscription.getCarID() == subs.getCarID()){
                RegularSubscription regularSub = (RegularSubscription) subscription;
                if (regularSub.getParkingLotNumber().equals(subs.getParkingLotNumber())) //then this is just a renewal of an existing subscription!
                {
                    regularSub.setRegularExitTime(subs.getRegularExitTime()); // in case times has changed
                    if (subscriptionController.renewSubscription(regularSub)) //just renewing it's expiration date does the job.
                        return SubscriptionOperationReturnCodes.RENEWED;
                    else
                        return SubscriptionOperationReturnCodes.FAILED;
                }
            }
        }

        if (subscriptionController.addRegularSubscription(subs) != -1) {
            //cust.getSubscriptionMap().put(regularSubscription.getSubscriptionID(), regularSubscription);
            return SubscriptionOperationReturnCodes.SUCCESS_ADDED;
        }else
            return SubscriptionOperationReturnCodes.FAILED;
    }

    /**
     *  Same as addNewRegularSubscription just on FullSubscription this time.
     * @param fSubs - Full Subscription
     * @return CustomerController.SubscriptionOperationReturnCodes Return Code
     */
    public SubscriptionOperationReturnCodes addNewFullSubscription(FullSubscription fSubs)
    {
        Customer cust = customerController.getCustomer(fSubs.getUserID());
        Map<Integer, Subscription> subscriptionList = cust.getSubscriptionMap();
        for (Subscription subscription : subscriptionList.values()) {
            if (subscription.getSubscriptionType() == Subscription.SubscriptionType.FULL && subscription.getCarID() == fSubs.getCarID()){
                subscriptionController.renewSubscription(subscription); //just renewing it's expiration date does the job.
            }else
                return SubscriptionOperationReturnCodes.FAILED;
        }
        //Has no Full Subscription over this car
        if (subscriptionController.addFullSubscription(fSubs) != -1) {
            //cust.getSubscriptionMap().put(fullSubscription.getSubscriptionID(), fullSubscription);
            return SubscriptionOperationReturnCodes.SUCCESS_ADDED;
        }else
            return SubscriptionOperationReturnCodes.FAILED;
    }
}