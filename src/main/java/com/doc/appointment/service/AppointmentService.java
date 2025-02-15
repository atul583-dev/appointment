package com.doc.appointment.service;

import com.doc.appointment.model.Appointment;
import com.doc.appointment.repo.AppointmentRepository;
import com.doc.appointment.utils.AppointmentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment save(Appointment appointment) {

        appointment.setStatus(AppointmentUtils.getStatus(appointment.getDate(), appointment.getTime()));
        System.out.println("Saving An Appointment : " + appointment);
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByDoctor(String doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    public Appointment findByPhone(String phone) {
        return appointmentRepository.findByPhone(phone);
    }

    public boolean isSlotAvailable(String date, String time) {
        System.out.println("Date : " + date);
        System.out.println("Time : " + time);
        return !appointmentRepository.existsByDateAndTime(date, time);
    }

    public void cancelAppointment(String phone) {
        appointmentRepository.deleteByPhone(phone);
    }
}
