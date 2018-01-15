package controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import entity.Order;
import entity.ParkingLot;
import entity.ParkingSpace;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
        for(int i = 1; i <= this._parkingLotList.get(parkingLotNumber).getDepth(); i++){
            for(int j = 1; j <= this._parkingLotList.get(parkingLotNumber).getWidth(); j++){
                for(int k=1; k <= _parkingLotList.get(parkingLotNumber).getHeight(); k++){
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

        if(this._parkingLotList.get(parkingLotNumber).getIsFullState()){
            return false;
        }

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
     * @param parkingLotNumber The number of the parking lot to export its status map.
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

    /**
     * This function exports the current status map of the parking lot to a pdf file.
     * The map will show such symbols
     * F : Free space
     * S : Ordered space
     * O : Occupied space
     * X : Unavailable space
     * @param parkingLotNumber The number of the parking lot to export its status map.
     */
    public void createPDF(Integer parkingLotNumber) throws FileNotFoundException, DocumentException {
        String str = "";
        String newline = System.getProperty("line.separator");
        Integer depthNum = this._parkingLotList.get(parkingLotNumber).getDepth();
        String[] result = new String[depthNum];
        // emptying the result string to start working on it.
        for(int i = 0 ; i < result.length ; i++){
            result[i] = "";
        }

        Document doc = new Document();
        //The pdf file will be created and stored in the same projec folder.
        PdfWriter.getInstance(doc, new FileOutputStream("Report.pdf"));
        doc.open();

        for(int i=1; i<= this._parkingLotList.get(parkingLotNumber).getDepth(); i++){
            result[i] += "Depth " + Integer.toString((i));
            doc.add(new Paragraph(result[i]));
//            doc.setMargins(50, 50, 50, 50);
            result[i]="";
            for(int j = 1; j<= this._parkingLotList.get(parkingLotNumber).getWidth(); j++){
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
                result[i] += newline;
                doc.add(new Paragraph(result[i]));
                result[i] = "";
            }
        }
        str += newline + newline + newline + newline;
        str += "____________________________________________\n";
        str += "* F : Free space \n " +
                "* S : Ordered space \n " +
                "* O : Occupied space \n " +
                "* X : Unavailable space \n";
        doc.add(new Paragraph(str));
        doc.close();
    }

    /**
     * This function is responsible for setting a parking space status due to the employee's requirement.
     * @param parkingLotNumber The number of the parking lot to export its status map.
     * @param status The parking space status to set.
     * @param x The parking space coordinate in width axes.
     * @param y The parking space coordinate in height axes.
     * @param z The parking space coordinate in depth axes.
     */
    public void setParkingSpaceStatus(Integer parkingLotNumber, ParkingSpace.ParkingStatus status, Integer x, Integer y, Integer z){
        this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[z][x][y].setStatus(status);
    }

    public void reserveParkingSpace(Integer parkingLotNumber, Integer orderID , Integer x, Integer y, Integer z){
        this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[z][x][y].setOccupyingOrderID(orderID);
        this._parkingLotList.get(parkingLotNumber).getParkingSpaceMatrix()[z][x][y].setStatus(ParkingSpace.ParkingStatus.ORDERED);
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
}



