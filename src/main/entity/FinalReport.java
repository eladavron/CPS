package entity;

import java.util.Date;

/**
 * A class representing an already generated report as queried by the database.
 * It's intent is to be displayed to the company manager only!
 */
public class FinalReport {
    private String _fullContent;
    private String _shortDescription;
    private Integer _id;
    private Date _timeCreated;

    public FinalReport() {
    }

    public FinalReport(Integer id, String shortDescription, String fullContent, Date timeCreated) {
        this._fullContent = fullContent;
        this._shortDescription = shortDescription;
        this._id = id;
        this._timeCreated = timeCreated;
    }

    //region Getters and Setters
    public String getFullContent() {
        return _fullContent;
    }

    public void setFullContent(String fullContent) {
        this._fullContent = fullContent;
    }

    public String getShortDescription() {
        return _shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this._shortDescription = shortDescription;
    }

    public Integer getId() {
        return _id;
    }

    public void setId(Integer id) {
        this._id = id;
    }

    public Date getTimeCreated() {
        return _timeCreated;
    }

    public void setTimeCreated(Date timeCreated) {
        this._timeCreated = timeCreated;
    }
    //endregion
}
