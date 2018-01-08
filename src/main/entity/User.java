package entity;

/**
 * A sample class of how a User class will look like in the system.
 *
 */
public class User {
    /**
     * Private attributes
     */
    protected long _uid = -1;
    protected String _name;
    protected String _email;

    /**
     * Class Constructor
     * @param _uid User's UID
     * @param _name User's name
     * @param _email User's email
     */
    public User(long uid, String name, String email){
        this._uid = uid;
        this._name = name;
        this._email = email;
    }

    /**
     * Empty constructor for Jackson
     * @author Elad Avron
     */
    public User(){}

    /**
     * Getters and setters functions for USer class.
     */

    /**
     * Get the user's UID.
     * @return User's UID
     */
    public long getUID() {
        return _uid;
    }

    /**
     * Set the user's UID.
     * @param _uid uid to set to user's uid.
     */
    public void setUID(long _uid) {
        this._uid = _uid;
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
     * @param _name name to set the user's name
     */
    public void setName(String _name) {
        this._name = _name;
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
     * @param _email email to set the user's email
     */
    public void setEmail(String _email) {
        this._email = _email;
    }



}
