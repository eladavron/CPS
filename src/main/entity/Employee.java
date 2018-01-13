package entity;

import java.util.Date;

/**
 * A sample class of how an Employee class will look in the system.
 */
public class Employee extends User{
    /**
     * Private properties
     */
    private String _password;
    private Date _creationDate;

    /**
     * Constructor
     * @param uID Employee's UID
     * @param name Employee's name
     * @param email Employee's email
     * @param password Employee's Password
     */
    public Employee(Integer uID, String name, String email, String password) {
        super(uID,name, password, email, UserType.EMPLOYEE);
        this._password = password;
        this._creationDate = new Date();
    }
   
    /**
     * Getters and Setters.
     */
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


    @Override
    public String toString() {
        return super.toString() +
                ", " +
                "user type = EMPLOYEE ."; 
    }
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        if (!super.equals(o)) return false;
        Employee employee = (Employee) o;
        return (super._name.equals(employee._name)) &&
                (super._email.equals(employee._email)) &&
                (super._uID == employee._uID) &&
        		(_password.equals(employee._password)) &&
        		(_creationDate.equals(employee._creationDate));
    }
}
