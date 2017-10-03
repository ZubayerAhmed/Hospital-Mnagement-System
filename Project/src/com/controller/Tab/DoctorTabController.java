package com.controller.Tab;

import com.model.Doctor;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.controlsfx.control.Notifications;
import org.controlsfx.dialog.Dialogs;

public class DoctorTabController implements Initializable {

    private Connection con;
    private PreparedStatement ps = null;
    private Statement st = null;
    private final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private final String dbUrl = "jdbc:derby:hospitalDB";
    private ResultSet rs;
    @FXML
    private Button btSave;
    @FXML
    private TextField tfDoctorSearch;
    @FXML
    private Button btAddDoctor;
    @FXML
    private ComboBox<String> cbSearch;
    @FXML
    private TableView<Doctor> doctorTable;
    @FXML
    private TableColumn doctorIdColumn;
    @FXML
    private TableColumn doctorNameColumn;
    @FXML
    private TableColumn departmentColumn;
    @FXML
    private TextField tfAddDoctorID;
    @FXML
    private TextField tfAddDoctorName;
    @FXML
    private TextField tfAddDepartment;
    private ObservableList<Doctor> doctorData = FXCollections.observableArrayList();
    private ObservableList<Doctor> filteredData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
        setEditable();
        cbSearchInit();
        createConnection();
    }

    @FXML
    public void handleBtAddDoctor() {
        if (tfAddDoctorID.getText().equals("") || tfAddDoctorName.getText().equals("") || tfAddDepartment.getText().equals("")) {
            Notifications.create().title("Add Error")
                    .text("All Text Field must be filled!")
                    .showError();
        } else if (isDuplicate(tfAddDoctorID.getText())) {
            Notifications.create().title("Add Error")
                    .text("Duplicate DoctorID")
                    .showError();
        } else {
            String id = tfAddDoctorID.getText();
            String name = tfAddDoctorName.getText();
            String department = tfAddDepartment.getText();
            Doctor doctor = new Doctor();
            doctor.setIdProperty(id);
            doctor.setNameProperty(name);
            doctor.setDepartmentProperty(department);
            doctorData.add(doctor);
            String add = "INSERT INTO APP.DOCTOR (ID, Name, Department)"
                    + "VALUES (?, ?, ?)";
            try {
                ps = con.prepareStatement(add);
                ps.setString(1, id);
                ps.setString(2, name);
                ps.setString(3, department);
                ps.executeUpdate();
                ps.close();
                ps = null;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            tfAddDoctorID.clear();
            tfAddDoctorName.clear();
            tfAddDepartment.clear();
        }
    }

    @FXML
    public void handleBtDelete() {
        int selectedIndex = doctorTable.getSelectionModel().getSelectedIndex();
        String selectedID = doctorData.get(selectedIndex).getIdProperty();
        String detele = "DELETE FROM APP.DOCTOR WHERE ID = ?";
        try {
            ps = con.prepareStatement(detele);
            ps.setString(1, selectedID);
            ps.executeUpdate();
            ps.close();
            ps = null;
        } catch (SQLException ex) {
            Dialogs.create()
                    .owner(doctorTable)
                    .title("Exception")
                    .masthead("There is an exception")
                    .message("Ooops, there was an exception!")
                    .showException(ex);
        }

        doctorData.remove(selectedIndex);
    }

    public void cbSearchInit() {
        cbSearch.getItems().addAll("ID", "Name", "Department");
        cbSearch.setValue("ID");
    }

    private void createConnection() {
        try {
            Class.forName(driver).newInstance();
            con = DriverManager.getConnection(dbUrl);
            String sql = "SELECT * FROM APP.Doctor";
            st = con.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next()) {
                Doctor doctor = new Doctor();
                doctor.setIdProperty(rs.getString(1));
                doctor.setNameProperty(rs.getString(2));
                doctor.setDepartmentProperty(rs.getString(3));
                doctorData.add(doctor);
            }
            st.close();
            st = null;
        } catch (ClassNotFoundException | SQLException ex) {
        } catch (InstantiationException | IllegalAccessException ex) {
        }
    }

    public void initTable() {
        doctorIdColumn.setCellValueFactory(
                new PropertyValueFactory<>("IdProperty")
        );
        doctorNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("nameProperty")
        );
        departmentColumn.setCellValueFactory(
                new PropertyValueFactory<>("departmentProperty")
        );

        //Create fillter for search field
        FilteredList<Doctor> filteredData = new FilteredList<>(doctorData, p -> true);
        doctorTable.setItems(filteredData);
        tfDoctorSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(doctor -> {
                // If filter text is empty, display all
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                switch (cbSearch.getValue()) {
                    case "ID":
                        if (doctor.getIdProperty().toLowerCase().contains(lowerCaseFilter)) {
                            return true; 
                        }
                    break;
                    case "Name":
                        if (doctor.getNameProperty().toLowerCase().contains(lowerCaseFilter)) {
                            return true; 
                        }
                    break;
                    case "Department":
                        if (doctor.getDepartmentProperty().toLowerCase().contains(lowerCaseFilter)) {
                            return true; 
                        }
                    break;
                }
                return false;
            });
        });
    }

    public void setEditable() {
        doctorTable.setEditable(true);
        doctorIdColumn.setEditable(true);
        doctorIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorIdColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Doctor, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Doctor, String> t) {
                Doctor doctor = ((Doctor) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                if (t.getNewValue().length() == 0) {
                    forceRefresh(); // Regain CONTROL for an UNDO
                    Notifications.create().title("Edit Error")
                            .text("Field cannot be empty!")
                            .showError();
                } else if (isDuplicate(t.getNewValue())) {
                    forceRefresh();
                    Notifications.create().title("Add Error")
                            .text("Duplicate DoctorID")
                            .showError();
                } else {
                    String update = "UPDATE APP.DOCTOR SET ID = ? WHERE Name = ?";
                    try {
                        ps = con.prepareStatement(update);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, doctor.getNameProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;
                    } catch (SQLException ex) {
                        Dialogs.create()
                                .owner(doctorTable)
                                .title("Exception")
                                .masthead("There is an exception")
                                .message("Ooops, there was an exception!")
                                .showException(ex);
                    }
                    doctor.setIdProperty(t.getNewValue());
                }
            }
        });
        doctorNameColumn.setEditable(true);
        doctorNameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        doctorNameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Doctor, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Doctor, String> t) {
                Doctor doctor = ((Doctor) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                if (t.getNewValue().length() == 0) {
                    forceRefresh(); // Regain CONTROL for an UNDO
                    Notifications.create().title("Edit Error")
                            .text("Field cannot be empty!")
                            .showError();
                } else {
                    String update = "UPDATE APP.DOCTOR SET Name = ? WHERE ID = ?";
                    try {
                        ps = con.prepareStatement(update);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, doctor.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;
                    } catch (SQLException ex) {
                        Dialogs.create()
                                .owner(doctorTable)
                                .title("Exception")
                                .masthead("There is an exception")
                                .message("Ooops, there was an exception!")
                                .showException(ex);
                    }
                    doctor.setNameProperty(t.getNewValue());
                }
            }
        });
        departmentColumn.setEditable(true);
        departmentColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        departmentColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Doctor, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Doctor, String> t) {
                Doctor doctor = ((Doctor) t.getTableView().getItems().get(t.getTablePosition().getRow()));
                if (t.getNewValue().length() == 0) {
                    forceRefresh(); // Regain CONTROL for an UNDO
                    Notifications.create().title("Edit Error")
                            .text("Field cannot be empty!")
                            .showError();
                } else {
                    String update = "UPDATE APP.DOCTOR SET Department = ? WHERE ID = ?";
                    try {
                        ps = con.prepareStatement(update);
                        ps.setString(1, t.getNewValue());
                        ps.setString(2, doctor.getIdProperty());
                        ps.executeUpdate();
                        ps.close();
                        ps = null;
                    } catch (SQLException ex) {
                        Dialogs.create()
                                .owner(doctorTable)
                                .title("Exception")
                                .masthead("There is an exception")
                                .message("Ooops, there was an exception!")
                                .showException(ex);
                    }
                    doctor.setDepartmentProperty(t.getNewValue());
                }
            }
        });
    }

    public void forceRefresh() {
        new java.util.Timer().schedule(new java.util.TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    TableView<Doctor> docTab;
                    docTab = doctorTable;
                    docTab.getColumns().get(0).setVisible(false);
                    docTab.getColumns().get(0).setVisible(true);
                });
            }
        }, 50);
    }
    public boolean isDuplicate(String s) {
        for (Doctor doctor : doctorData) {
            if (s.equals(doctor.getIdProperty())) {
                return true;
            }
        }
        return false;
    }
}
