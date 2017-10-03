package com.controller;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author 404NotFound
 */


public class MainController implements Initializable{
    private Connection con;
    private PreparedStatement ps;
    private Statement st = null;
    private ResultSet rs;
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final String dbUrl = "jdbc:derby:hospitalDB";
    @FXML
    private Tab docTab;
    @FXML
    private Tab patientTab;
    @FXML
    private Tab userTab;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Menu userMenu;
    @FXML
    private TabPane tabPane;
    private int level = 1;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createConnection();
        if (level != 1) {
            tabPane.getTabs().remove(2);
        }
    }
    private void createConnection() {
        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(dbUrl);
        String checkAccount = "SELECT level FROM APP.Account Where isActive = true";
            st = con.createStatement();
            rs = st.executeQuery(checkAccount);
            while(rs.next()) {
                level = rs.getInt("level");
            }
            st.close();
            st = null;
        } catch (ClassNotFoundException | SQLException ex) {
        } catch (InstantiationException | IllegalAccessException ex) {
        }
    }
    
    @FXML
    private void handlechangePassword(){
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/com/view/Dialog/PasswordChangeDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Change Password");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            dialogStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
