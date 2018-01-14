package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A sample class of how a Customer class will look like in the system.
 *
 */
public class Customer extends User{
	/**
	 * Private attributes of the Customer class.
	 */
	private ArrayList<Integer> _carIDList;
	private Map<Integer, Subscription> _subscriptionList;
	private Map<Integer, Order> _activeOrders;
	
	/**
	 * Customer Class Constructor.
	 * @param uID Customer's UID
	 * @param name Customer's name
	 * @param email Customer's email
	 * @param carIDList Customer's cars' ids list.
	 */
	public Customer(Integer uID, String name, String password, String email, ArrayList<Integer> carIDList) {
		super(uID, name, password, email, UserType.CUSTOMER);
		this._carIDList = carIDList;
		this._subscriptionList = new HashMap<>();
		this._activeOrders = new HashMap<>();
	}

	/**
	 * Default constructor
	 */
	public Customer(){}
	/**
	 * Get the customer's car ids list.
	 * @return Customer's car id list.
	 */
	public ArrayList<Integer> getCarIDList() {
		return _carIDList;
	}
	
	/**
	 * Set the customer's car ids list.
	 * @param carIDList Car ID list to set the customer's car ids list.
	 */
	public void setCarIDList(ArrayList<Integer> carIDList) {
		this._carIDList = carIDList;
	}

	/**
	 * Get the customer's subscriptions' list.
	 * @return Customer's subscriptions' list.
	 */
	public Map<Integer, Subscription> getSubscriptionMap() {
		return _subscriptionList;
	}

	public ArrayList<Object> getSubscriptionList(){
		return new ArrayList<Object>(_subscriptionList.values());
	}

	/**
	 * Set the customer's subscriptions' list.
	 * @param subscriptionList Subscription's list to set the customer's subscriptions' list.
	 */
	public void setSubscriptionMap(Map<Integer, Subscription> subscriptionList) {
		this._subscriptionList = subscriptionList;
	}

	
	/**
	 * Get the customer's active orders' list.
	 * @return Customer's active orders' list.
	 */
	public Map<Integer, Order> getActiveOrders() {
		return _activeOrders;
	}
	
	/**
	 * Set the customer's active orders' list.
	 * @param activeOrders Active orders list to set the customer's active orders' list.
	 */
	public void setActiveOrders(Map<Integer, Order> activeOrders) {
		this._activeOrders = activeOrders;
	}
	
	@Override
    public String toString() {
        return super.toString() +
                ", cars list: " + _carIDList
				+ ", subscriptions list: " + _subscriptionList
				+ ", active orders: " + _activeOrders;
    }
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        if (!super.equals(o)) return false;
        Customer customer = (Customer) o;
		return (super._name.equals(customer._name)) &&
				(super._email.equals( customer._email)) &&
				(super._uID == customer._uID) &&
        		_carIDList.equals(customer._carIDList) && 
        		_subscriptionList.equals(customer._subscriptionList) &&
        		_activeOrders.equals(customer._activeOrders);
    }
}