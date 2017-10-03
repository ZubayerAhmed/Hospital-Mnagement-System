package com.model;


import javafx.beans.property.SimpleStringProperty;

public class Doctor {
        private  SimpleStringProperty idProperty = new SimpleStringProperty();
        private  SimpleStringProperty nameProperty = new SimpleStringProperty();
        private  SimpleStringProperty departmentProperty = new SimpleStringProperty();
        
        public void setIdProperty(String s){
            this.idProperty.setValue(s);
        }
        public void setNameProperty(String s){
            this.nameProperty.setValue(s);
        }
        public void setDepartmentProperty(String s){
            this.departmentProperty.setValue(s);
        }
        public String getIdProperty(){
            return idProperty.get();
        }
        public String getNameProperty() {
            return nameProperty.get();
        }
        public String getDepartmentProperty() {
            return departmentProperty.get();
        }
    }