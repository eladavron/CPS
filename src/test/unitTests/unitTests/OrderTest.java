package unitTests;

import controller.DBController;
import controller.OrderController;
import entity.Billing.priceList;
import entity.Order;
import entity.PreOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Date;

import static controller.Controllers.dbController;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTest extends ApplicationTest {
    private Integer parkingTemp = 1;
    private Order simpleOrder = new Order (0, 777, new Date(), parkingTemp);
    private Date estimated = new Date();

    @BeforeAll
    static void setTestInitDB(){
        dbController = DBController.getInstance();
        dbController.isTest = true;
    }

    @Test
    void make_simple_order_test(){
        Order actual = OrderController.getInstance().makeNewSimpleOrder(0, 777, estimated, parkingTemp);
        Order expected = simpleOrder;
        expected.setEntryTime(actual.getEntryTime());

        assertThat(actual).isEqualTo(expected);

        estimated = new Date(estimated.getTime() + 5000);
        Order notExpected = new Order(0, 777, estimated, parkingTemp);

        assertThat(notExpected).isNotEqualTo(actual);
    }

    @Test
    void make_simple_pre_order_test(){
        Date estimatedEntry = new Date();
        Date estimatedExit = new Date(estimatedEntry.getTime()+5000000);
        Order actual = OrderController.getInstance().makeNewPreOrder(1, 776, estimatedExit, parkingTemp, estimatedEntry);
        Order expected = new PreOrder(1, 776, estimatedExit, parkingTemp, 5.533333333333333 ,estimatedEntry);

        assertThat(actual).isInstanceOf(PreOrder.class);
        assertThat(actual).isEqualTo(expected);
        estimatedExit.setTime(estimatedExit.getTime() + 100000000);
        Order notExpected =  new PreOrder(1, 776, estimatedExit, parkingTemp, 0 ,estimatedEntry);
        assertThat(notExpected).isNotEqualTo(actual);
    }

    @Test
    void finish_simple_order_test(){
        //Setting entry time to be minus 500,000 in order for a charge to happen.
        Order expected;
        simpleOrder.setEntryTime(new Date(estimated.getTime() - 500000));
        Order notExpected = new Order(simpleOrder);
        expected  = new Order(simpleOrder);
        Order actual = OrderController.getInstance().finishOrder(simpleOrder.getOrderID(), priceList.ONE_TIME_PARKING);
        notExpected.setPrice(4);
        expected.setPrice(0.6666666666666666);
        expected.setActualExitTime(actual.getActualExitTime());

        assertThat(actual).isEqualTo(expected);
        assertThat(actual).isNotEqualTo(notExpected);

    }
}
