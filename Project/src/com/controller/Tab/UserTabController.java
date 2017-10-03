package com.controller.Tab;

import com.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.commons.codec.digest.DigestUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.Dialogs;

public class UserTabController implements Initializable {

    private Connection con;
    private PreparedStatement ps;
    private Statement st;
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final String dbUrl = "jdbc:derby:hospitalDB";
    private ResultSet rs;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn userNameColumn;
    @FXML
    private TableColumn emailColumn;
    @FXML
    private TableColumn levelColumn;
    @FXML
    private TableColumn docIdColumn;
    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<String> cbSearch;
    @FXML
    private TextField tfUsername;
    @FXML
    private PasswordField tfPassword;
    @FXML
    private TextField tfEmail;
    @FXML
    private TextField tfDoctorID;
    @FXML
    private RadioButton rdBt1;
    @FXML
    private ToggleGroup groupLevel;
    @FXML
    private RadioButton rdBt2;
    @FXML
    private Button btAdd;
    @FXML
    private Button btUpdate;
    
    private ObservableList<User> userData = FXCollections.observableArrayList();
    private int selectedLevel;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cbSearchInit();
        createConnection();
        initTable();
        rdBt1.setOnAction(e -> selectedLevel = 1);
        rdBt2.setOnAction(e -> selectedLevel = 2);
    }

    public void cbSearchInit() {
        cbSearch.getItems().addAll("Username", "Email", "Level");
        cbSearch.setValue("Username");
    }

    private void createConnection() {
        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(dbUrl);
            String sql = "SELECT * FROM APP.Account";
            st = con.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                User user = new User();
                user.setDoctorID(rs.getString("ID"));
                user.setUsername(rs.getString("Username"));
                user.setEmail(rs.getString("Email"));
                user.setPassworld(rs.getString("Password"));
                user.setLevel(rs.getInt("Level"));
                userData.add(user);
            }
            if (st != null) {
                st.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public void initTable() {
        userNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("Username")
        );
        emailColumn.setCellValueFactory(
                new PropertyValueFactory<>("Email")
        );
        levelColumn.setCellValueFactory(
                new PropertyValueFactory<>("Level")
        );
        docIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("DoctorID")
        );
        //Create fillter for search field
        FilteredList<User> filteredData = new FilteredList<>(userData, p -> true);
        userTable.setItems(filteredData);
        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                // If filter text is empty, display all 
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();

                switch (cbSearch.getValue()) {
                    case "Username":
                        if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        break;
                    case "Email":
                        if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        break;
                    case "Level":
                        if (isNumeric(newValue)) {
                            if (user.getLevel() == Integer.parseInt(newValue)) {
                                return true;
                            }
                        }
                        break;
                    default:
                        return false;
                }
                return false;
            });
        });
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showUserDetails(newValue));
    }

    public void showUserDetails(User user) {
        if (user != null) {
            tfUsername.setText(user.getUsername());
            tfEmail.setText(user.getEmail());
            tfPassword.setText(user.getPassworld());
            tfDoctorID.setText(user.getDoctorID());
            switch (user.getLevel()) {
                case 1:
                    rdBt1.setSelected(true);
                    break;
                case 2:
                    rdBt2.setSelected(true);
                    break;
            }
        } else {
            tfUsername.setText("");
            tfEmail.setText("");
            tfPassword.setText("");
            tfDoctorID.setText("");
        }
    }

    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @FXML
    public void handleBtAdd() {
        if (!isValidID(tfDoctorID.getText())) {
            Notifications.create().title("Add Error")
                    .text("ID not avaible")
                    .showError();
        }
        if (isDuplicateID(tfDoctorID.getText())) {
            Notifications.create().title("Add Error")
                    .text("ID already taken")
                    .showError();
        } else if (tfEmail.getText().equals("") || tfPassword.getText().equals("")
                || tfUsername.getText().equals("")) {
            Notifications.create().title("Add Error")
                    .text("All Text Field must be filled!")
                    .showError();
        } else if (rdBt1.isSelected() == false
                && rdBt2.isSelected() == false) {
            Notifications.create().title("Add Error")
                    .text("Please select a user level")
                    .showError();
        } else if (isDuplicateName(tfUsername.getText())) {
            Notifications.create().title("Add Error")
                    .text("Username already taken")
                    .showError();
        } else if (isDuplicateEmail(tfEmail.getText())) {
            Notifications.create().title("Add Error")
                    .text("Email already taken!")
                    .showError();
        } else {
            String id = tfDoctorID.getText();
            String uname = tfUsername.getText();
            String pass = DigestUtils.md5Hex(tfPassword.getText());
            String email = tfEmail.getText();
            if (rdBt1.isSelected()) {
                selectedLevel = 1;
            } else if (rdBt2.isSelected()) {
                selectedLevel = 2;
            } else {
                selectedLevel = 3;
            }
            User user = new User();
            user.setDoctorID(id);
            user.setUsername(uname);
            user.setPassworld(pass);
            user.setEmail(email);
            user.setLevel(selectedLevel);
            userData.add(user);
            String add = "INSERT INTO APP.Account (ID, Username, Password, Email, Level)";
            add += "VALUES (?, ?, ?, ?, ?)";

            try {
                ps = con.prepareStatement(add);
                ps.setString(1, id);
                ps.setString(2, uname);
                ps.setString(3, pass);
                ps.setString(4, email);
                ps.setInt(5, selectedLevel);
                ps.executeUpdate();
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Dialogs.create()
                        .owner(userTable)
                        .title("Exception")
                        .masthead("There is an exception")
                        .message("Ooops, there was an exception!")
                        .showException(ex);
            }
        }
    }

    @FXML
    public void handleBtUpdate() {
        if (tfEmail.getText().equals("") || tfPassword.getText().equals("")
                || tfUsername.getText().equals("")) {
            Notifications.create().title("Add Error")
                    .text("All Text Field must be filled!")
                    .showError();
        } else if (!isDuplicateName(tfUsername.getText())
                || !userTable.getSelectionModel().getSelectedItem().getUsername().equals(tfUsername.getText())) {
            Notifications.create().title("Add Error")
                    .text("Username doesn't exist!")
                    .showError();
        } else {
            String uname = tfUsername.getText();
            String pass = DigestUtils.md5Hex(tfPassword.getText());
            String email = tfEmail.getText();
            if (rdBt1.isSelected()) {
                selectedLevel = 1;
            } else if (rdBt2.isSelected()) {
                selectedLevel = 2;
            } else {
                selectedLevel = 3;
            }
            String update = "UPDATE APP.Account SET Password = ?, Email = ?, Level = ? WHERE username = ?";
            try {
                ps = con.prepareStatement(update);
                ps.setString(1, pass);
                ps.setString(2, email);
                ps.setInt(3, selectedLevel);
                ps.setString(4, uname);
                ps.executeUpdate();
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Dialogs.create()
                        .owner(userTable)
                        .title("Exception")
                        .masthead("There is an exception")
                        .message("Ooops, there was an exception!")
                        .showException(ex);
            }
            User user = userTable.getSelectionModel().getSelectedItem();
            user.setEmail(email);
            user.setLevel(selectedLevel);
            user.setPassworld(pass);
            forceRefresh();
            Notifications.create().title("Update")
                    .text("Update Successfully")
                    .showInformation();
        }
    }

    @FXML
    public void handleBtDelete() {
        int selectedIndex = userTable.getSelectionModel().getSelectedIndex();
        String selectedUsername = userData.get(selectedIndex).getUsername();
        String detele = "DELETE FROM APP.Account WHERE Username = ?";
        try {
            ps = con.prepareStatement(detele);
            ps.setString(1, selectedUsername);
            ps.executeUpdate();
            ps.close();
            ps = null;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        userData.remove(selectedIndex);
    }

    public boolean isDuplicateName(String s) {
        return userData.stream().anyMatch((user) -> (s.equals(user.getUsername())));
    }

    public boolean isDuplicateEmail(String s) {
        return userData.stream().anyMatch((user) -> (s.equals(user.getEmail())));
    }

    public boolean isDuplicateID(String id) {
        return userData.stream().anyMatch((user) -> (id.equals(user.getDoctorID())));
    }

    public boolean isValidID(String id) {
                        
        List<String> idList = new ArrayList<>();
        String sql = "SELECT ID FROM APP.Doctor";
        try {
            st = con.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                idList.add(rs.getString(1));
            }
            if (st != null) {
                st.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return idList.stream().anyMatch((doctorID) -> (doctorID.equals(id)));
    }

    public User getUser(String name) {
        for (User user : userData) {
            if (user.getUsername().equals(name)) {
                return user;
            }
        }
        return null;
    }

    public void forceRefresh() {
        new java.util.Timer().schedule(new java.util.TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    userTable.getColumns().get(0).setVisible(false);
                    userTable.getColumns().get(0).setVisible(true);
                });
            }
        }, 50);
    }

}
