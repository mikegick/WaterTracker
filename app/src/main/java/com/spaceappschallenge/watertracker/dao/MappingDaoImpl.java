package com.spaceappschallenge.watertracker.dao;

import com.spaceappschallenge.watertracker.model.DataPoint;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by Andrew on 4/11/2015.
 */
public class MappingDaoImpl implements MappingDao {
    private String url;

    @Override
    public Integer addDataPoint(DataPoint dataPoint) {
        final String fullUrl = "http://192.168.200.86:8080/mapping/water/addDataPoint?userID={userID}&latitude={latitude}&" +
                "longitude={longitude}&category={category}&purpose={purpose}";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        //userId defaulting to 1 for now until login system
        Integer response = restTemplate.getForObject(fullUrl, Integer.class,
                1,dataPoint.getLatitude(), dataPoint.getLongitude(),dataPoint.getCategory(),dataPoint.getPurpose());
        return response;
    }

    @Override
    public Integer modifyDataPoint(int userId, int dpId, String category, String purpose) {
        final String fullUrl = "http://192.168.200.86:8080/mapping/water/modifyDataPoint?" +
                "userID={userID}&dpid={dpid}&category={category}&purpose={purpose}";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        //userId defaulting to 1 for now until login system
        Integer response = restTemplate.getForObject(fullUrl, Integer.class,
                userId,dpId, category, purpose);
        return response;
    }

    @Override
    public DataPoint[] retrieveDataPoints(double maxLatitude, double minLatitude, double maxLongitude, double minLongitude) {
        final String fullUrl = "http://192.168.200.86:8080/mapping/water/retrieveDataPoints?" +
                "maxlatitude={maxlatitude}&minlatitude={minlatitude}&maxlongitude={maxlongitude}&minlongitude={minlongitude}";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
       DataPoint[] response = restTemplate.getForObject(fullUrl, DataPoint[].class,
                maxLatitude,minLatitude,maxLongitude,minLongitude);
        return response;
    }

    @Override
    public DataPoint[] recentUserDataPoints(int userId) {
        final String fullUrl = "http://192.168.200.86:8080/mapping/water/userDataPoints?userID={userID}";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        DataPoint[] response = restTemplate.getForObject(fullUrl, DataPoint[].class, userId);
        return response;

    }
}
