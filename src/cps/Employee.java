package cps;

import java.util.Date;

/**
 * A sample class of how an Employee class will look in the system.
 */
public class Employee {
    /**
     * Private properties
     */
    private int _uid = -1;
    private String _name;
    private String _email;
    private String _password;
    private Date _creationDate;

    /**
     * Constructor
     * @param _name Employee Name
     * @param _email Employee Email
     * @param _password Employee Password
     */
    public Employee(String _name, String _email, String _password) {
        this._name = _name;
        this._email = _email;
        this._password = _password;
    }

    /**
     * Getters and Setters
     */

    /**
     *  Get the Employee's UID
     * @return UID
     */
    public int get_uid() {
        return _uid;
    }

    /**
     * Set Employee UID
     * @param _uid UID
     */
    public void set_uid(int _uid) {
        this._uid = _uid;
    }

    /**
     * Get Employee name
     * @return Name
     */
    public String get_name() {
        return _name;
    }

    /**
     * Set Employee Name
     * @param _name Employee Name
     */
    public void set_name(String _name) {
        this._name = _name;
    }

    /**
     * Get Employee Email Address
     * @return Employee Email
     */
    public String get_email() {
        return _email;
    }

    /**
     * Set Employee Email Address
     * @param _email Email Address
     */
    public void set_email(String _email) {
        this._email = _email;
    }

    /**
     * Get Employee Password
     * @return Password
     */
    public String get_password() {
        return _password;
    }

    /**
     * Set Employee Password
     * @param _password Password
     */
    public void set_password(String _password) {
        this._password = _password;
    }

    /**
     * Get Creation Date
     * @return Creation Date.
     */
    public Date get_creationDate() {
        return _creationDate;
    }

    /**
     * Set Creation Date
     * @param _creationDate Create Date
     */
    public void set_creationDate(Date _creationDate) {
        this._creationDate = _creationDate;
    }

    /**
     * A string representation of the Employee.
     * @return A representation formatted: "Employee No. [UID] [NAME] [EMAIL]"
     */
    @Override
    public String toString() {
        return String.format("Employee No. %d: %s (%s)", this._uid, this._name, this._email);
    }
}
