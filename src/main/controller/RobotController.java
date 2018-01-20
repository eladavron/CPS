package controller;

import entity.Robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RobotController {
    private static RobotController instance;

    static {
        instance = new RobotController();
    }
    public static RobotController getInstance() { return instance; }

    Map<Integer, Robot> _listOfRobots = new HashMap<>();


    public void createRobot(Integer parkingLotID){
        Robot robot = new Robot(parkingLotID);
        _listOfRobots.put(parkingLotID, robot);
        if (Controllers.IS_DEBUG_CONTROLLER)
        {
            System.out.println("Hello from "+ robot.toString());
        }
    }

    public void  removeRobot(Integer parkingLotID){
        _listOfRobots.remove(parkingLotID);
    }

    /**
     * Insert car to parking lot algorithm
     * @param carIDtoPut
     * @param parkingLotId
     * @param heightWidthDepthNewCarLocation
     * @param oldCarIDsToHeightWidthDepth
     * @return
     */
    public boolean insertCarToParkingLot(Integer carIDtoPut, Integer parkingLotId, ArrayList<Integer> heightWidthDepthNewCarLocation , Map<Integer,ArrayList<Integer>> oldCarIDsToHeightWidthDepth){

        return true;
    }

    /**
     * Extract car from parking lot and fix the state algorithm.
     * @param carIDtoExtract
     * @param parkingLotId
     * @param heightWidthDepthCarLocation
     * @param CarIDsToHeightWidthDepth
     * @return
     */
    public boolean extractCarFromParkingLot(Integer carIDtoExtract, Integer parkingLotId, ArrayList<Integer> heightWidthDepthCarLocation , Map<Integer,ArrayList<Integer>> CarIDsToHeightWidthDepth){

        return true;
    }

    /**
     * Remove car due to late in pickup etc.
     * @param carIDtoRemove
     * @param parkingLotId
     * @param heightWidthDepthCarLocation
     * @param CarIDsToHeightWidthDepth
     * @return
     */
    public boolean removeCarFromParkingLot(Integer carIDtoRemove, Integer parkingLotId, ArrayList<Integer> heightWidthDepthCarLocation , Map<Integer,ArrayList<Integer>> CarIDsToHeightWidthDepth){

        return true;
    }

}