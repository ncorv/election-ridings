package com.election.ridings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;

public class CsvOutput {
    private static PrintWriter writer;

    public static String GetLine(String zip){
        String[] array = new String[128];
        try {
            JsonArray boundariesCentroid  = JSONHandler.GETZipcode(zip).getAsJsonArray("boundaries_centroid");
            int i = 0;
            for(JsonElement boundary : boundariesCentroid) {
                JsonObject boundaryObject = boundary.getAsJsonObject();
                String external_id = boundaryObject.get("external_id").getAsString();
                String boundary_set_name = boundaryObject.get("boundary_set_name").getAsString();
                String url = boundaryObject.get("url").getAsString();
                String name = boundaryObject.get("name").getAsString();
                String boundary_set_url = boundaryObject.get("related").getAsJsonObject().get("boundary_set_url").getAsString();

                if(boundary_set_name.contains("Federal")){
                   array[i]=(boundary_set_name + "," + name);
                }else if(boundary_set_name.contains("electoral")){
                    array[i]=(boundary_set_name + "," + name);
                }
                i++;
            }

            return array[0] +"\n"+ array[1] +"\n"+ array[2] +"\n"+ array[3] +"\n"+ array[4] +"\n"+ array[5];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void write(String zip) {
        try {
            JsonArray boundariesCentroid  = JSONHandler.GETZipcode(zip).getAsJsonArray("boundaries_centroid");


            writer = new PrintWriter("test.csv", "UTF-8");
            writer.println("nation_builder,zip,federal_district,provincial_district,name,url");
            for(JsonElement boundary : boundariesCentroid) {
                JsonObject boundaryObject = boundary.getAsJsonObject();
                String external_id = boundaryObject.get("external_id").getAsString();
                String boundary_set_name = boundaryObject.get("boundary_set_name").getAsString();
                String url = boundaryObject.get("url").getAsString();
                String name = boundaryObject.get("name").getAsString();
                String boundary_set_url = boundaryObject.get("related").getAsJsonObject().get("boundary_set_url").getAsString();

                if(boundary_set_name.contains("Federal")){
                   writer.println("nb_id_here" + "," + zip + "," + boundary_set_name + "," + "NO" + "," + name + "," + "https://represent.opennorth.ca"+url+"?format=apibrowser");
                }else if(boundary_set_name.contains("electoral")){
                    writer.println("nb_id_here" + "," + zip + ",NO," + boundary_set_name + "," + name + "," + "https://represent.opennorth.ca"+url+"?format=apibrowser");
                }
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
