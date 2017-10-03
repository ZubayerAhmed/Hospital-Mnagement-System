package com.controller.Tab;

import com.model.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.CustomTextField;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.Dialogs;

public class PatientTabController implements Initializable {

    private Connection con;
    private PreparedStatement ps;
    private Statement st;
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final String dbUrl = "jdbc:derby:hospitalDB";
    private ResultSet rs;
    @FXML
    private TableView patientTable;
    @FXML
    private TableColumn patientIdColumn;
    @FXML
    private TableColumn patientNameColumn;
    @FXML
    private TableColumn detailsColumn;
    @FXML
    private TableColumn doctorColumn;
    @FXML
    private TextField tfAddDoctorID;
    @FXML
    private CustomTextField tfPatientID;
    @FXML
    private Button btAdd;
    @FXML
    private CustomTextField tfPatientName;
    @FXML
    private TextArea taDetails;
    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<String> cbSearch;
    @FXML
    private ToggleGroup viewMode;
    @FXML
    private RadioButton btViewAll;
    @FXML
    private RadioButton btViewMine;
    private int level = 3;
    private String isActiveUser;
    private ObservableList<Patient> patientData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
        cbSearchInit();
        createConnection();
        reloadTable();
        btViewAll.selectedProperty().addListener(ov -> {
            if (btViewAll.isSelected()) {
                patientTable.setEditable(false);
                reloadTable();
            }
        });
        btViewMine.selectedProperty().addListener(ov -> {
            if (btViewMine.isSelected()) {
                viewMineMode();
                setEditable();
            }
        });
    }

    @FXML
    public void handleAddButton() {
        if (tfPatientID.getText().equals("") || tfPatientName.getText().equals("") || taDetails.getText().equals("")) {
            Notifications.create().title("Add Error")
                    .text("All Text Field must be filled!")
                    .showError();
        } else if (isDuplicate(tfPatientID.getText())) {
            Notifications.create().title("Add Error")
                    .text("Duplicate PatientID")
                    .showError();
        } else {
            String id = tfPatientID.getText();
            String name = tfPatientName.getText();
            String details = taDetails.getText();
            String doctorId = tfAddDoctorID.getText();
            String doctorName = "";
            try {
                String checkAccount = "SELECT Name FROM APP.Doctor Where ID = ?";
                ps = con.prepareStatement(checkAccount);
                ps.setString(1, id);
                rs = ps.executeQuery();
                while (rs.next()) {
                    doctorName = rs.getString(1);
                }
                if (st != null) {
                    st.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            Patient patient = new Patient();
            patient.setIdProperty(id);
            patient.setNameProperty(name);
            patient.setDetailsProperty(details);
            patient.setDoctorNameProperty(doctorName);
            patientData.add(patient);
            String add = "INSERT INTO APP.Patient (ID, Name, Details, DoctorID)"
                    + "VALUES (?, ?, ?, ?)";
            try {
                ps = con.prepareStatement(add);
                ps.setString(1, id);
                ps.setString(2, name);
                ps.setString(3, details);
                ps.setString(4, doctorId);
                ps.executeUpdate();
                ps.close();
                ps = null;
            } catch (SQLException ex) {
                Dialogs.create()
                        .owner(patientTable)
                        .title("Exception")
                        .masthead("There is an exception")
                        .message("Ooops, there was an exception!")
                        .showException(ex);
            }
            reloadTable();
            tfPatientID.clear();
            tfPatientName.clear();
            taDetails.clear();
        }
    }

    @FXML
    public void handleBtDelete() {
        int selectedIndex = patientTable.getSelectionModel().getSelectedIndex();
        String selectedID = patientData.get(selectedIndex).getIdProperty();
        String detele = "DELETE FROM APP.Patient WHERE ID = ?";
        try {
            ps = con.prepareStatement(detele);
            ps.setString(1, selectedID);
            ps.executeUpdate();
            ps.close();
            ps = null;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        patientData.remove(selectedIndex);
    }

    private void initTable() {
        patientIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("IdProperty")
        );
        patientNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("nameProperty")
        );
        detailsColumn.setCellValueFactory(
                new PropertyValueFactory<>("detailsProperty")
        );
        doctorColumn.setCellValueFactory(
                new PropertyValueFactory<>("doctorNameProperty")
        );
        if (level != 3) {
            setEditable();
        }
        //Create fillter for search field
        FilteredList<Patient> filteredData = new FilteredList<>(patientData, p -> true);
        patientTable.setItems(filteredData);

        tfSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(patient -> {
                // If filter text is empty, display all p
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                // Compare first name and last name of every person with filter text.
                String lowerCaseFilter = newValue.toLowerCase();
                switch (cbSearch.getValue()) {
                    case "ID":
                        if (patient.getIdProperty().toLowerCase().contains(lowerCaseFilter)) {
                            return true; 
                        }
                        break;
                    case "Name":
                        if (patient.getNameProperty().toLowerCase().contains(lowerCaseFilter)) {
                            return true; 
                        }
                        break;
                    case "Details":
                        if (patient.getDetailsProperty().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        break;
                    case "Doctor":
                        if (patient.getDoctorNameProperty().toLowerCase().contains(lowerCaseFilter)) {
                            return true;
                        }
                        break;
                }
                return false;
            });
        });
    }
    
    private void cbSearchInit() {
        cbSearch.getItems().addAll("ID", "Name", "Department", "Doctor");
        cbSearch.setValue("ID");
    }

    private void createConnection() {
        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(dbUrl);
        } catch (ClassNotFoundException | SQLException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
        try {
            String checkAccount = "SELECT id, level FROM APP.Account Where isActive = true";
            st = con.createStatement();
            rs = st.executeQuery(checkAccount);
            while (rs.next()) {
                level = rs.getInt("level");
                isActiveUser = rs.getString("id");
            }
            if (st != null) {
                st.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setEditable() {
        patientTable.setEditable(true);
        patientIdColumn.setEditable(true);
        patientIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        patientIdColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Patient, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Patient, String> t) {
                Patient patient = ((Patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                if (t.getNewValue().length() == 0) {
                    forceRefresh(); // Regain CONTROL for an UNDO
                    Notifications.create().title("Edit Error")
                            .text("Field cannot be empty!")
                            .showError();
                } else if (isDuplicate(t.getNewValue())) {
                    forceRefresh();
                    Notifications.create().title("Add Error")
                            .text("Duplicate PatientID")
                            .showError();
                } else {
                    String update = "UPDATE APP.Patient SET ID = ? WHERE name = ?";
                    try {
                        ps = con.prepareStatement(update);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, patient.getNameProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;
                    } catch (SQLException ex) {
                        Dialogs.create()
                                .owner(patientTable)
                                .title("Exception")
                                .masthead("There is an exception")
                                .message("Ooops, there was an exception!")
                                .showException(ex);
                    }
                    patient.setIdProperty(t.getNewValue());
                }

            }
        });
        patientNameColumn.setEditable(true);
        patientNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        patientNameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Patient, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Patient, String> t) {
                Patient patient = ((Patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                if (t.getNewValue().length() == 0) {
                    forceRefresh(); // Regain CONTROL for an UNDO
                    Notifications.create().title("Edit Error")
                            .text("Field cannot be empty!")
                            .showError();
                } else {
                    String update = "UPDATE APP.Patient SET Name = ? WHERE ID = ?";
                    try {
                        ps = con.prepareStatement(update);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, patient.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;
                    } catch (SQLException ex) {
                        Dialogs.create()
                                .owner(patientTable)
                                .title("Exception")
                                .masthead("There is an exception")
                                .message("Ooops, there was an exception!")
                                .showException(ex);
                    }
                    patient.setNameProperty(t.getNewValue());
                }
            }
        });
        detailsColumn.setEditable(true);
        detailsColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        detailsColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Patient, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Patient, String> t) {
                Patient patient = ((Patient) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                if (t.getNewValue().length() == 0) {
                    forceRefresh(); // Regain CONTROL for an UNDO
                    Notifications.create().title("Edit Error")
                            .text("Field cannot be empty!")
                            .showError();
                } else {
                    String update = "UPDATE APP.Patient SET Details = ? WHERE ID = ?";
                    try {
                        ps = con.prepareStatement(update);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, patient.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;
                    } catch (SQLException ex) {
                        Dialogs.create()
                                .owner(patientTable)
                                .title("Exception")
                                .masthead("There is an exception")
                                .message("Ooops, there was an exception!")
                                .showException(ex);
                    }
                    patient.setDetailsProperty(t.getNewValue());
                }
            }
        });
    }

    public void forceRefresh() {
        new java.util.Timer().schedule(new java.util.TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    TableView<Patient> patientTab;
                    patientTab = patientTable;
                    patientTab.getColumns().get(0).setVisible(false);
                    patientTab.getColumns().get(0).setVisible(true);
                });
            }
        }, 50);
    }

    public boolean isDuplicate(String s) {
        for (Patient patient : patientData) {
            if (s.equals(patient.getIdProperty())) {
                return true;
            }
        }
        return false;
    }
    
    private void reloadTable(){
        try {
            patientData.clear();
            String sql = "SELECT Patient.ID, Patient.Name, Patient.Details, Doctor.Name ";
            sql += "FROM APP.Patient INNER JOIN App.Doctor ";
            sql += "On Patient.DoctorId = Doctor.ID";
            st = con.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                Patient p = new Patient();
                p.setIdProperty(rs.getString(1));
                p.setNameProperty(rs.getString(2));
                p.setDetailsProperty(rs.getString(3));
                p.setDoctorNameProperty(rs.getString(4));
                patientData.add(p);
            }
            if (st != null) {
                st.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(PatientTabController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public void viewMineMode(){
        try {
            patientData.clear();
            String sql = "SELECT Patient.ID, Patient.Name, Patient.Details, Doctor.Name ";
            sql += "FROM APP.Patient, App.Doctor ";
            sql += "where Patient.DoctorId = Doctor.ID And Patient.DoctorId = ?";
            ps = con.prepareStatement(sql);
            ps.setString(1, isActiveUser);
            rs = ps.executeQuery();
            while (rs.next()) {
                Patient p = new Patient();
                p.setIdProperty(rs.getString(1));
                p.setNameProperty(rs.getString(2));
                p.setDetailsProperty(rs.getString(3));
                p.setDoctorNameProperty(rs.getString(4));
                patientData.add(p);
            }
            if (ps != null) {
                st.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
