package unitTests;

import controller.BillingController;
import controller.DBController;
import controller.SubscriptionController;
import entity.FullSubscription;
import entity.RegularSubscription;
import entity.Subscription;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import utils.TimeUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.billingController;
import static controller.Controllers.dbController;
import static entity.Subscription.SubscriptionType.REGULAR;
import static org.assertj.core.api.Assertions.assertThat;
import static utils.TimeUtils.addTime;

/**
 * Testing the functionality of SubscriptionController.
 */
public class SubscriptionControllerTest extends ApplicationTest{
    private Subscription _testSubscription;
    private RegularSubscription _testRegularSubscription;
    private Subscription _newSubscriptionFromConstructor;
    private FullSubscription _testFullSubscription;
    private static SubscriptionController subscriptionController;
    private Subscription _testSubscription2nd;
    private ArrayList<Integer> _carsIDList1 = new ArrayList<>();
    private ArrayList<Integer> _carsIDList2 = new ArrayList<>();
    private ArrayList<Integer> _carsIDList3 = new ArrayList<>();
    private ArrayList<Integer> _carsIDList4 = new ArrayList<>();

    @BeforeAll
    static void setTestInitDB(){
        dbController = DBController.getInstance();
        subscriptionController = SubscriptionController.getInstance();
        billingController = BillingController.getInstance();
        dbController.isTest = true;
    }

    @BeforeEach
    private void beforeEach()
    {
        _carsIDList1.add(21);
        _carsIDList2.add(611);
        _carsIDList3.add(222);
        _carsIDList4.add(22);
        _carsIDList2.add(61);
        _testSubscription = new Subscription(12,_carsIDList1,REGULAR);
        _testSubscription2nd = new Subscription(51, _carsIDList2, REGULAR);
        _testRegularSubscription = new RegularSubscription(111,_carsIDList3,"11:11",1);
        _testFullSubscription = new FullSubscription(11,_carsIDList4);
    }

    /**
     * Test of getSubscription function.
     */
    @Test
    void getSubscriptionTest()
    {
        _newSubscriptionFromConstructor = subscriptionController
                .addRegularSubscription(111,_carsIDList1,"11:11",1);

        Subscription sub = subscriptionController.getSubscription(_newSubscriptionFromConstructor.getSubscriptionID());
        assertThat(sub).isEqualToComparingFieldByField(_newSubscriptionFromConstructor);
    }

    /**
     * Test of addRegularSubscription function.
     */
    @Test
    void addRegularSubscriptionTest()
    {
        _newSubscriptionFromConstructor = subscriptionController
                .addRegularSubscription(111,_carsIDList3,"11:11",1);

        assertThat(_newSubscriptionFromConstructor).isEqualToIgnoringGivenFields(_testRegularSubscription,"_expiration");
    }

    /**
     * Test of addFullSubscription function.
     */
    @Test
    void addFullSubscriptionTest() {
//        _newSubscriptionFromConstructor = subscriptionController.addFullSubscription(11, _carsIDList4);
        try {
            _newSubscriptionFromConstructor = subscriptionController.addFullSubscription(11,_carsIDList4);
        } catch (NullPointerException e){
            System.out.println("The fucking bug got to here......");
            e.printStackTrace();
        }
        assertThat(_newSubscriptionFromConstructor).isEqualToIgnoringGivenFields(_testFullSubscription,"_expiration","_charge");
    }

    /**
     * Test of renewSubscription function.
     */
    @Test
    void renewSubscriptionTest() {
        try {
            Date oldDate = _testSubscription.getExpiration();
            if(subscriptionController.renewSubscription(_testSubscription)){
                assertThat(_testSubscription.getExpiration()).isEqualTo(addTime(oldDate, TimeUtils.Units.DAYS, 28));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test of findSubscriptionsByCarID function.
     */
    @Test
    void findSubscriptionsByCarIDTest() {
        ArrayList<Integer> subscriptionsIDs = new ArrayList<>();
        Map<Integer, Subscription> subscriptionList = new HashMap<>();
        subscriptionList.put(_testSubscription.getSubscriptionID(),_testSubscription);
        subscriptionList.put(_testSubscription2nd.getSubscriptionID(),_testSubscription2nd);
        subscriptionsIDs = subscriptionController.findSubscriptionsByCarID(subscriptionList,611);
        assertThat(subscriptionsIDs).contains(_testSubscription2nd.getSubscriptionID());
        assertThat(subscriptionsIDs).contains(_testSubscription2nd.getSubscriptionID());

    }

    /**
     * Test of putAll function.
     */
    @Test
    void putAllTest() {
        ArrayList<Object> objectsList = new ArrayList<>();
        objectsList.add(_testRegularSubscription);
        objectsList.add(_testFullSubscription);
        assertThat(objectsList.size()).isNotEqualTo(0);
        subscriptionController.putAll(objectsList);
        assertThat(subscriptionController.getSubscription(_testFullSubscription.getSubscriptionID()))
                .isEqualToComparingFieldByField(_testFullSubscription);
    }
}



