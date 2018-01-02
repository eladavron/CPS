package entity;

import java.util.ArrayList;

import entity.Order;


/**
 * A sample class of how a Customer class will look like in the system.
 *
 */
public class Customer extends User{
	/**
	 * Private attributes of the Customer class.
	 */
	private User _user;
	private ArrayList<Integer> _carIDList = new ArrayList<Integer>();
	private ArrayList<Subscription> _subscriptionList = new ArrayList<Subscription>();
	private ArrayList<Order> _activeOrders = new ArrayList<Order>();
	
	/**
	 * Customer Class Constructor.
	 * @param caridlist Customer's cars' ids list.
	 * @param subscriptionlist Customer's subscriptions' list.
	 * @param activeorders Customer's active orders' list.
	 */
	public Customer(User user, ArrayList<Integer> caridlist, ArrayList<Subscription> subscriptionlist, ArrayList<Order> activeorders) {
		super(user.getUID(),user.getName(),user.getEmail());
		this._user = user;
		this._carIDList = caridlist;
		this._subscriptionList = subscriptionlist;
		this._activeOrders = activeorders;
	}
	
	/**
	 * Get the customer's user details.
	 * @return Customer's user details..
	 */
	public User getUser() {
		return _user;
	}

	/**
	 * Set the customer's user details.
	 * @param _user Customer's user details.
	 */
	public void setUser(User _user) {
		this._user = _user;
	}
	
	/**
	 * Get the customer's car ids list.
	 * @return Customer's car id list.
	 */
	public ArrayList<Integer> getCaridlist() {
		return _carIDList;
	}
	
	/**
	 * Set the customer's car ids list.
	 * @param _caridlist Car ID list to set the customer's car ids list.
	 */
	public void setCaridlist(ArrayList<Integer> _caridlist) {
		this._carIDList = _caridlist;
	}

	/**
	 * Get the customer's subscriptions' list.
	 * @return Customer's subscriptions' list.
	 */
	public ArrayList<Subscription> getSubscriptionlist() {
		return _subscriptionList;
	}

	/**
	 * Set the customer's subscriptions' list.
	 * @param _subscriptionlist Subscription's list to set the customer's subscriptions' list.
	 */
	public void setSubscriptionlist(ArrayList<Subscription> _subscriptionlist) {
		this._subscriptionList = _subscriptionlist;
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
	 * @param _activeorders Active orders list to set the customer's active orders' list.
	 */
	public void setActiveorders(ArrayList<Order> _activeorders) {
		this._activeOrders = _activeorders;
	}
}