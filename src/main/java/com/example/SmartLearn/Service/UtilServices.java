package com.example.SmartLearn.Service;


import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class UtilServices {
    public boolean checkUrl(String strUrl) {
        try{
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("Head");
            connection.setConnectTimeout(5000); // Timeout after 5 seconds
            connection.connect();
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        }catch (IOException e){
            return false;
        }
    }
}
