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

        System.out.println("Checking if slot is available");
        System.out.println("Is slot available: " + appointmentService.isSlotAvailable(appointment.getDate(), appointment.getTime()));

        appointmentService.save(appointment);
        return ResponseEntity.ok(appointment);
    }

    @GetMapping("/getAll")
    public List<Appointment> getAllAppointments() {
        System.out.println("Called getAll appointment: " + appointmentService.getAllAppointments());
        return appointmentService.getAllAppointments();
    }

    @GetMapping("/search")
    public Appointment searchAppointments(String phone) {
        System.out.println("Searching an appointment " + appointmentService.findByPhone(phone));
        return appointmentService.findByPhone(phone);
    }
}

