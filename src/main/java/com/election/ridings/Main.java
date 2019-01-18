package com.election.ridings;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        try {
            CsvOutput.ReadInputCSV();
            CsvOutput.GenerateOutput();
            System.out.println("Done");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
