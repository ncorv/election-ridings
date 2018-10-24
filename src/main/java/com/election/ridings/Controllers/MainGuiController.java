package com.election.ridings.Controllers;

import com.election.ridings.CsvOutput;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainGuiController  implements Initializable {
    @FXML //fx:id="openButton"
            Button openButton;
    @FXML //fx:id="csv"
            Button csv;
    @FXML //fx:id="pane";
            Pane pane;


    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        openButton.setOnAction(e -> {
            try {
                FileChooser fileChooser = new FileChooser();
                configureFileChooser(fileChooser);
                File file = fileChooser.showOpenDialog(pane.getScene().getWindow());
                if (file != null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText(file.getAbsolutePath());
                    alert.showAndWait();
                }
            } catch (Exception ex){

            }
        });

        csv.setOnAction(event -> {
            try{
                System.out.println("button clicked");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setWidth(1024);
                alert.setContentText(CsvOutput.GetLine("H0H0H0"));
                alert.showAndWait();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });
    }
    private static void configureFileChooser(final FileChooser fileChooser){
        fileChooser.setTitle("Open File");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }
}
