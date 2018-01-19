package unitTests;

import controller.*;
import entity.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import utils.TimeUtils;

import java.sql.SQLException;
import java.util.*;

import static controller.Controllers.dbController;
import static controller.Controllers.customerController;
import static controller.Controllers.employeeController;
import static controller.Controllers.subscriptionController;
import static controller.Controllers.billingController;
import static controller.Controllers.orderController;

import static org.assertj.core.api.Assertions.assertThat;
import static utils.TimeUtils.addTime;


class CustomerControllerTest extends ApplicationTest {
    private Customer _testCustomer;
    private ArrayList<Integer> carList;

    @BeforeAll
    static void setTestInitDB(){
        dbController = DBController.getInstance();
        dbController.isTest = true;
        employeeController = EmployeeController.getInstance();

        try {
            customerController = CustomerController.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        subscriptionController = SubscriptionController.getInstance();
        billingController = BillingController.getInstance();
        orderController = OrderController.getInstance();
    }

    @BeforeEach
    private void beforeEach()
    {
        carList = new ArrayList<>();
        carList.add(6677788);
        _testCustomer = new Customer(777, "Bob","666" , "FakeMail@.com",carList);
        customerController.getCustomersList().clear();
    }

    @Test
    void getCustomerTest(){
        Customer customer = null;
        try {
            customerController.addNewCustomer(_testCustomer);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        customer = customerController.getCustomer(777);
        assertThat(customer).isEqualToComparingFieldByField(_testCustomer);
    }

    @Test
    void getCustomerByEmailTest(){
        try {
            customerController.addNewCustomer(_testCustomer);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        Customer customer = customerController.getCustomerByEmail("FakeMail@.com");
        assertThat(customer).isEqualToComparingFieldByField(_testCustomer);
    }

    @Test
    void addNewCustomerTest() {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(11121);
        Customer newCustomer = new Customer(123, "Clark","1212" , "ClarkTomas@.com",list);
        try {
            customerController.addNewCustomer(newCustomer);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertThat(customerController.getCustomersList()).contains(newCustomer);
    }

    @Test
    void addCarToCustomerTest()
    {
        assertThat(_testCustomer.getCarIDList().size()).isEqualTo(1);
        try {
            customerController.addCar(_testCustomer, 7788899);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        assertThat(_testCustomer.getCarIDList().size()).isEqualTo(2);
        assertThat(_testCustomer.getCarIDList()).contains(7788899);
    }

    @Test
    void addCarTest()
    {
        try {
            customerController.addCar(_testCustomer,211);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertThat(_testCustomer.getCarIDList()).contains(211);
    }

    @Test
    void removeCarTest()
    {
        try {
            customerController.removeCar(_testCustomer,211);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertThat(_testCustomer.getCarIDList()).doesNotContain(211);
    }

    @Test
    void addNewRegularSubscriptionTest()
    {
        try {
            customerController.addNewCustomer(_testCustomer);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        RegularSubscription regularSubscription = new RegularSubscription(777,carList,"23:23",1);
        try {
            customerController.addNewRegularSubscription(regularSubscription);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Map<Integer, Subscription> subscriptionList = customerController.getCustomer(777).getSubscriptionMap();
        assertThat(subscriptionList).containsKeys(regularSubscription.getSubscriptionID());
    }

    @Test
    void addNewFullSubscriptionTest()
    {
        try {
            customerController.addNewCustomer(_testCustomer);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        FullSubscription fullSubscription = new FullSubscription(777,carList);
        try {
            customerController.addNewFullSubscription(fullSubscription);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Map<Integer, Subscription> subscriptionList = customerController.getCustomer(777).getSubscriptionMap();
        assertThat(subscriptionList).containsKeys(fullSubscription.getSubscriptionID());
    }

    @Test
    void addNewOrderTest(){
        Date date = new Date();
        Order newOrder = null;

        try {
            customerController.addNewCustomer(_testCustomer);
            newOrder = customerController.addNewOrder(777,6677788,addTime(date, TimeUtils.Units.DAYS, 2),1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertThat(customerController.getCustomer(777).getActiveOrders()).containsValues(newOrder);
    }

    @Test
    void addNewPreOrderTest(){
        Date date = new Date();
        Order newOrder = null;

        try {
            customerController.addNewCustomer(_testCustomer);
            newOrder = customerController.addNewPreOrder(777,6677788,addTime(date, TimeUtils.Units.DAYS, 2),1,addTime(date, TimeUtils.Units.DAYS, 1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        assertThat(customerController.getCustomer(777).getActiveOrders()).containsValues(newOrder);
    }
}
