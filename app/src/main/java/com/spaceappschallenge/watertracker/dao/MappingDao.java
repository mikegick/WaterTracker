package com.spaceappschallenge.watertracker.dao;

import com.spaceappschallenge.watertracker.model.DataPoint;

import java.util.List;

/**
 * Created by Andrew on 4/11/2015.
 */
public interface MappingDao {
    public Integer addDataPoint(DataPoint dataPoint);
    public String modifyDataPoint(int userId,int dpId, String category);
    public DataPoint[] retrieveDataPoints(double maxLatitude,double minLatitude,double maxLongitude,double minLongitude);
    public String recentUserDataPoints(int userId);
}
