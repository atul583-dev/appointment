package com.doc.appointment.controller;

import com.doc.appointment.model.Appointment;
import com.doc.appointment.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {

        System.out.println("Booked appointment: " + appointment);
        appointment.setId(System.currentTimeMillis()); // Temporary ID

        appointmentService.save(appointment);
        return ResponseEntity.ok(appointment);
    }
}

