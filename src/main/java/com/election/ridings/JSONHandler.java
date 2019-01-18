package com.election.ridings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

public class JSONHandler {
    public JSONHandler() {
    }

    public static JsonArray possibleBoundaryCentroid(String zip) {
        try {
            URL api = new URL("https://represent.opennorth.ca/postcodes/" + zip + "/");
            HttpsURLConnection apiConnection = (HttpsURLConnection)api.openConnection();
            apiConnection.setRequestMethod("GET");
            apiConnection.setRequestProperty("Accept", "application/json");
            apiConnection.setRequestProperty("Content-Type", "application/json");
            apiConnection.connect();
            if (apiConnection.getResponseCode() != 200) {
                JsonArray jA = new JsonArray();
                jA.add(zip);
                return jA;
            } else {
                String assembledOutput = "";
                String output;
                for(BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(apiConnection.getInputStream())); (output = responseBuffer.readLine()) != null; assembledOutput = assembledOutput + output) {}
                apiConnection.disconnect();
                return (new JsonParser().parse(assembledOutput).getAsJsonObject().getAsJsonArray("boundaries_centroid"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonArray();
        }
    }

    public static JsonObject getJsonResponseFromZip(String zip) {
        try {
            URL api = new URL("https://represent.opennorth.ca/postcodes/" + zip + "/");
            HttpsURLConnection apiConnection = (HttpsURLConnection)api.openConnection();
            apiConnection.setRequestMethod("GET");
            apiConnection.setRequestProperty("Accept", "application/json");
            apiConnection.setRequestProperty("Content-Type", "application/json");
            apiConnection.connect();
            if (apiConnection.getResponseCode() != 200) {
                JsonObject jO = new JsonObject();
                return jO;
            } else {
                String assembledOutput = "";
                String output;
                for(BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(apiConnection.getInputStream())); (output = responseBuffer.readLine()) != null; assembledOutput = assembledOutput + output) {}
                apiConnection.disconnect();
                return (new JsonParser().parse(assembledOutput).getAsJsonObject());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonObject();
        }
    }
}