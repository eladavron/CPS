package cps;

import java.util.Date;

public class Employee {
    private int _uid = -1;
    private String _name;
    private String _email;
    private String _password;
    private Date _creationDate;

    public Employee(String _name, String _email, String _password) {
        this._name = _name;
        this._email = _email;
        this._password = _password;
    }

    public int get_uid() {
        return _uid;
    }

    public void set_uid(int _uid) {
        this._uid = _uid;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_email() {
        return _email;
    }

    public void set_email(String _email) {
        this._email = _email;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public Date get_creationDate() {
        return _creationDate;
    }

    public void set_creationDate(Date _creationDate) {
        this._creationDate = _creationDate;
    }

    @Override
    public String toString() {
        return String.format("Employee No. %d: %s (%s)", this._uid, this._name, this._email);
    }
}
