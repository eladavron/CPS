package unitTests;

        import controller.BillingController;
        import controller.CustomerController;
        import controller.DBController;
        import controller.OrderController;
        import entity.Order;
        import entity.PreOrder;
        import org.junit.jupiter.api.BeforeAll;
        import org.junit.jupiter.api.Test;
        import org.testfx.framework.junit5.ApplicationTest;
        import utils.TimeUtils;

        import java.sql.SQLException;
        import java.util.Date;

        import static controller.Controllers.dbController;
        import static controller.Controllers.orderController;
        import static controller.Controllers.customerController;
        import static controller.Controllers.billingController;
        import static org.assertj.core.api.Assertions.assertThat;
        import static utils.TimeUtils.addTime;

class OrderControllerTest extends ApplicationTest {
    private Integer parkingTemp = 1;
//    private Order simpleOrder = new Order(818, 777, new Date(), parkingTemp);
    private Date date = new Date();

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
    }

    @Test
    void makeSimpleOrderTest() {
        Order simpleOrder = new Order(818, 777, addTime(date, TimeUtils.Units.DAYS, 2), parkingTemp);
        Order actual = null;
        try {
            actual = orderController.makeNewSimpleOrder(818, 777, addTime(date, TimeUtils.Units.DAYS, 2), parkingTemp);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Order expected = simpleOrder;
        expected.setActualEntryTime(actual.getActualEntryTime());

        assertThat(actual).isEqualToComparingFieldByField(expected);

//        date = new Date(date.getTime() + 5000);
//        Order notExpected = new Order(818, 777, date, parkingTemp);
//
//        assertThat(notExpected).isNotEqualTo(actual);
    }

//
    @Test
    void makeSimplePreOrderTest() {
        Date estimatedEntry = new Date();
        Date estimatedExit = addTime(date, TimeUtils.Units.DAYS, 2);
        Order actual = null;
        try {
            actual = orderController.makeNewPreOrder(1, 776, estimatedExit, parkingTemp, estimatedEntry);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Order expected = new PreOrder(1, 776, estimatedExit, parkingTemp, 5.533333333333333, estimatedEntry);

        assertThat(actual).isInstanceOf(PreOrder.class);
        assertThat(actual).isEqualTo(expected);
        estimatedExit.setTime(estimatedExit.getTime() + 100000000);
        Order notExpected = new PreOrder(1, 776, estimatedExit, parkingTemp, 0, estimatedEntry);
        assertThat(notExpected).isNotEqualTo(actual);
    }
//
//    @Test
//    void finishSimpleOrderTest() throws SQLException {
//        //Setting entry time to be minus 500,000 in order for a charge to happen.
//        Order expected;
//        simpleOrder.setActualEntryTime(new Date(date.getTime() - 500000));
//        Order notExpected = new Order(simpleOrder);
//        expected  = new Order(simpleOrder);
//        Order actual = OrderController.getInstance().finishOrder(simpleOrder.getOrderID(), 3.0);//RAMI PLEASE FIX THE PRICE!!!
//        notExpected.setPrice(4);
//        expected.setPrice(0.6666666666666666);
//        expected.setActualExitTime(actual.getActualExitTime());
//
//        assertThat(actual).isEqualTo(expected);
//        assertThat(actual).isNotEqualTo(notExpected);
//
//    }
}
