package entity;

import java.util.Objects;

/**
 * A sample class of how a User class will look like in the system.
 *
 */
public class User {
	/**
     * Private attributes
     */
	protected Integer _uID = -1;
	protected String _name;
	protected String _email;
	
	/**
	 * Class Constructor
	 * @param uID User's UID
	 * @param name User's name
	 * @param email User's email
	 */
	public User(Integer uID, String name, String email){
		this._uID = uID;
		this._name = name;
		this._email = email;
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
	 * Get the user's emaiil.
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

	@Override
    public String toString() {
        return String.format("User No. %d: %s (%s)", this._uID, this._name, this._email);
    }

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		User user = (User) o;
		return _uID == user._uID &&
				Objects.equals(_name, user._name) &&
				Objects.equals(_email, user._email);
	}
}
