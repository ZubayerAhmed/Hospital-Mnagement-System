package com.model;

import javafx.beans.property.SimpleStringProperty;

public class Patient {

    private SimpleStringProperty idProperty = new SimpleStringProperty();
    private SimpleStringProperty nameProperty = new SimpleStringProperty();
    private SimpleStringProperty detailsProperty = new SimpleStringProperty();
    private SimpleStringProperty doctorNameProperty = new SimpleStringProperty();
    public void setIdProperty(String s) {
        this.idProperty.setValue(s);
    }

    public void setNameProperty(String s) {
        this.nameProperty.setValue(s);
    }

    public void setDetailsProperty(String s) {
        this.detailsProperty.setValue(s);
    }

    public String getIdProperty() {
        return idProperty.get();
    }

    public String getNameProperty() {
        return nameProperty.get();
    }

    public String getDetailsProperty() {
        return detailsProperty.get();
    }

    public String getDoctorNameProperty() {
        return doctorNameProperty.get();
    }

    public void setDoctorNameProperty(String doctorNameProperty) {
        this.doctorNameProperty.setValue(doctorNameProperty);
    }
}
