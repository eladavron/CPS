package unitTests;

import controller.CustomerController;
import entity.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


class CustomerControllerTest extends ApplicationTest {
    private Customer _testCustomer;
    private ArrayList<Integer> carList;

    @BeforeEach
    private void beforeEach(){
        carList = new ArrayList<>();
        carList.add(6677788);
        _testCustomer = new Customer(777, "Bob","666" , "FakeMail@.com",carList);
    }

    @Test
    void addNewCustomerTest()
    {
        Customer newCustomer = null;
        try {
            newCustomer = CustomerController.getInstance()
                    .addNewCustomer(777, "Bob","666", "FakeMail@.com", carList);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        assertThat(newCustomer).isEqualToComparingFieldByField(_testCustomer);
    }

    @Test
    void addCarToCustomerTest()
    {
        assertThat(_testCustomer.getCarIDList().size()).isEqualTo(1);
        try {
            CustomerController.getInstance().addCar(_testCustomer, 7788899);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        assertThat(_testCustomer.getCarIDList().size()).isEqualTo(2);
        assertThat(_testCustomer.getCarIDList()).contains(7788899);
    }

}
