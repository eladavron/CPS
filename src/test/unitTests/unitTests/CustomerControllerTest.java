package unitTests;

import Exceptions.CustomerNotificationFailureException;
import controller.*;
import entity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import utils.TimeUtils;

import java.sql.SQLException;
import java.util.*;

import static controller.Controllers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static utils.TimeUtils.addTime;

/**
 * Testing the functionality of CustomerController.
 */
class CustomerControllerTest extends ApplicationTest {
    private Customer _testCustomer;
    private ArrayList<Integer> carList;

    @BeforeAll
    static void setTestInitDB() throws SQLException {
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
        parkingController = ParkingController.getInstance();
    }

    @BeforeEach
    private void beforeEach()
    {
        Map<Integer, ParkingLot> parkingLotList = parkingController.getParkingLotList();
        parkingLotList.put(1 , new ParkingLot(3,1,1,"FakeLot"));
        carList = new ArrayList<>();
        carList.add(6677788);
        _testCustomer = new Customer(777, "Bob","666" , "FakeMail@.com",carList);
        customerController.getCustomersList().clear();
    }

    /**
     * Test of getCustomer function.
     */
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

    /**
     * Test of getCustomerByEmail function.
     */
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

    /**
     * Test of addNewCustomer function.
     */
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

    /**
     * Test of addCarToCustomer function.
     */
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

    /**
     * Test of addCar function.
     */
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

    /**
     * Test of removeCar function.
     */
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

    /**
     * Test of addNewRegularSubscription function.
     */
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

    /**
     * Test of addNewFullSubscription function.
     */
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

    /**
     * Test of addNewOrder function.
     */
    @Test
    void addNewOrderTest(){
        Date date = new Date();
        Order newOrder = null;

        try {
            customerController.addNewCustomer(_testCustomer);
            newOrder = customerController.addNewOrder(777,6677788,addTime(date, TimeUtils.Units.DAYS, 2),1);
        } catch (SQLException | CustomerNotificationFailureException e) {
            e.printStackTrace();
        }
        assertThat(customerController.getCustomer(777).getActiveOrders()).containsValues(newOrder);
    }

    /**
     * Test of addNewPreOrder function.
     */
    @Test
    void addNewPreOrderTest(){
        Date date = new Date();
        Order newOrder = null;

        try {
            customerController.addNewCustomer(_testCustomer);
            newOrder = customerController.addNewPreOrder(777,6677788,addTime(date, TimeUtils.Units.DAYS, 2),1,addTime(date, TimeUtils.Units.DAYS, 1));
        } catch (SQLException | CustomerNotificationFailureException e) {
            e.printStackTrace();
        }
        assertThat(customerController.getCustomer(777).getActiveOrders()).containsValues(newOrder);
    }
}
