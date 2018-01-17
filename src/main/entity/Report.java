package entity;

import java.util.Date;
import java.util.Objects;

import static controller.Controllers.employeeController;

public class Report {
    /**
     * Private members.
     * _managerID ID of report's responsible manager initiator.
     * _generationDate The date of the generation of such report.
     * _type The type of the report.
     */
    private Integer _managerID; // will be 999 if made by Tasker.
    private Date _generationDate;
    private ReportType _type;
    private Integer _reportID; //to be generated from the DB...

    /**
     * Report different types.
     */
    public enum ReportType{
        DAILY_FINISHED_ORDERS,
        DAILY_CANCELED_ORDERS,
        DAILY_LATED_ORDERS,
        QUARTERLY_ORDERS,
        QUARTERLY_COMPLAINTS,
        QUARTERLY_UNAVAILABLE_PARKING_SPACES
    }

    /**
     * Class constructor
     * @param managerID ID of report's responsible manager initiator.
     * @param type The type of the report.
     */
    public Report(Integer managerID, ReportType type) {
        this._managerID = managerID;
        this._generationDate = new Date();
        this._type = type;
    }

    /**
     * Getters and setters
     */
    public Integer getManagerID() {
        return _managerID;
    }

    public void setManagerID(Integer managerID) {
        this._managerID = managerID;
    }

    public Date getGenerationDate() {
        return _generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this._generationDate = generationDate;
    }

    public ReportType getType() {
        return _type;
    }

    public void setType(ReportType type) {
        this._type = type;
    }

    public Integer getReportID() {
        return _reportID;
    }

    public void setReportID(Integer reportID) {
        this._reportID = reportID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Report report = (Report) o;
        return Objects.equals(_managerID, report._managerID) &&
                Objects.equals(_generationDate, report._generationDate) &&
                _type == report._type;
    }

    @Override
    public String toString() {
        return "Report of type: " + this._type + ", " +
                "initiated by manager: " + employeeController.getEmployeeByID(this._managerID).getName() + ", " +
                "of generation date=" + _generationDate + ".";

    }
}
