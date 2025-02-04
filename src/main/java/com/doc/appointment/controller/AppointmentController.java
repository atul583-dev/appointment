package com.doc.appointment.controller;

import com.doc.appointment.model.Appointment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @PostMapping
    public ResponseEntity<Appointment> createAppointment(@RequestBody Appointment appointment) {

        System.out.println("Booked appointment: " + appointment);
        // Save the appointment (Assume you are using a service to save it in DB)
        appointment.setId(System.currentTimeMillis()); // Temporary ID
        return ResponseEntity.ok(appointment);
    }
}

