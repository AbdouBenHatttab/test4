package com.healthapp.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "doctor-activation-service", url = "http://localhost:8083")
public interface DoctorServiceClient {

    @PostMapping("/api/doctors/appointments/from-patient")
    Map<String, Object> createAppointmentFromPatient(@RequestBody Map<String, Object> request);

    @GetMapping("/api/doctors/appointments/patient/{patientId}")
    List<Map<String, Object>> getPatientAppointments(@PathVariable String patientId);

    @PostMapping("/api/doctors/appointments/{appointmentId}/cancel")
    void cancelAppointment(@PathVariable String appointmentId, @RequestBody Map<String, String> body);

    @GetMapping("/api/doctors/available")
    List<Map<String, Object>> getActivatedDoctors();
}