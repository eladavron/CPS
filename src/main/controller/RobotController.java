package controller;

import entity.ParkingLot;
import entity.Robot;

import java.util.*;

import static controller.Controllers.parkingController;

public class RobotController {
    private static RobotController instance;

    static {
        instance = new RobotController();
    }
    public static RobotController getInstance() { return instance; }

    Map<Integer, Robot> _listOfRobots = new HashMap<>();


    /**
     * Ctor for robot
     * @param parkingLotID
     * @param parkingLotDepth
     * @param parkingLotWidth
     * @param parkingLotHeigt
     */
    public void createRobot(Integer parkingLotID, Integer parkingLotDepth, Integer parkingLotWidth, Integer parkingLotHeigt){
        Robot robot = new Robot(parkingLotID, parkingLotDepth, parkingLotWidth, parkingLotHeigt);
        _listOfRobots.put(parkingLotID, robot);
        populateRobotOnInit();
        if (Controllers.IS_DEBUG_CONTROLLER)
        {
            System.out.println("Hello from "+ robot.toString());
        }
    }

    public void populateRobotOnInit(){
    }

    public void  removeRobot(Integer parkingLotID){
        _listOfRobots.remove(parkingLotID);
    }

    /**
     * Insert car to parking lot algorithm
     * @param carIdToPut
     * @param parkingLotId
     * @param estimatedExitTime
     * @return
     */
    public boolean insertCarToParkingLot(Integer carIdToPut, Integer parkingLotId, Date estimatedExitTime){
        try {
            Robot myRobot = _listOfRobots.get(parkingLotId);
            ArrayList<Robot.ParkedCar> listFromRobot = new ArrayList<>();
            listFromRobot.addAll(myRobot.getParkedCarList());

            Robot.ParkedCar myParkedCar = myRobot.createParkedCar(carIdToPut,estimatedExitTime);

            Integer parkingLotDepth = parkingController.getParkingLotByID(parkingLotId).getDepth();
            Integer parkingLotHeight = parkingController.getParkingLotByID(parkingLotId).getHeight();


            listFromRobot.add(myParkedCar);
            Collections.sort(listFromRobot);

            Integer idOfCarInRobotsList = listFromRobot.lastIndexOf(myParkedCar);

            // update XYZ for new car.
            Robot.Coordinates cords = myRobot.createCoordinates();
            cords.setCoordinatesByInt(idOfCarInRobotsList, parkingLotDepth, parkingLotHeight);
            myParkedCar.setCoordinates(cords);

            // list of action for robot
            ArrayList<Robot.ParkedCar> carsToPutOut  = new ArrayList<>();
            ArrayList<Robot.ParkedCar> carsToInsert  = new ArrayList<>();
            carsToInsert.add(myParkedCar);

            boolean flagFoundCar = false;

            for (Robot.ParkedCar car : listFromRobot){
                if (car == myParkedCar){
                    flagFoundCar = true;
                    continue;
                }
                // update XYZ everyTime for consistency (can be used under the if)
                idOfCarInRobotsList = listFromRobot.lastIndexOf(car);
                cords = myRobot.createCoordinates();
                cords.setCoordinatesByInt(idOfCarInRobotsList, parkingLotDepth, parkingLotHeight);
                car.setCoordinates(cords); // updated XYZ!

                if (flagFoundCar) {
                    //Robot.ParkedCar temp = car;
                    carsToPutOut.add(car);
                    carsToInsert.add(car);
                }

            }

            myRobot.InsertCarByRobot(carIdToPut, carsToPutOut, carsToInsert);


            return true;
        } catch (Exception e) {
            if (Controllers.IS_DEBUG_CONTROLLER) {
                e.printStackTrace();
                throw e;
            }
            return true;
        }
    }

    /**
     * Extract car from parking lot and fix the state algorithm.
     * @param carIdToExtract
     * @param parkingLotId
     * @return
     */
    public boolean extractCarFromParkingLot(Integer carIdToExtract, Integer parkingLotId) {
        try {
            Robot myRobot = _listOfRobots.get(parkingLotId);
            ArrayList<Robot.ParkedCar> listFromRobot = myRobot.getParkedCarList();

            Robot.ParkedCar myParkedCar = myRobot.getParkedCarByID(carIdToExtract);
            if (myParkedCar == null)
                return true; // car not found. return true so we won't break anything.

            // list of action for robot
            ArrayList<Robot.ParkedCar> carsToPutOut  = new ArrayList<>();
            ArrayList<Robot.ParkedCar> carsToInsert  = new ArrayList<>();
            carsToPutOut.add(myParkedCar);

            boolean flagFoundCar = false;

            for (Robot.ParkedCar car : listFromRobot){
                if (car == myParkedCar){
                    flagFoundCar = true;
                    continue;
                }

                if (flagFoundCar) {
                    //Robot.ParkedCar temp = car;
                    carsToPutOut.add(car);
                    carsToInsert.add(car);
                }
            }

            myRobot.extractCarByRobot(carIdToExtract, carsToPutOut, carsToInsert);

            return true;
        } catch (Exception e) {
            if (Controllers.IS_DEBUG_CONTROLLER) {
                e.printStackTrace();
                throw e;
            }
            return true;
        }
    }



}