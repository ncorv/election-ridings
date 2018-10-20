package com.election.ridings;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
	// write your code here
        JSONHandler jh = new JSONHandler();
        try {
            System.out.println("Enter Zip Code: ");
            Scanner scanner = new Scanner(System. in); String input = scanner.nextLine();
            CsvOutput.WriteFile(input.toUpperCase());
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
