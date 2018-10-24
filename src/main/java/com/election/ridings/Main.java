package com.election.ridings;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/Main.fxml"));
        Scene scene = new Scene(root);

        stage.setTitle("Election Ridings");
        stage.setScene(scene);
        //stage.setResizable(false);
        stage.show();
    }
}
