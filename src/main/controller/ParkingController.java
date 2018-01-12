package controller;

import entity.*;
import java.util.HashMap;
import java.util.Map;

import static controller.Controllers.orderController;


/**
 * Parking controller is responsible of managing and functioning the parking lot.
 */
public class ParkingController {

    private Map<Integer, ParkingLot> _parkingLotList;
    private Integer _numberOfSlotsOccupied;


    private static ParkingController parkingInstance = new ParkingController();
    private Integer _heightNumOccupied;
    private Integer _widthNumOccupied;
    private Integer _depthNumOccupied;

    /**
     * A private default constructor which prevents any other class from creating another instance.
     */
    private ParkingController() {
        this._parkingLotList = new HashMap<>();
        this._numberOfSlotsOccupied = 0;
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
     */
    public void initiateParkingLot(Integer parkingLotNumber){
        for(int i = 1; i <= this._parkingLotList.get(parkingLotNumber).getHeight(); i++){
            for(int j = 1; j <= this._parkingLotList.get(parkingLotNumber).getWidth(); j++){
                for(int k=1; k <= _parkingLotList.get(parkingLotNumber).getDepth(); k++){
                    _parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[i][j][j].setStatus(ParkingSpace.ParkingStatus.FREE);
                }
            }
        }
    }

    /**
     * Enter parking lot by giving the order id of the parking space. Set its status to OCCUPIED and set its occupying
     * order ID to the exact one.
     * @param orderID The occupying ID of the parking space to be exited of.
     * @return true if successfully entered, false if the parking lot is full.
     */
    public boolean enterParkingLot(Integer orderID){
        Order order = orderController.getOrder(orderID);
        Integer parkingLotNumber = order.getParkingLotNumber();
        initCurrentParkingValues(parkingLotNumber);
        String status = checkAndUpdateBoundaries(parkingLotNumber);

        if(status.equals("FULL")){
            return false;
        }
        else{
            this._numberOfSlotsOccupied += 1;
            this._parkingLotList.get(parkingLotNumber).setHeightNumOccupied(++_heightNumOccupied);
            _heightNumOccupied = this._parkingLotList.get(parkingLotNumber).getHeightNumOccupied();
            this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[_depthNumOccupied][_widthNumOccupied][_heightNumOccupied].setOccupyingOrderID(orderID);
            this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[_depthNumOccupied][_widthNumOccupied][_heightNumOccupied].setStatus(ParkingSpace.ParkingStatus.OCCUPIED);
            return true;
        }
    }

    /**
     * Exit parking lot by giving the order id of the parking space. Set its status to FREE and set its occupying
     * order ID to -1.
     * @param orderID The occupying ID of the parking space to be exited of.
     */
    public void exitParkingLot(Integer orderID){
        Order order = orderController.getOrder(orderID);
        Integer parkingLotNumber = order.getParkingLotNumber();

        initCurrentParkingValues(parkingLotNumber);

        this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[_depthNumOccupied][_widthNumOccupied][_heightNumOccupied].setOccupyingOrderID(-1);
        this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[_depthNumOccupied][_widthNumOccupied][_heightNumOccupied].setStatus(ParkingSpace.ParkingStatus.FREE);
        this._numberOfSlotsOccupied -= 1;

        updateAfterExit(parkingLotNumber);
    }


        /**
         * This methods checks whether our current occupied slots indicators are valid and whether we have an empty
         * slot to park on.
         * @return FULL if the parking lot is full and we can't park or VALID if we still have space and we can park.
         */
    public String checkAndUpdateBoundaries(Integer parkingLotNumber){


        initCurrentParkingValues(parkingLotNumber);

        if(this._heightNumOccupied.equals(this._parkingLotList.get(parkingLotNumber).getHeight())){
            if(this._widthNumOccupied.equals(this._parkingLotList.get(parkingLotNumber).getWidth())){
                if(this._depthNumOccupied.equals(this._parkingLotList.get(parkingLotNumber).getDepth())){
                    return "FULL";
                }
                else{
                    this._parkingLotList.get(parkingLotNumber)
                            .setDepthNumOccupied(_depthNumOccupied +1);
                    this._parkingLotList.get(parkingLotNumber)
                            .setWidthNumOccupied(1);
                    this._parkingLotList.get(parkingLotNumber)
                            .setHeightNumOccupied(1);
                }
            }
            else{
                this._parkingLotList.get(parkingLotNumber)
                        .setWidthNumOccupied(_widthNumOccupied + 1);
                this._parkingLotList.get(parkingLotNumber)
                        .setHeightNumOccupied(1);
            }
        }
        else{
            this._parkingLotList.get(parkingLotNumber)
                    .setHeightNumOccupied(_heightNumOccupied + 1);
        }
        return "VALID";
    }

    /**
     * Update the current occupied slots indicators after an exit operation.
     * @param parkingLotNumber
     */
    public void updateAfterExit(Integer parkingLotNumber){
        initCurrentParkingValues(parkingLotNumber);
        if (this._heightNumOccupied == 1){
            if(this._widthNumOccupied == 1){
                this._parkingLotList.get(parkingLotNumber)
                        .setDepthNumOccupied(_depthNumOccupied - 1);
                this._parkingLotList.get(parkingLotNumber)
                        .setHeightNumOccupied(this._parkingLotList.get(parkingLotNumber).getHeight());
                this._parkingLotList.get(parkingLotNumber)
                        .setWidthNumOccupied(this._parkingLotList.get(parkingLotNumber).getWidth());
            }
            else{
                this._parkingLotList.get(parkingLotNumber)
                        .setWidthNumOccupied(_widthNumOccupied -1);
                this._parkingLotList.get(parkingLotNumber)
                        .setHeightNumOccupied(this._parkingLotList.get(parkingLotNumber).getHeight());
            }
        }
        else{
            this._parkingLotList.get(parkingLotNumber)
                    .setHeightNumOccupied(_heightNumOccupied -1 );
        }
    }

    //TODO: This function will be completed on next branch.
    /**
     * This function exports the current status map of the parking lot as a pdf file.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
//    public void getCurrentStatusMap(Integer parkingLotNumber){
//
//        try {
//            File file = new File("statusMap.pdf");
//
//            // creates the file
//            file.createNewFile();
//
//            // creates a FileWriter Object
//            FileWriter bw = new FileWriter(file);
//
//            // Writes the content to the file
//            for(int i = 0; i< this._parkingLotList.getHeight(); i++){
//                for(int j = 0; j < this._parkingLotList.getWidth(); j++){
//                    switch (this._parkingLotList.getParkingSpaceMatrix()[parkingLotNumber][i][j].getStatus()){
//                        case FREE:
//                            bw.write(" O ");
//                            break;
//                        case ORDERED:
//                            bw.write(" T ");
//                            break;
//                        case OCCUPIED:
//                            bw.write(" D ");
//                            break;
//                        case UNAVAILABLE:
//                            bw.write(" X ");
//                            break;
//                    }
//                }
//                System.out.println();
//            }
//            bw.close();
//        }catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
}


