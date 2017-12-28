package entity;

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

    public int getUid() {
        return _uid;
    }

    public void setUid(int uid) {
        this._uid = uid;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        this._email = email;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        this._password = password;
    }

    public Date getCreationDate() {
        return _creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this._creationDate = creationDate;
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
