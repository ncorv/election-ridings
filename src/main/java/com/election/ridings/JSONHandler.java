package com.election.ridings;

import com.google.gson.*;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class JSONHandler {

    public static JsonObject GETZipcode(String zip) throws IOException{
         return new JsonParser().parse(JSONRequest(new URL("https://represent.opennorth.ca/postcodes/"+zip+"/"))).getAsJsonObject();
    }

    public static String JSONRequest(URL url) throws IOException{
        URL api = url;

        HttpsURLConnection apiConnection = (HttpsURLConnection) api.openConnection();

        apiConnection.setRequestMethod("GET");
        apiConnection.setRequestProperty("Accept", "application/json");
        apiConnection.setRequestProperty("Content-Type", "application/json");
       // apiConnection.setRequestProperty("authorization", "Bearer " + key);

        apiConnection.connect();

        if (apiConnection.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + apiConnection.getResponseCode());
        }

        String assembledOutput = "";

        BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                (apiConnection.getInputStream())));

        String output;
        while ((output = responseBuffer.readLine()) != null) {
            assembledOutput = assembledOutput + output;
        }

        apiConnection.disconnect();

        return assembledOutput;
    }
}
