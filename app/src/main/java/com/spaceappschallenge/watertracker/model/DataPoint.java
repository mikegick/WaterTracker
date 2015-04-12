package com.spaceappschallenge.watertracker.model;

public class DataPoint {
    private double latitude;
    private double longitude;
    private String discDate;
    private String category;
    private int dataPointID;
    private String purpose;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDiscDate() {
        return discDate;
    }

    public void setDiscDate(String discDate) {
        this.discDate = discDate;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public int getDataPointID() {
        return dataPointID;
    }

    public void setDataPointID(int dataPointID) {
        this.dataPointID = dataPointID;
    }

}
