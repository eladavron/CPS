package entity;

import java.util.ArrayList;
import java.util.Date;

/**
 * Beep boop beep.
 */
public class Robot {

    /**
     * Private attributes
     */
    private Integer _parkingLot;
    private String _friendlyName;
    private ArrayList<ParkedCar> _parkedCarList = new ArrayList<>();
    private Coordinates _parkingLotSize;


    public Robot(Integer _parkingLot, Integer depth, Integer width, Integer height) {
        this._parkingLot = _parkingLot;
        this._friendlyName = "Friendly Robot for parking lot "+ _parkingLot;
        _parkingLotSize = new Coordinates(depth, width, height);
    }

    public void  extractCarByRobot(Integer carId, ArrayList<ParkedCar> carsToRemove, ArrayList<ParkedCar> carsToPut){
        removeFromList(carsToRemove);
        addToList(carsToPut);
        // update XYZ
        for (ParkedCar car : _parkedCarList){
            Integer idOfCarInRobotsList = _parkedCarList.lastIndexOf(car);
            Coordinates cords = createCoordinates();
            cords.setCoordinatesByInt(idOfCarInRobotsList, _parkingLotSize._depth, _parkingLotSize._height);
            car.setCoordinates(cords); // updated XYZ!
        }
    }

    public void  InsertCarByRobot(Integer carId, ArrayList<ParkedCar> carsToRemove, ArrayList<ParkedCar> carsToPut){
        removeFromList(carsToRemove);
        addToList(carsToPut);
        // update XYZ
        for (ParkedCar car : _parkedCarList){
            Integer idOfCarInRobotsList = _parkedCarList.lastIndexOf(car);
            Coordinates cords = createCoordinates();
            cords.setCoordinatesByInt(idOfCarInRobotsList, _parkingLotSize._depth, _parkingLotSize._height);
            car.setCoordinates(cords); // updated XYZ!
        }
    }


    /**
     * Getters and Setters
     */
    public Integer getParkingLot() {
        return _parkingLot;
    }

    public void setParkingLot(Integer parkingLot) {
        this._parkingLot = parkingLot;
    }

    public String getFriendlyName() {
        return _friendlyName;
    }

    public Coordinates getParkingLotSize() {
        return _parkingLotSize;
    }

    public void setFriendlyName(String friendlyName) {
        this._friendlyName = friendlyName;
    }

    public ArrayList<ParkedCar> getParkedCarList() {
        return _parkedCarList;
    }

    public ParkedCar getParkedCarByID (Integer carId){
        for (ParkedCar car : _parkedCarList){
            if (car.getCarId() == carId)
                return car;
        }
        return null;
    }

    /**
     * Removes cars of robot's list
     * @param toRemove
     */
    public void removeFromList(ArrayList<ParkedCar> toRemove){
        for (ParkedCar car : toRemove){
            _parkedCarList.remove(car);
        }

    }

    /**
     * Inserts cars to robot's list
     * @param toInsert
     */
    public void addToList(ArrayList<ParkedCar> toInsert){
        for (ParkedCar car : toInsert){
            _parkedCarList.add(car);
        }
    }

    /**
     *
     * @param carId
     * @param estimatedExitTime
     * @return
     */
    public ParkedCar createParkedCar(Integer carId, Date estimatedExitTime){
        return new ParkedCar(carId, estimatedExitTime);
    }

    public Coordinates createCoordinates(){
        return new Coordinates();
    }

    @Override
    public String toString() {
        return getFriendlyName();
    }

    /**
     * depth, width, height
     */
    public class Coordinates{
        Integer _depth;
        Integer _width;
        Integer _height;

        public Coordinates(){};

        public Coordinates(Integer depth, Integer width, Integer height) {
            this._depth = depth;
            this._width = width;
            this._height = height;
        }

        public Integer getInt(Integer  parkingLotDepth,/* Integer parkingLotWidth,*/ Integer parkingLotHeight){
            return  (_depth * parkingLotDepth + _height) * parkingLotHeight + _width;

        }

        public void setCoordinatesByInt(Integer index, Integer  parkingLotDepth,/* Integer parkingLotWidth,*/ Integer parkingLotHeight){

            this._width = index % parkingLotHeight;
            this._height = ((index - _width) / parkingLotHeight) % parkingLotDepth;
            this._depth = ((((index - _width) / parkingLotHeight) - _height) / parkingLotDepth);

        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Coordinates)) return false;

            Coordinates cord = (Coordinates) obj;
            return (this._depth.equals(cord._depth) && (this._height.equals(cord._height)) && (this._width.equals(cord._depth)));
        }
    }

    /**
     * coordinates, carid, estimatedExitTime
     */
    public class ParkedCar implements Comparable<ParkedCar>{
        private Coordinates _coordinates;
        private Integer _carID;
        private java.util.Date _estimatedExitTime;

        public ParkedCar(Integer _carID, Date _estimatedExitTime) {
            this._carID = _carID;
            this._estimatedExitTime = (_estimatedExitTime != null) ? _estimatedExitTime : new Date(0) ;
        }

        public void setCoordinates(Coordinates coordinates) {
            this._coordinates = coordinates;
        }

        public Coordinates getCoordinates() {
            return _coordinates;
        }
        public Integer getCarId(){
            return  _carID;
        }


        @Override
        public int compareTo(ParkedCar o) {
            return _estimatedExitTime.compareTo(o._estimatedExitTime);
        }

        @Override
        public String toString() {
            return "Parked car in robot's list.\n"+
                    " parkingLot " + _parkingLot +
                    " carId " + _carID + " estimated exit time: " + _estimatedExitTime + "\n\n";
        }
    }

}
