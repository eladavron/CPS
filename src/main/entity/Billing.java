package entity;

/**
 *  An entity class to posses all the data that is needed for BillingController
 */
public class Billing {

    /**
     * A Billing constructor that can set initial prices.
     * @param oneTimeParkingPrice Inital price for one time parking.
     * @param preOrderOneTimeParkingPrice Initial price for Pre-order one time parking.
     */
    public Billing(Integer oneTimeParkingPrice, Integer preOrderOneTimeParkingPrice) {
        priceList.PRE_ORDER_ONE_TIME_PARKING.setPrice(preOrderOneTimeParkingPrice);
        priceList.ONE_TIME_PARKING.setPrice(oneTimeParkingPrice);
    }

    /**
     * A Billing constructor that uses the default prices.
     */
    public Billing() {
    }

    public enum priceList {
        ONE_TIME_PARKING(5),
        PRE_ORDER_ONE_TIME_PARKING(4),
        MONTHLY_REGULAR_SUBSCRIPTION(60*PRE_ORDER_ONE_TIME_PARKING.getPrice()),
        MONTHLY_REGULAR_MULTIPLE_SUBSCRIPTIONS_PER_CAR(54*PRE_ORDER_ONE_TIME_PARKING.getPrice()),
        MONTHLY_FULL_SUBSCRIPTION(72*PRE_ORDER_ONE_TIME_PARKING.getPrice()),
        NO_CHARGE_DUE_TO_SUBSCRIPTION(0)
        ;
        private double price;

        public double getPrice(){
            return  this.price;
        }

        private void setPrice(double price){
            this.price = price;
        }

        /**
         * Enum C'tor
         * @param price
         */
        priceList(double price) {
            this.price = price;
        }

    }

    public String getPriceList(){
       return priceList.values().toString();
    }

    public double getPrice(String priceType){
        return priceList.valueOf(priceType).getPrice();
    }
}
