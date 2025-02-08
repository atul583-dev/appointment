package com.doc.appointment.service;

import com.doc.appointment.model.Appointment;
import com.doc.appointment.repo.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    public Appointment save(Appointment appointment) {
        System.out.println("Saving");
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getAppointmentsByDoctor(String doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }
}
