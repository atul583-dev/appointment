package com.doc.appointment.repo;

import com.doc.appointment.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    // Custom queries can be defined here
    List<Appointment> findByName(String name);
}
