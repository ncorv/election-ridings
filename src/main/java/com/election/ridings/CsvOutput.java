package com.election.ridings;

import com.google.gson.*;
import com.opencsv.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class CsvOutput {
    private static PrintWriter writer;
    private static List<List<String[]>> lineList = new ArrayList();

    public CsvOutput() {
    }

    public static void ReadInputCSV() throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader("Postal_codes.csv"));
        CSVReader csvReader = new CSVReader(reader);
        List<String[]> list = csvReader.readAll();
        reader.close();
        csvReader.close();
        lineList.add(list);
    }

    public static void GenerateOutput() throws Exception {
        writer = new PrintWriter("test.csv", "UTF-8");
        writer.println("Postal Code,Federal Riding,Provincial Riding");
        PrintWriter badwriter = new PrintWriter("badzip.csv", "UTF-8");
        badwriter.println("bad_zip");
        PrintWriter goodwriter = new PrintWriter("goodzip.csv", "UTF-8");
        goodwriter.println("good_zip");
        int index = 0;
        for(int i = 0; i < lineList.get(0).toArray().length; i++) {
            StringBuilder currentZip = new StringBuilder();

            // preliminary replacements, remove blank characters, then spaces, then dashes, then uppercase blank spaces????
            currentZip.append(StringUtils.join((Object[])((List)lineList.get(0)).get(i), " ")
                    .replace(Character.toString('\uFEFF'),"")
                    .replace(" ", "")
                    .replace("-", "")
                    .replace(Character.toString('\uFEFF'), "").toUpperCase());

            //if the length of the current zipcode we are looking at is 6, try preliminary substitutions
            if (currentZip.length() == 6) {
                for(int j = 1; j < currentZip.length(); j += 2) { //if any of the even characters are letters, try to make a number
                    if (currentZip.charAt(j) == 'O') {
                        currentZip.setCharAt(j, '0');
                    } else if (currentZip.charAt(j) == 'I' || currentZip.charAt(j) == 'L') {
                        currentZip.setCharAt(j,'1');
                    } else if (currentZip.charAt(j) == 'S') {
                        currentZip.setCharAt(j, '5');
                    } else if (currentZip.charAt(j) == 'B') {
                        currentZip.setCharAt(j, '8');
                    }
                }
            }

            // at this stage line represents a potential valid zipcode, with all easy substitutions having been made
            JsonObject jsonResponse = JSONHandler.getJsonResponseFromZip(currentZip.toString());
            JsonArray boundariesCentroid = jsonResponse.getAsJsonArray("boundaries_centroid");
            if (currentZip.length() != 6 || jsonResponse.entrySet().isEmpty()) {
                badwriter.println(currentZip.toString());
                badwriter.flush();
                System.out.println(currentZip.toString() + " : Bad Zip");
            } else if(boundariesCentroid.size() == 0) { // sometimes json response will not be zero but we still wont have a boundaries_centroid
                //need second bad case dependant on first failing
                badwriter.println(currentZip.toString());
                badwriter.flush();
                System.out.println(currentZip.toString() + " : Bad Zip");
            }else{
                index++;
                goodwriter.println(currentZip.toString());
                goodwriter.flush();
                System.out.println(currentZip.toString() + " : Good Zip #" + index);
                //if good print line for zip, federaldistrict, provincial district
                writer.println(currentZip.toString() + "," + getFederalDistrict(boundariesCentroid) + "," + getProvincialDistrict(jsonResponse));
                writer.flush();
                }
        }
        writer.close();
        badwriter.close();
        goodwriter.close();
    }

    private static String getProvincialDistrict(JsonObject jsonResponse) {
    String currentProvincialDistrict = "";
        JsonArray currentBoundariesCentroid = jsonResponse.getAsJsonArray("boundaries_centroid");
        JsonArray currentBoundariesConcordance = jsonResponse.getAsJsonArray("boundaries_concordance");
        Iterator boundariesCentroidIterator = currentBoundariesCentroid.iterator();
        JsonElement currentBoundary;
        //find current data
        while (boundariesCentroidIterator.hasNext()) {
            // can we get a current result
            currentBoundary = (JsonElement) boundariesCentroidIterator.next();
            if (!currentBoundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("federal")
                    && currentBoundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("electoral")
                    && !currentBoundary.getAsJsonObject().get("url").toString().matches(".*-\\d{4}-.*")
                    && !currentBoundary.getAsJsonObject().get("url").toString().matches(".*-\\d{4}/.*")) {
                currentProvincialDistrict = StringUtils.stripAccents(currentBoundary.getAsJsonObject().get("name").getAsString().replace('—', '-').replace('–', '-'));
            }
        }
        //is there a boundaries concordance? and will it give us a name if it exists
        if(currentBoundariesConcordance.size() > 0){
            Pattern pattern = Pattern.compile(".*-(\\d{4})/.*");
            int maxYear = Integer.MIN_VALUE;
            String maxUrl = "";
            Iterator boundariesConcordanceIterator = currentBoundariesConcordance.iterator();
            while (boundariesConcordanceIterator.hasNext()) {
                currentBoundary = (JsonElement) boundariesConcordanceIterator.next();
                Matcher matcher = pattern.matcher(currentBoundary.getAsJsonObject().get("url").toString());
                if (matcher.matches()
                        && !currentBoundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("federal")
                        && currentBoundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("electoral")) {
                    int year = Integer.parseInt(matcher.group(1));
                    if (year > maxYear) {
                        maxUrl = currentBoundary.getAsJsonObject().get("url").toString();
                        maxYear = year;
                    }
                }
            }
            boundariesConcordanceIterator = currentBoundariesConcordance.iterator();
            while (boundariesConcordanceIterator.hasNext()) {
                currentBoundary = (JsonElement) boundariesConcordanceIterator.next();
                if (currentBoundary.getAsJsonObject().get("url").toString().contains(maxUrl)){
                    currentProvincialDistrict = StringUtils.stripAccents(currentBoundary.getAsJsonObject().get("name").getAsString().replace('—', '-').replace('–', '-'));
                }
            }
        } else if(currentProvincialDistrict == ""){// no boundaries concordance means we must go to highest year
            //get highest year, then we iterate again to find it & extract
            Pattern pattern = Pattern.compile(".*-(\\d{4})-.*");
            Pattern pattern2 = Pattern.compile(".*-(\\d{4})/.*");
            int maxYear = Integer.MIN_VALUE;
            String maxUrl = "";
            if(currentProvincialDistrict == "") { // if its still blank we have to iterate again and find the highest year
                boundariesCentroidIterator = currentBoundariesCentroid.iterator();
                while (boundariesCentroidIterator.hasNext()) {
                    currentBoundary = (JsonElement) boundariesCentroidIterator.next();
                    Matcher matcher = pattern.matcher(currentBoundary.getAsJsonObject().get("url").toString());
                    Matcher matcher2 = pattern2.matcher(currentBoundary.getAsJsonObject().get("url").toString());
                    if (matcher.matches()
                            && !currentBoundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("federal")
                            && currentBoundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("electoral")) {
                        int year = Integer.parseInt(matcher.group(1));
                        if (year > maxYear) {
                            maxUrl = currentBoundary.getAsJsonObject().get("url").toString();
                            maxYear = year;
                        }
                    } else if (matcher2.matches()
                            && !currentBoundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("federal")
                            && currentBoundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("electoral")) {
                        int year = Integer.parseInt(matcher2.group(1));
                        if (year > maxYear) {
                            maxUrl = currentBoundary.getAsJsonObject().get("url").toString();
                            maxYear = year;
                        }
                    }
                }
                boundariesCentroidIterator = currentBoundariesCentroid.iterator();
                while (boundariesCentroidIterator.hasNext()) {
                    currentBoundary = (JsonElement) boundariesCentroidIterator.next();
                    if (currentBoundary.getAsJsonObject().get("url").toString().contains(maxUrl) && maxUrl.length() != 0){
                        currentProvincialDistrict = StringUtils.stripAccents(currentBoundary.getAsJsonObject().get("name").getAsString().replace('—', '-').replace('–', '-'));
                    }
                }
            }
        }
    return currentProvincialDistrict;
    }

    private static String getFederalDistrict(JsonArray currentBoundariesCentroid){
        String currentFederalDistrict = "";
        Iterator boundariesCentroidIterator = currentBoundariesCentroid.iterator();
        JsonElement currentBoundary;
        while (boundariesCentroidIterator.hasNext()) {
            currentBoundary = (JsonElement) boundariesCentroidIterator.next();
            if (currentBoundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("federal electoral district")
                    && currentBoundary.getAsJsonObject().get("url").toString().contains("/boundaries/federal-electoral-districts/")) {
                currentFederalDistrict = StringUtils.stripAccents(currentBoundary.getAsJsonObject().get("name").getAsString().replace('—', '-').replace('–', '-'));
            }
        }
        return currentFederalDistrict;
    }
}


/*
      JsonArray boundariesCentroid = JSONHandler.possibleBoundaryCentroid(line.toString());
            String currentFederalDistrict = "";

            if (line.length() != 6 || boundariesCentroid.size() <= 1) {
                badwriter.println(line.toString());
                badwriter.flush();
                System.out.println(line.toString() + " : Bad Zip");
            } else {
                index++;
                goodwriter.println(line.toString());
                goodwriter.flush();
                System.out.println(line.toString() + " : Good Zip #"+index);
                Iterator boundariesCentroidIterator = boundariesCentroid.iterator();

                JsonElement boundary;
                Pattern pattern = Pattern.compile(".*-(\\d{4})/.*");
                int maxYear = Integer.MIN_VALUE;
                String maxUrl = "";
                String name = "";
                Boolean hasCurrent = false;
                //for each element in zip code json response
                while (boundariesCentroidIterator.hasNext()) {
                    boundary = (JsonElement) boundariesCentroidIterator.next();
                    if (boundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("federal")) {
                        currentFederalDistrict = StringUtils.stripAccents(boundary.getAsJsonObject().get("name").getAsString().replace('—', '-').replace('–', '-'));;
                    }
                    if (!boundary.getAsJsonObject().get("url").toString().contains("federal")
                            && boundary.getAsJsonObject().get("url").toString().contains("electoral")
                            && !boundary.getAsJsonObject().get("url").toString().contains(".*\\d{4}-.*")
                            && !boundary.getAsJsonObject().get("url").toString().contains(".*\\d{4}/.*")) {
                        hasCurrent = true;
                        name = StringUtils.stripAccents(boundary.getAsJsonObject().get("name").getAsString().replace('—', '-').replace('–', '-'));
                        //System.out.println(name);
                    }
                    if (!boundary.getAsJsonObject().get("url").toString().toLowerCase().contains("census")
                        && !boundary.getAsJsonObject().get("url").toString().matches(".*-(\\d{4})/.*")
                        && !boundary.getAsJsonObject().get("url").toString().matches(".*-(\\d{4})-.*")
                        && !boundary.getAsJsonObject().get("url").toString().toLowerCase().contains("ward")
                        && !boundary.getAsJsonObject().get("name").toString().toLowerCase().contains("lot")
                        && !boundary.getAsJsonObject().get("name").toString().toLowerCase().contains("district")){

                        if (!boundary.getAsJsonObject().get("name").toString().toLowerCase().contains("federal")
                            && !boundary.getAsJsonObject().get("boundary_set_name").toString().toLowerCase().contains("federal")){
                            name = StringUtils.stripAccents(boundary.getAsJsonObject().get("name").getAsString().replace('—', '-').replace('–', '-'));
                           // System.out.println(name);
                        }
                        // if the specific boundary we are looking at does not contain census and does not contain federal
                    } else if (hasCurrent == false
                            && boundary.getAsJsonObject().get("url").toString().toLowerCase().contains("electoral")
                            && !boundary.getAsJsonObject().get("url").toString().toLowerCase().contains("federal")
                            && !boundary.getAsJsonObject().get("url").toString().toLowerCase().contains("census")) {
                        Matcher matcher = pattern.matcher(boundary.getAsJsonObject().get("url").toString());
                        if (matcher.matches()) {
                            int year = Integer.parseInt(matcher.group(1));
                            if (year > maxYear) {
                                maxUrl = boundary.getAsJsonObject().get("url").toString();
                                maxYear = year;
                            }
                        }
                    }
                }
                // if we iterate over the entire array and hasCurrent is still false, it means we only have historical data for electoral district name
                if (hasCurrent == false){
                    boundariesCentroidIterator = boundariesCentroid.iterator();
                    while (boundariesCentroidIterator.hasNext()) {
                        boundary = (JsonElement) boundariesCentroidIterator.next();
                        if (boundary.getAsJsonObject().get("url").toString().toLowerCase().contains(maxUrl)
                                && !boundary.getAsJsonObject().get("url").toString().toLowerCase().contains("census")
                                && !boundary.getAsJsonObject().get("url").toString().toLowerCase().contains("ward")
                                && !boundary.getAsJsonObject().get("name").toString().toLowerCase().contains("lot")){
                            name = StringUtils.stripAccents(boundary.getAsJsonObject().get("name").getAsString().replace('—', '-').replace('–', '-'));
                           // System.out.println(name);
                        }
                    }
                }
                writer.println(line + "," + currentFederalDistrict + "," + name);
                writer.flush();
                //System.out.println(maxYear + " " + maxUrl + " " + name);
            }
 */




//here lies my hopes and dreams

//writer.println(line + "," + currentFederalDistrict + "," + name);
// writer.flush();
                /*
                  public String getWithHighestYear(Iterable<String> urls) {
                  Pattern pattern = Pattern.compile(".*-(\\d{4})/.*");
                  int maxYear = Integer.MIN_VALUE;
                  String maxUrl = "";
                   for (String url: urls) {
                        Matcher matcher = pattern.matcher(url);
                        if (matcher.matches()) {
                            int year = Integer.parseInt(matcher.group(1));
                            if (year > maxYear) {
                                maxUrl = url;
                                maxYear = year;
                            }
                    }
                    else {
                        // no year, must be max
                         return url;
                     }
                    }

                 return maxUrl;
                 }
                 */

                /*
                    Pattern pattern = Pattern.compile(".*(\\d{4}).*");
                    Stream.of("/boundaries/ontario-electoral-districts-representation-act-2015/timiskaming-cochrane/", "/boundaries/ontario-electoral-districts-representation-act-2005/timiskaming-cochrane")
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .sorted(Comparator.comparing(m -> m.group(1)))
                    .map(m -> m.group(0))
                    .forEachOrdered(System.out::println);
                 */

                /*
                List<String> result = yourJson.stream()
                    .filter(item -> line.contains(electoral)
                    .filter(containElectorals -> !containsElectorals.contains("federal") && containElectorals.contains("year"))
                    .collect(Collectors.toList());

                result.forEach(System.out::println);

                List<string> result = yourJson.stream()
                 .filter(jsonLine -> jsonLine.contains("year"))
                .sorted(o1, o2) -> o1("year"  whatever finds the year in the object ).compareTo(o2("year" same)
                        .collect(Collectors.toList());

                string yourLastJsonWithHighestYear = result.get(result.size()-1);

                 sort my json url responses according to a custom comparable method and pick the highest weighted result
                 */
                /*
                JsonElement BoundaryCurrent;
                JsonElement Boundary2015;
                JsonElement Boundary2005;
                JsonElement Boundary2003;

                while(boundariesCentroidIterator.hasNext()) {
                    boundary = (JsonElement)boundariesCentroidIterator.next();
                    if (!boundary.getAsJsonObject().get("url").toString().matches(".*-\\d{4}.*") && boundary.getAsJsonObject().get("url").toString().contains("electoral")) {
                        boundaryObject = boundary.getAsJsonObject();
                        boundary_set_name = StringUtils.stripAccents(boundaryObject.get("boundary_set_name").getAsString().replace('—', '-').replace('–', '-'));
                        name = StringUtils.stripAccents(boundaryObject.get("name").getAsString().replace('—', '-').replace('–', '-'));
                        if (boundary_set_name.contains("electoral") && !boundary_set_name.contains("Federal")) {
                            writer.println(line + "," + currentFederalDistrict + "," + name);
                            writer.flush();
                        }
                       BoundaryCurrent = boundary;
                    } else if (boundary.getAsJsonObject().get("url").toString().matches(".*-\\d{4}.*")){
                        if(boundary.getAsJsonObject().get("url").toString().contains("2015") && boundary.getAsJsonObject().get("url").toString().contains("electoral") && !boundary.getAsJsonObject().get("url").toString().contains("federal")){
                            Boundary2015 = boundary;
                        } else if(boundary.getAsJsonObject().get("url").toString().contains("2005") && boundary.getAsJsonObject().get("url").toString().contains("electoral") && !boundary.getAsJsonObject().get("url").toString().contains("federal")){
                            Boundary2005 = boundary;
                        } else if(boundary.getAsJsonObject().get("url").toString().contains("2003") && boundary.getAsJsonObject().get("url").toString().contains("electoral") && !boundary.getAsJsonObject().get("url").toString().contains("federal")){
                            Boundary2003 = boundary;
                        }
                    }
                }*/