package controller;

import entity.Order;
import entity.ParkingLotNumber;
import entity.PreOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class OrderController {

    private ArrayList<Order> _ordersList;

    public OrderController() {
        _ordersList = new ArrayList<>();
    }

    // TODO: for testing purposes makeNewSimpleOrder will send back the order...needs to be a void function once there is a database.
    public Order makeNewSimpleOrder(Integer customerID, Integer carID, Date estimatedExitTime, ParkingLotNumber parkingLotNumber){
        Order newOrder = new Order(customerID, carID, estimatedExitTime, parkingLotNumber);
        _ordersList.add(newOrder);
        return newOrder;
    }

    //TODO : After entering with the car into the parking lot the entry time of Order (super) should be set!)
    public Order makeNewPreOrder(Integer customerID, Integer carID, Date estimatedExitTime, ParkingLotNumber parkingLotNumber, Date estimatedEntryTime){
        Double charge = calculateCharge(estimatedEntryTime, estimatedExitTime, PriceList.PRE_ORDER_ONE_TIME_PARKING);
        Order newPreOrder = new PreOrder(customerID, carID, estimatedExitTime ,parkingLotNumber, charge, estimatedEntryTime);
        _ordersList.add(newPreOrder);
        return newPreOrder;
    }

    /**
     * Given a new estimated entry time, this func will update the order's entry.
     * assuming here the only PreOrder class has estimated entry time.
     * @param order
     * @param estimatedEntryTime : the new entry time.
     * @return order with the new entry time updated.
     */
    public Order changeEstimatedEntryTimeOfOrder(PreOrder order, Date estimatedEntryTime){
        order.setEstimatedEntryTime(estimatedEntryTime);
        return order;
    }


    //TODO : Add different options to reach the specific order maybe using the customer's profile or so.
    /**
     *  When a customer wants to finish his order (and move the car out) this function is called,
     *  with the current time as the exitTime of the customer and the order's price is calculated.
     * @param order : the customer's order to be finished
     * @return order : the finished order with the final price updated
     */
    public Order finishOrder(Order order, PriceList pricePerHour){
        order.setActualExitTime(new Date());
        order.setPrice(calculateCharge(order.getEntryTime(), order.getActualExitTime(),pricePerHour));
        return order;
    }

    //TODO : move to billing once the class is made.

    public enum PriceList {
        ONE_TIME_PARKING(5), PRE_ORDER_ONE_TIME_PARKING(4),
        MONTHLY_REGULAR_SUBSCRIPTION(60*PRE_ORDER_ONE_TIME_PARKING.getPrice()),
        MONTHLY_REGULAR_MULTIPLE_SUBSCRIPTIONS_PER_CAR(54*PRE_ORDER_ONE_TIME_PARKING.getPrice()),
        MONTHLY_FULL_SUBSCRIPTION(72*PRE_ORDER_ONE_TIME_PARKING.getPrice())
        ;
        private final double price;

        public double getPrice(){
            return  this.price;
        }

        PriceList(double price) {
            this.price = price;
        }
    }

    private double calculateCharge(Date entryTime, Date exitTime, PriceList pricePerHour ){
        double minutes = TimeUnit.MILLISECONDS.toMinutes(Math.abs(exitTime.getTime() - entryTime.getTime()));
        return (minutes * pricePerHour.getPrice()) / 60 ;
    }
}
