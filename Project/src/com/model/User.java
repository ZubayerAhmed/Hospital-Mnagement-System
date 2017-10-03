package com.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;


public class User {
    private SimpleStringProperty username = new SimpleStringProperty();
    private SimpleStringProperty passworld = new SimpleStringProperty();
    private SimpleStringProperty email = new SimpleStringProperty();
    private SimpleIntegerProperty level = new SimpleIntegerProperty();
    private SimpleStringProperty doctorID = new SimpleStringProperty();

    public String getUsername() {
        return username.get();
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassworld() {
        return passworld.get();
    }

    public void setPassworld(String passworld) {
        this.passworld.set(passworld);
    }

    public String getEmail() {
        return email.get();
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public int getLevel() {
        return level.get();
    }

    public void setLevel(int level) {
        this.level.set(level);
    }
    public String getDoctorID() {
        return doctorID.get();
    }

    public void setDoctorID(String doctorNameProperty) {
        this.doctorID.setValue(doctorNameProperty);
    }
}
