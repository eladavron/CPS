package entity;

import java.util.ArrayList;
import entity.Order;
import entity.Subscription;


/**
 * A sample class of how a Customer class will look like in the system.
 *
 */
public class Customer extends User{
	/**
	 * Private attributes of the Customer class.
	 */
	private ArrayList<Integer> _carIDList = new ArrayList<>();
	private ArrayList<Subscription> _subscriptionList;
	private ArrayList<Order> _activeOrders;
	
	/**
	 * Customer Class Constructor.
	 * @param uID Customer's UID
	 * @param name Customer's name
	 * @param email Customer's email
	 * @param carIDList Customer's cars' ids list.
	 * @param subscriptionList Customer's subscriptions' list.
	 */
	public Customer(long uID, String name, String email, ArrayList<Integer> carIDList, ArrayList<Subscription> subscriptionList) {
		super(uID,name,email);
		this._carIDList = carIDList;
		this._subscriptionList = new ArrayList<>();
		this._activeOrders = new ArrayList<>();
	}
	
	/**
	 * Get the customer's car ids list.
	 * @return Customer's car id list.
	 */
	public ArrayList<Integer> getCaridList() {
		return _carIDList;
	}
	
	/**
	 * Set the customer's car ids list.
	 * @param carIDList Car ID list to set the customer's car ids list.
	 */
	public void setCaridList(ArrayList<Integer> carIDList) {
		this._carIDList = carIDList;
	}

	/**
	 * Get the customer's subscriptions' list.
	 * @return Customer's subscriptions' list.
	 */
	public ArrayList<Subscription> getSubscriptionList() {
		return _subscriptionList;
	}

	/**
	 * Set the customer's subscriptions' list.
	 * @param subscriptionList Subscription's list to set the customer's subscriptions' list.
	 */
	public void setSubscriptionList(ArrayList<Subscription> subscriptionList) {
		this._subscriptionList = subscriptionList;
	}

	
	/**
	 * Get the customer's active orders' list.
	 * @return Customer's active orders' list.
	 */
	public ArrayList<Order> getActiveorders() {
		return _activeOrders;
	}
	
	/**
	 * Set the customer's active orders' list.
	 * @param activeOrders Active orders list to set the customer's active orders' list.
	 */
	public void setActiveorders(ArrayList<Order> activeOrders) {
		this._activeOrders = activeOrders;
	}
	
	@Override
    public String toString() {
        return super.toString() +
                ", " +
                "user type = CUSTOMER ."; 
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