package com.health.virtualdoctor.ui.data.models

import com.google.gson.annotations.SerializedName

data class AppointmentRequest(

    @SerializedName("doctorId")
    val doctorId: String,

    @SerializedName("appointmentDateTime")
    val appointmentDateTime: String, // Send as ISO-8601 string (e.g. "2025-11-14T18:30:00")

    @SerializedName("appointmentType")
    val appointmentType: String,

    @SerializedName("reason")
    val reason: String,

    @SerializedName("notes")
    val notes: String?
)
