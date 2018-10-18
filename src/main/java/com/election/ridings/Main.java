package com.election.ridings;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
	// write your code here
        JSONHandler jh = new JSONHandler();
        try {
            System.out.println(jh.GETZipcode("H0H0H0"));
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
