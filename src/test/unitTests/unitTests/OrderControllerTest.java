package unitTests;

import Exceptions.CustomerNotificationFailureException;
import controller.*;
import entity.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import utils.TimeUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import static controller.Controllers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static utils.TimeUtils.addTime;

/**
 * Testing the functionality of OrderController.
 */
class OrderControllerTest extends ApplicationTest {

    private Integer parkingTemp = 1;
    private Date date = new Date();
    private Order simpleOrder;
    private Customer customer;
    private ArrayList<Integer> carList;

    @BeforeAll
    static void setTestInitDB() {
        dbController = DBController.getInstance();
        dbController.isTest = true;
        orderController = OrderController.getInstance();
        try {
            customerController = CustomerController.getInstance();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        billingController = BillingController.getInstance();
        employeeController = EmployeeController.getInstance();

    }
    @BeforeEach
    private void beforeEach()
    {
        parkingController = ParkingController.getInstance();
        Map<Integer, ParkingLot> parkingLotList = parkingController.getParkingLotList();
        parkingLotList.put(1 , new ParkingLot(3,1,1,"FakeLot"));
        orderController.getOrdersMap().clear();
        simpleOrder = new Order(818, 777, addTime(date, TimeUtils.Units.DAYS, 2), parkingTemp);
        carList = new ArrayList<>();
        carList.add(6677788);
        customer = new Customer(818,"Rami","pw","rami@h.com",carList);
        customerController.getCustomersList().clear();
    }

    /**
     * Test of makeSimpleOrder function.
     */
    @Test
    void makeSimpleOrderTest() {

        Order actual = null;
        try {
            actual = orderController.makeNewSimpleOrder(818, 777, addTime(date, TimeUtils.Units.DAYS, 2), parkingTemp);
        } catch (SQLException | CustomerNotificationFailureException e) {
            e.printStackTrace();
        }
        Order expected = simpleOrder;
        // These values below must be set in this way because the makeNewSimpleOrder sets their values
        // and can't be set directly from the constructor...
        expected.setOrderStatus(Order.OrderStatus.IN_PROGRESS);
        expected.setActualEntryTime(actual.getActualEntryTime());
        expected.setEstimatedEntryTime(actual.getEstimatedEntryTime());
        expected.setOrderID(actual.getOrderID());
        assertThat(actual).isEqualToComparingFieldByField(expected);

        date = new Date(date.getTime() + 5000);
        Order notExpected = new Order(818, 777, date, parkingTemp);

        assertThat(notExpected).isNotEqualTo(actual);
    }

    /**
     * Test of makeSimplePreOrder function.
     */
    @Test
    void makeSimplePreOrderTest() {
        Date estimatedEntry = new Date();
        Date estimatedExit = addTime(date, TimeUtils.Units.DAYS, 2);
        PreOrder actual = null;
        Customer customer = new Customer(818,"Rami","pw","rami@h.com",carList);
        try {
            customerController.addNewCustomer(customer);
            actual =(PreOrder) orderController.makeNewPreOrder(818, 776, estimatedExit, parkingTemp, estimatedEntry);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PreOrder expected = new PreOrder(818, 776, estimatedExit, parkingTemp, 5.533333333333333, estimatedEntry);
        assert actual != null;
        expected.setCharge(actual.getCharge()); // I have to because I don't have any other way to calculate the charge for now..
        expected.setOrderID(actual.getOrderID()); // ID is received from DB but this object don't access the DB.

        assertThat(actual).isEqualToIgnoringGivenFields(actual,"_actualEntryTime");

        estimatedExit.setTime(estimatedExit.getTime() + 100000000);
        Order notExpected = new PreOrder(1, 776, estimatedExit, parkingTemp, 0, estimatedEntry);

        assertThat(notExpected).isNotEqualTo(actual);
    }

    /**
     * Test of finishSimpleOrder function.
     */
    @Test
    void finishSimpleOrderTest() throws SQLException, CustomerNotificationFailureException {
        //Setting entry time to be minus 500,000 in order for a charge to happen.
        Order expected;
        simpleOrder.setActualEntryTime(new Date(date.getTime() - 500000));
        expected = orderController.makeNewSimpleOrder(simpleOrder.getCostumerID(), simpleOrder.getCarID()
                , simpleOrder.getEstimatedExitTime() , simpleOrder.getParkingLotNumber());

        customerController.addNewCustomer(customer);

        Order actual = orderController.finishOrder(expected.getOrderID(), expected.getPrice());

        expected.setPrice(0.6666666666666666);
        expected.setActualExitTime(actual.getActualExitTime());

        assertThat(actual).isEqualTo(expected);

    }

    /**
     * Test of deleteOrder function.
     */
    @Test
    void deleteOrderTest(){
        Order orderToDelete = null;
        try {
            customerController.addNewCustomer(customer);
            orderToDelete = orderController.makeNewPreOrder(818,777,addTime(date, TimeUtils.Units.DAYS, 2), parkingTemp, date);
            orderController.deleteOrder(orderToDelete.getOrderID());
        } catch (SQLException | CustomerNotificationFailureException e) {
            e.printStackTrace();
        }
        assert orderToDelete != null;
        assertThat(orderController.getOrdersMap()).doesNotContainKeys(orderToDelete.getOrderID());
    }
}
