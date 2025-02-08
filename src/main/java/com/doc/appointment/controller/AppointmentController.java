package com.doc.appointment.controller;

import com.doc.appointment.model.Appointment;
import com.doc.appointment.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {

        System.out.println("Booked appointment: " + appointment);
        appointment.setId(System.currentTimeMillis());

        appointmentService.save(appointment);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping
    public List<Appointment> getAllAppointments(@RequestParam(required = false) String doctor) {
        if (doctor != null) {
            return appointmentService.getAppointmentsByDoctor(doctor);
        }
        return appointmentService.getAllAppointments();
    }
}

