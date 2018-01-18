package entity;

import java.util.Objects;

/**
 * A sample class of how a User class will look like in the system.
 *
 */
public class User {

	public enum UserType{USER, CUSTOMER, MANAGER, EMPLOYEE, CUSTOMER_SERVICE, SUPERMAN } //SUPERMAN = Network Manager

	/**
     * Private attributes
     */
	protected Integer _uID;
	protected String _name;
	protected String _email;
	protected UserType _userType;
    protected String _password;
	
	/**
	 * Class Constructor
	 * @param uID User's UID
	 * @param name User's name
	 * @param email User's email
	 * @param userType the enum of above.
     * @param password the user's Login password!
	 */

	public User(Integer uID, String name, String password, String email, UserType userType){
		this._uID = uID;
		this._name = name;
		this._email = email;
		this._userType = userType;
		this._password = password;
	}
	/**
	 * Empty constructor for Jackson
	 */
	public User(){}

	/* Getters and Setters */
	/**
	 * Get the user's UID.
	 * @return User's UID
	 */
	public Integer getUID() {
		return _uID;
	}
	
	/**
	 * Set the user's UID.
	 * @param uID uid to set to user's uid.
	 */
	public void setUID(Integer uID) {
		this._uID = uID;
	}
	
	/**
	 * Get the user's name.
	 * @return User's name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Set the user's name.
	 * @param name name to set the user's name
	 */
	public void setName(String name) {
		this._name = name;
	}
	
	/**
	 * Get the user's email.
	 * @return  User's email
	 */
	public String getEmail() {
		return _email;
	}

	/**
	 * Set the user's email
	 * @param email email to set the user's email
	 */
	public void setEmail(String email) {
		this._email = email;
	}


    public UserType getUserType() {
        return _userType;
    }

    public void setUserType(UserType userType) {
        this._userType = userType;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        this._password = password;
    }


	@Override
    public String toString() {
        return ("User No: "+ _uID+ ", name: " + _name + ", email: " + _email +", Type: " + _userType);
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return _uID.equals(user._uID) &&
				Objects.equals(_name, user._name) &&
				Objects.equals(_email, user._email);
	}
}
