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

        appointmentService.save(appointment);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/getAll")
    public List<Appointment> getAllAppointments() {
        System.out.println("Called getAll appointment: " + appointmentService.getAllAppointments());
        return appointmentService.getAllAppointments();
    }
}

