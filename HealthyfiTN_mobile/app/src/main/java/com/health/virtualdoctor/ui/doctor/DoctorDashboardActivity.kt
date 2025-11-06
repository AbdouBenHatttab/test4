package com.health.virtualdoctor.ui.doctor

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.health.virtualdoctor.R
import com.health.virtualdoctor.ui.data.api.RetrofitClient
import com.health.virtualdoctor.ui.data.models.UpdateDoctorProfileRequest
import com.health.virtualdoctor.ui.utils.TokenManager
import kotlinx.coroutines.launch

class DoctorDashboardActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    // Views
    private lateinit var tvDoctorName: TextView
    private lateinit var tvDoctorEmail: TextView
    private lateinit var tvActivationStatus: TextView
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etSpecialization: EditText
    private lateinit var etHospital: EditText
    private lateinit var etYearsOfExperience: EditText
    private lateinit var etOfficeAddress: EditText
    private lateinit var etConsultationHours: EditText
    private lateinit var btnUpdateProfile: Button
    private lateinit var btnCheckActivation: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_dashboard)

        tokenManager = TokenManager(this)

        initViews()
        setupListeners()
        loadDoctorProfile()
    }

    private fun initViews() {
        tvDoctorName = findViewById(R.id.tvDoctorName)
        tvDoctorEmail = findViewById(R.id.tvDoctorEmail)
        tvActivationStatus = findViewById(R.id.tvActivationStatus)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etSpecialization = findViewById(R.id.etSpecialization)
        etHospital = findViewById(R.id.etHospital)
        etYearsOfExperience = findViewById(R.id.etYearsOfExperience)
        etOfficeAddress = findViewById(R.id.etOfficeAddress)
        etConsultationHours = findViewById(R.id.etConsultationHours)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)
        btnCheckActivation = findViewById(R.id.btnCheckActivation)
    }

    private fun setupListeners() {
        btnUpdateProfile.setOnClickListener {
            updateDoctorProfile()
        }

        btnCheckActivation.setOnClickListener {
            checkActivationStatus()
        }
    }

    private fun loadDoctorProfile() {
        lifecycleScope.launch {
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"

                // ✅ Call DOCTOR SERVICE (port 8083)
                val response = RetrofitClient.getDoctorService(this@DoctorDashboardActivity)
                    .getDoctorProfile(token)

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!

                    // Display profile info
                    tvDoctorName.text = profile.fullName
                    tvDoctorEmail.text = profile.email
                    tvActivationStatus.text = if (profile.isActivated) {
                        "✅ Activated"
                    } else {
                        "⏳ Pending Activation"
                    }

                    // Pre-fill edit fields
                    etFirstName.setText(profile.firstName)
                    etLastName.setText(profile.lastName)
                    etPhoneNumber.setText(profile.phoneNumber ?: "")
                    etSpecialization.setText(profile.specialization)
                    etHospital.setText(profile.hospitalAffiliation)
                    etYearsOfExperience.setText(profile.yearsOfExperience.toString())
                    etOfficeAddress.setText(profile.officeAddress ?: "")
                    etConsultationHours.setText(profile.consultationHours ?: "")

                    Log.d("DoctorProfile", "✅ Profile loaded: ${profile.email}")
                } else {
                    val error = response.errorBody()?.string() ?: "Error loading profile"
                    Toast.makeText(this@DoctorDashboardActivity, "❌ $error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("DoctorProfile", "❌ Exception: ${e.message}", e)
                Toast.makeText(
                    this@DoctorDashboardActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateDoctorProfile() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val specialization = etSpecialization.text.toString().trim()
        val hospital = etHospital.text.toString().trim()
        val yearsOfExperience = etYearsOfExperience.text.toString().trim().toIntOrNull()
        val officeAddress = etOfficeAddress.text.toString().trim()
        val consultationHours = etConsultationHours.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || specialization.isEmpty()) {
            Toast.makeText(this, "⚠️ Required fields are missing", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                btnUpdateProfile.isEnabled = false
                btnUpdateProfile.text = "Updating..."

                val token = "Bearer ${tokenManager.getAccessToken()}"
                val request = UpdateDoctorProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber.ifEmpty { null },
                    specialization = specialization,
                    hospitalAffiliation = hospital,
                    yearsOfExperience = yearsOfExperience,
                    officeAddress = officeAddress.ifEmpty { null },
                    consultationHours = consultationHours.ifEmpty { null }
                )

                // ✅ Call DOCTOR SERVICE (port 8083)
                val response = RetrofitClient.getDoctorService(this@DoctorDashboardActivity)
                    .updateDoctorProfile(token, request)

                if (response.isSuccessful && response.body() != null) {
                    val updatedProfile = response.body()!!

                    tvDoctorName.text = updatedProfile.fullName

                    Toast.makeText(
                        this@DoctorDashboardActivity,
                        "✅ Profile updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d("DoctorProfile", "✅ Profile updated: ${updatedProfile.email}")
                } else {
                    val error = response.errorBody()?.string() ?: "Update failed"
                    Toast.makeText(this@DoctorDashboardActivity, "❌ $error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("DoctorProfile", "❌ Exception: ${e.message}", e)
                Toast.makeText(
                    this@DoctorDashboardActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnUpdateProfile.isEnabled = true
                btnUpdateProfile.text = "Update Profile"
            }
        }
    }

    private fun checkActivationStatus() {
        lifecycleScope.launch {
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"

                // ✅ Call DOCTOR SERVICE (port 8083)
                val response = RetrofitClient.getDoctorService(this@DoctorDashboardActivity)
                    .getDoctorActivationStatus(token)

                if (response.isSuccessful && response.body() != null) {
                    val status = response.body()!!
                    val isActivated = status["isActivated"] as? Boolean ?: false
                    val message = status["message"] as? String ?: "Unknown"

                    tvActivationStatus.text = if (isActivated) {
                        "✅ Activated"
                    } else {
                        "⏳ $message"
                    }

                    Toast.makeText(this@DoctorDashboardActivity, message, Toast.LENGTH_LONG).show()

                    Log.d("DoctorProfile", "✅ Activation status: $isActivated")
                } else {
                    val error = response.errorBody()?.string() ?: "Failed to check status"
                    Toast.makeText(this@DoctorDashboardActivity, "❌ $error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("DoctorProfile", "❌ Exception: ${e.message}", e)
                Toast.makeText(
                    this@DoctorDashboardActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
