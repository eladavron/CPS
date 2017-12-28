package unitTests;

import entity.Order;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;

public class OrderTest extends ApplicationTest {
    private Order testOrder;

    @Test
    public void make_simple_order(){ //currently doesn't work since equalTo is not modified on order class.
        Date estimated = new Date();
        testOrder = new Order("Tester", Integer.valueOf(777), estimated);
        Order simpleOrder = new Order("Tester", Integer.valueOf(777), estimated);
        // Should equal
        assertThat(testOrder).isEqualTo(simpleOrder);
        estimated.setTime(90000);
        assertThat(testOrder).isEqualTo(simpleOrder);

    }
}
