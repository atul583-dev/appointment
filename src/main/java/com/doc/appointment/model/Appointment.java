package com.doc.appointment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "appointment")
public class Appointment {

    @Id
    private Long id;
    private String name;
    private String phone;
    private String date;
    private String time;
    private String doctor;

    // Constructors
    public Appointment() {
    }

    public Appointment(Long id, String name, String phone, String date, String time, String doctor) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.doctor = doctor;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", doctor='" + doctor + '\'' +
                '}';
    }
}
