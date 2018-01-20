package controller;

import Exceptions.CustomerNotificationFailureException;
import Exceptions.NotImplementedException;
import entity.Order;
import entity.ParkingLot;
import entity.ParkingSpace;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.*;
import static entity.ParkingSpace.ParkingStatus.FREE;

/**
 * Parking controller is responsible of managing and functioning the parking lot.
 */
public class ParkingController {

    private Map<Integer, ParkingLot> _parkingLotList;
    private ArrayList<Integer> _parkedCarList;

    private static ParkingController parkingInstance = new ParkingController();
    private Integer _heightNumOccupied;
    private Integer _widthNumOccupied;
    private Integer _depthNumOccupied;

    /**
     * A private default constructor which prevents any other class from creating another instance.
     */
    private ParkingController(){
        _parkingLotList = new HashMap<>() ;
        _parkedCarList = new ArrayList<>();
        // get all parked car on init.
        addAllParkedCars();
    }

    /** Static 'instance' method */
    public static ParkingController getInstance() {
        return parkingInstance;
    }

    private void initCurrentParkingValues(Integer parkingLotNumber)
    {
        _heightNumOccupied = this._parkingLotList.get(parkingLotNumber).getHeightNumOccupied();
        _widthNumOccupied = this._parkingLotList.get(parkingLotNumber).getWidthNumOccupied();
        _depthNumOccupied = this._parkingLotList.get(parkingLotNumber).getDepthNumOccupied();
    }
    /**
     * Initiating parking lot of number "parkingLotNumber" and settings its parking spaces as FREE.
     * @param parkingLotNumber The number of the parking lot to initiate.
     * @param parkingSpaces list of existing spaces on DB for this parking lot.
     */
    private void initiateParkingLot(Integer parkingLotNumber, ArrayList<ParkingSpace> parkingSpaces) throws SQLException
    {
        if (Controllers.IS_DEBUG_CONTROLLER)
        {
            System.out.println("Starting Init of parking lot number: " + parkingLotNumber );
        }
        ParkingLot thisParkingLot = this._parkingLotList.get(parkingLotNumber);
        for (ParkingSpace parkingSpace : parkingSpaces)
        {
            thisParkingLot.getParkingSpaceMatrix()
                    [parkingSpace.getDepth()]
                    [parkingSpace.getWidth()]
                    [parkingSpace.getHeight()]
                    = parkingSpace
            ;
        }
        for(int i = 1; i <= thisParkingLot.getDepth(); i++)
        {
            for(int j = 1; j <= thisParkingLot.getWidth(); j++)
            {
                for(int k=1; k <= thisParkingLot.getHeight(); k++)
                {
                    ParkingSpace currentParkingSpace = thisParkingLot.getParkingSpaceMatrix()[i][j][k];
                    if (currentParkingSpace ==  null)
                    {
                        // Meaning this is a new parkingSpace unknown to DB
                        currentParkingSpace = new ParkingSpace();
                        currentParkingSpace.setDepth(i);
                        currentParkingSpace.setWidth(j);
                        currentParkingSpace.setHeight(k);
                        dbController.updateParkingSpace(parkingLotNumber, currentParkingSpace);
                        thisParkingLot.getParkingSpaceMatrix()[i][j][k] = currentParkingSpace;
                    }
                }
            }
        }
        if (Controllers.IS_DEBUG_CONTROLLER) {
            System.out.println("Done.");
        }
    }

    public void initiateParkingLot(Integer parkingLotNumber) throws SQLException{
        initiateParkingLot(parkingLotNumber,dbController.getParkingSpaces(parkingLotNumber));
    }

    public void initiateParkingLots() throws SQLException {
        for (Integer parkingLotID: _parkingLotList.keySet()) {
            robotController.createRobot(parkingLotID);
            initiateParkingLot(parkingLotID, dbController.getParkingSpaces(parkingLotID));
        }
    }

    /**
     * Enter parking lot by giving the order id of the parking space. Set its status to OCCUPIED and set its occupying
     * order ID to the exact one.
     * @param orderID The occupying ID of the parking space to be exited of.
     * @return true if successfully entered, false if the parking lot is full.
     */
    public boolean enterParkingLot(Integer orderID)throws Exception
    {
        Order order = orderController.getOrder(orderID);
        Integer parkingLotNumber = order.getParkingLotNumber();

        // first check if car already parked.
        if (checkCarAlreadyParked(order.getCarID()))
        {
            throw new CustomerNotificationFailureException("Your car is already parked in our lot!");
        }

        if(this._parkingLotList.get(parkingLotNumber).getIsFullState()){
            return false;
        }

        initCurrentParkingValues(parkingLotNumber);
        String status = checkAndUpdateBoundaries(parkingLotNumber);

        if(status.equals("FULL"))
        {
            throw new CustomerNotificationFailureException("Parking lot is full, redirect yourself to another parking lot!");
        }
        else if (!addCarAsParked(order.getCarID(), orderID, parkingLotNumber))
        {
            throw new CustomerNotificationFailureException("Your request cannot be currently processed, please contact customer service at Oops@iDidItAgain.com!");
        }
        else
        {
            //Car is inserted onto the ParkingLot in position :(depth,width,height)
            ParkingSpace currentParkingSpace = this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[_depthNumOccupied][_widthNumOccupied][_heightNumOccupied];
            currentParkingSpace.setOccupyingOrderID(orderID);
            currentParkingSpace.setStatus(ParkingSpace.ParkingStatus.OCCUPIED);
            this._parkingLotList.get(parkingLotNumber).setHeightNumOccupied(_heightNumOccupied + 1);
            orderController.setOrderHeightWidthDepth(orderID, _heightNumOccupied, _widthNumOccupied, _depthNumOccupied); // TODO: fill in actual values. here or in other place.
            robotController.insertCarToParkingLot(order.getCarID(), order.getParkingLotNumber(),null, null);
            return dbController.updateParkingSpace(parkingLotNumber, currentParkingSpace);
        }
    }

    /**
     * Exit parking lot by giving the order id of the parking space. Set its status to FREE and set its occupying
     * order ID to -1.
     * @param orderID The occupying ID of the parking space to be exited of.
     * @return on failure false.
     */
    public boolean exitParkingLot(Integer orderID) throws Exception
    {
        Order order = orderController.getOrder(orderID);
        if (!checkCarAlreadyParked(order.getCarID()))
        {
            throw new CustomerNotificationFailureException("\nYour car is not parked in our parking lots." +
                                                           "\nLocate it elsewhere or you can call the cops!");
        }
        Integer parkingLotNumber = order.getParkingLotNumber();

        initCurrentParkingValues(parkingLotNumber);

        if (_heightNumOccupied > 1)
        {//this is just in case someone tried to exit before entering(somehow)
            _heightNumOccupied--;
        }
        ParkingSpace currentParkingSpace = this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[_depthNumOccupied][_widthNumOccupied][_heightNumOccupied];
        currentParkingSpace.setStatus(FREE);
        currentParkingSpace.setOccupyingOrderID(null);
        if (!unsetCarAsParked(order.getCarID()))
        {
            throw new CustomerNotificationFailureException("Your request cannot be currently processed, please contact customer service at Oops@iDidItAgain.com!");
        }
        updateAfterExit(parkingLotNumber);
        robotController.extractCarFromParkingLot(order.getCarID(), order.getParkingLotNumber(), null, null);

        return dbController.updateParkingSpace(parkingLotNumber,currentParkingSpace);
    }


    /**
     * This methods checks whether our current occupied slots indicators are valid and whether we have an empty
     * slot to park on.
     * @return FULL if the parking lot is full and we can't park or VALID if we still have space and we can park.
     */
    private String checkAndUpdateBoundaries(Integer parkingLotNumber){

        initCurrentParkingValues(parkingLotNumber);

        if(this._heightNumOccupied > this._parkingLotList.get(parkingLotNumber).getHeight())
        {
            if(this._widthNumOccupied.equals(this._parkingLotList.get(parkingLotNumber).getWidth()))
            {
                if(this._depthNumOccupied.equals(this._parkingLotList.get(parkingLotNumber).getDepth()))
                {
                    return "FULL";
                }
                else
                { //Then we didnt reach max depth yet.
                    this._parkingLotList.get(parkingLotNumber)
                            .setDepthNumOccupied(_depthNumOccupied+1);
                    this._parkingLotList.get(parkingLotNumber)
                            .setWidthNumOccupied(1);
                    this._parkingLotList.get(parkingLotNumber)
                            .setHeightNumOccupied(1);
                }
            }
            else
            { //Then we didnt reach max width yet.
                this._parkingLotList.get(parkingLotNumber)
                        .setWidthNumOccupied(_widthNumOccupied + 1);
                this._parkingLotList.get(parkingLotNumber)
                        .setHeightNumOccupied(1);
            }
        }
        //After updating the occupied spaces we want to continue with the new values so we call init again!
        initCurrentParkingValues(parkingLotNumber);
        return "VALID";
    }

    /**
     * Update the current occupied slots indicators after an exit operation.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
    public void updateAfterExit(Integer parkingLotNumber){
        initCurrentParkingValues(parkingLotNumber);
        if (this._heightNumOccupied == 1)
        { //Then we need to revert  Width.
            if(this._widthNumOccupied == 1)
            { //Then we need to revert Depth
                if (this._depthNumOccupied == 1)
                { //Then all is set to 1...parking lot is empty.

                }
                else
                { //We revert to the former depth which is a full width&height parking lot.
                    this._parkingLotList.get(parkingLotNumber)
                            .setDepthNumOccupied(_depthNumOccupied - 1);
                    this._parkingLotList.get(parkingLotNumber)
                            .setHeightNumOccupied(this._parkingLotList.get(parkingLotNumber).getHeight());
                    this._parkingLotList.get(parkingLotNumber)
                            .setWidthNumOccupied(this._parkingLotList.get(parkingLotNumber).getWidth());
                }
            }
            else
            { //We revert to the former width which is a full height parking lot.
                this._parkingLotList.get(parkingLotNumber)
                        .setWidthNumOccupied(_widthNumOccupied -1);
                this._parkingLotList.get(parkingLotNumber)
                        .setHeightNumOccupied(this._parkingLotList.get(parkingLotNumber).getHeight());
            }
        }
        else
        {  //We revert to the former height.
            this._parkingLotList.get(parkingLotNumber)
                    .setHeightNumOccupied(_heightNumOccupied -1 );
        }
    }

    /**
     * This function exports the current status map of the parking lot to a pdf file.
     * The map will show such symbols
     * F : Free space
     * S : Ordered space
     * O : Occupied space
     * X : Unavailable space
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
    public String generateParkingStatusReport(Integer parkingLotNumber){
        String str = "";
        Integer depthNum = this._parkingLotList.get(parkingLotNumber).getDepth();
        String[] result = new String[depthNum +1];
        // emptying the result string to start working on it.
        for(int i = 0 ; i < result.length ; i++){
            result[i] = "";
        }
        StringBuilder finalString = new StringBuilder();

        for(int i=1; i <= this._parkingLotList.get(parkingLotNumber).getDepth(); i++){
            result[i] += "Depth " + Integer.toString((i));

            finalString.append("\n").append(result[i]);
            result[i]="";
            for(int j = 1; j <= this._parkingLotList.get(parkingLotNumber).getWidth(); j++){
                for(int k = 1; k <= this._parkingLotList.get(parkingLotNumber).getHeight(); k++){
                    switch (this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[i][j][k].getStatus()){
                        case FREE:
                            result[i]  += "    F    ";
                            break;
                        case ORDERED:
                            result[i]  += "    S    ";
                            break;
                        case OCCUPIED:
                            result[i]  += "    O    ";
                            break;
                        case UNAVAILABLE:
                            result[i]  += "    X    ";
                            break;
                    }
                }
                result[i] += "\n";
                finalString.append("\n").append(result[i]);
                result[i] = "";
            }
        }
        str += "\n\n\n";
        str += "____________________________________________\n";
        str += "* F : Free space \n " +
                "* S : Ordered space \n " +
                "* O : Occupied space \n " +
                "* X : Unavailable space \n";
        finalString.append("\n").append(str);
        return finalString.toString();
    }

    /**
     * This function is responsible for setting a parking space status due to the employee's requirement.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     * @param status The parking space status to set.
     * @param width The parking space coordinate in width axes.
     * @param height The parking space coordinate in height axes.
     * @param depth The parking space coordinate in depth axes.
     */
    public void setParkingSpaceStatus(Integer parkingLotNumber, ParkingSpace.ParkingStatus status, Integer width, Integer height, Integer depth){
        this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[depth][width][height].setStatus(status);
    }

    //TODO: Try to wrap those 2 calls in one function.
    public void reserveParkingSpace(Integer parkingLotNumber, Integer orderID , Integer width, Integer height, Integer depth){
        this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[depth][width][height].setOccupyingOrderID(orderID);
        this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[depth][width][height].setStatus(ParkingSpace.ParkingStatus.ORDERED);
    }

    /**
     * Setting the specific parking lot as full.
     * TODO: direct the user to another parking lot (think about something nice).
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
    public void toggleParkingLotStatus(Integer parkingLotNumber){
        this._parkingLotList.get(parkingLotNumber)
                .setIsFullState(!(this._parkingLotList
                        .get(parkingLotNumber).getIsFullState())
                );
    }

    /**
     * Private function that retrieves the parkingLot list form the DB, on startup.
     */
    public ArrayList<Object> getParkingLots() throws SQLException{
        ArrayList<Object> parkingLots = dbController.getParkingLots();
        setParkingLotsList(parkingLots);
        initiateParkingLots();
        return parkingLots;
    }

    /**
     * Private function used in order to convert the general obj array into parkingLot
     * and then map it into our parkingLot list.
     * @param list - the parkingLot list taken from the DB.
     */
    private void setParkingLotsList(ArrayList<Object> list) {
        for (Object parkingLotObj : list)
        {
            ParkingLot parkingLot = (ParkingLot) parkingLotObj;
            _parkingLotList.put(parkingLot.getParkingLotID(), parkingLot);
        }
    }

    public  ParkingLot getParkingLotByID(Integer parkingLotNumber){
        return this._parkingLotList.get(parkingLotNumber);
    }

    private void addAllParkedCars(){ //TODO: throws SQLException{
        try{
            _parkedCarList.addAll(dbController.getAllParkedCars());
        }catch (SQLException e){
            //TODO: handle
        }
    }

    public boolean checkCarAlreadyParked(Integer carID) throws CustomerNotificationFailureException {
        return _parkedCarList.contains(carID);
    }

    private boolean addCarAsParked(Integer carID, Integer orderID, Integer parkingLotID) throws SQLException, CustomerNotificationFailureException {
        if (checkCarAlreadyParked(carID)) // precaution.
            return false;
        if (!dbController.setCarAsParked(carID, orderID, parkingLotID))
            return false;
        _parkedCarList.add(carID);
        return true;
    }

    private boolean unsetCarAsParked(Integer carID) throws SQLException, CustomerNotificationFailureException {
        if (!dbController.unsetCarAsParked(carID))
            throw new CustomerNotificationFailureException("Your request cannot be currently processed, please contact customer service at Oops@iDidItAgain.com!");
        _parkedCarList.remove(carID);
        return true;
    }

    /**
     * Gets order, finds the right place in DB according to estimated entry time
     * @param order new order to park
     * @return Map of carIds and new heightWidthDepth (For robot)
     */
    private Map<Integer, ArrayList<Integer>> calculateAndfixNewCarInParkingLot (Order order){

        throw new NotImplementedException();
    }

    /**
     * Gets order, locates it in the matrix. reorders the parking lot
     * @param order new order to park
     * @return Map of carIds and new heightWidthDepth (For robot)
     */
    private Map<Integer, ArrayList<Integer>> calculateAndfixExtractCarFromParkingLot (Order order){

        throw new NotImplementedException();
    }

    /**
     * This function is used in order to determine the actual size of the parking lot and return it.
     * this function is used in our controllers and will always use a real parking lot number.
     * @param parkingLotNumber - the parking lot in question
     * @return it's size.
     */
    public Integer getParkingLotSize(Integer parkingLotNumber)
    {
        ParkingLot parkingLot = _parkingLotList.get(parkingLotNumber);
        return  parkingLot.getDepth() * parkingLot.getHeight() * parkingLot.getWidth();
    }
}