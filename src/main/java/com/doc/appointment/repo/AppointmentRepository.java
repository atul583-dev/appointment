package com.doc.appointment.repo;

import com.doc.appointment.model.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    List<Appointment> findByName(String name);

    List<Appointment> findByDoctor(String doctor);

    Appointment findByPhone(String phone);

    boolean existsByDateAndTime(String date, String time);

    void deleteByPhone(String phone);
}
