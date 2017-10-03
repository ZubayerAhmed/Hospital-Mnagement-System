package com.controller;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;
import org.controlsfx.control.Notifications;

public class LoginController implements Initializable {

    @FXML
    private GridPane gridPane;
    @FXML
    private PasswordField tfPassword;
    @FXML
    private Button btLogin;
    @FXML
    private TextField tfUser;
    private boolean checkAccount = false;
    private Connection con;
    private PreparedStatement ps;
    private Statement st;
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final String dbUrl = "jdbc:derby:hospitalDB";
    private ResultSet rs;

    @FXML
    private void btLoginAction(ActionEvent e) throws IOException {
        try {
            String sql = "SELECT * FROM APP.Account WHERE username = ? AND password = ?";
            String pass = DigestUtils.md5Hex(tfPassword.getText());
            ps = con.prepareStatement(sql);
            ps.setString(1, tfUser.getText());
            ps.setString(2, pass);
            rs = ps.executeQuery();
            if (rs.next()) {
                checkAccount = true;
                ps.close();
                ps = null;
            } else {
                Notifications.create().title("Login Error")
                        .text("Check your username or password!")
                        .showError();
            }
        } catch (SQLException ex) {
            Notifications.create().title("Login Error")
                .showError();
        }

        if (checkAccount) {
            String update = "Update APP.Account Set isActive = true Where username = ?";
            try {
                ps = con.prepareStatement(update);
                ps.setString(1, tfUser.getText());
                ps.executeUpdate();
                ((Node) (e.getSource())).getScene().getWindow().hide();
                Parent parent = FXMLLoader.load(getClass().getResource("/com/view/Main.fxml"));
                Stage stage = new Stage();
                Scene scene = new Scene(parent);
                stage.setScene(scene);
                stage.setTitle("Hospital Management");
                stage.getIcons().add(new Image("http://icons.iconarchive.com/icons/medicalwp/medical/24/Hospital-red-icon.png"));
                stage.setResizable(false);
                stage.show();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /*@FXML*/
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createConnection();
        //Create new Runnable to request focus on Username
        Platform.runLater(() -> {
            tfUser.requestFocus();
        });

    }

    private void createConnection() {
        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(dbUrl);
            String reset = "Update APP.Account Set isActive = false";
            st = con.createStatement();
            st.executeUpdate(reset);
        } catch (ClassNotFoundException | SQLException ex) {
        } catch (InstantiationException | IllegalAccessException ex) {
        }
    }
}
