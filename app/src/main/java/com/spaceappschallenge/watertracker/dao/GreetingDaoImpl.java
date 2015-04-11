package com.spaceappschallenge.watertracker.dao;

import com.spaceappschallenge.watertracker.model.Greeting;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Andrew on 4/11/2015.
 */
public class GreetingDaoImpl implements GreetingDao{

    public Greeting getGreeting(String name) {
        final String url = "http://192.168.200.86:8080/greeting?name={name}";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        Greeting greeting = restTemplate.getForObject(url, Greeting.class,name);
        return greeting;
    }
}
