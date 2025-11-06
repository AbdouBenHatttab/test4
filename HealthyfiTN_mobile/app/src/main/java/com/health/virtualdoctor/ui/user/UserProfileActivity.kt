package com.health.virtualdoctor.ui.user


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.health.virtualdoctor.R
import com.health.virtualdoctor.ui.data.api.RetrofitClient
import com.health.virtualdoctor.ui.data.models.ChangePasswordRequest
import com.health.virtualdoctor.ui.data.models.UpdateUserProfileRequest
import com.health.virtualdoctor.ui.utils.TokenManager
import kotlinx.coroutines.launch

class UserProfileActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager

    // Views
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var tvUserRole: TextView
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnUpdateProfile: Button
    private lateinit var btnChangePassword: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        tokenManager = TokenManager(this)

        initViews()
        setupListeners()
        loadUserProfile()
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserRole = findViewById(R.id.tvUserRole)
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile)
        btnChangePassword = findViewById(R.id.btnChangePassword)
    }

    private fun setupListeners() {
        btnUpdateProfile.setOnClickListener {
            updateUserProfile()
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun loadUserProfile() {
        lifecycleScope.launch {
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"

                // ✅ Call USER SERVICE (port 8085)
                val response = RetrofitClient.getUserService(this@UserProfileActivity)
                    .getUserProfile(token)

                if (response.isSuccessful && response.body() != null) {
                    val profile = response.body()!!

                    // Display profile info
                    tvUserName.text = profile.fullName
                    tvUserEmail.text = profile.email
                    tvUserRole.text = profile.roles.joinToString(", ")

                    // Pre-fill edit fields
                    etFirstName.setText(profile.firstName)
                    etLastName.setText(profile.lastName)
                    etPhoneNumber.setText(profile.phoneNumber ?: "")

                    Log.d("UserProfile", "✅ Profile loaded: ${profile.email}")
                } else {
                    val error = response.errorBody()?.string() ?: "Error loading profile"
                    Toast.makeText(this@UserProfileActivity, "❌ $error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("UserProfile", "❌ Exception: ${e.message}", e)
                Toast.makeText(
                    this@UserProfileActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateUserProfile() {
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "⚠️ First name and last name are required", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                btnUpdateProfile.isEnabled = false
                btnUpdateProfile.text = "Updating..."

                val token = "Bearer ${tokenManager.getAccessToken()}"
                val request = UpdateUserProfileRequest(
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = phoneNumber.ifEmpty { null },
                    email = null,
                    profilePictureUrl = null
                )

                // ✅ Call USER SERVICE (port 8085)
                val response = RetrofitClient.getUserService(this@UserProfileActivity)
                    .updateUserProfile(token, request)

                if (response.isSuccessful && response.body() != null) {
                    val updatedProfile = response.body()!!

                    tvUserName.text = updatedProfile.fullName

                    Toast.makeText(
                        this@UserProfileActivity,
                        "✅ Profile updated successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d("UserProfile", "✅ Profile updated: ${updatedProfile.email}")
                } else {
                    val error = response.errorBody()?.string() ?: "Update failed"
                    Toast.makeText(this@UserProfileActivity, "❌ $error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("UserProfile", "❌ Exception: ${e.message}", e)
                Toast.makeText(
                    this@UserProfileActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnUpdateProfile.isEnabled = true
                btnUpdateProfile.text = "Update Profile"
            }
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = dialogView.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        MaterialAlertDialogBuilder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(this, "⚠️ All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "⚠️ Passwords don't match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                changePassword(currentPassword, newPassword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                val token = "Bearer ${tokenManager.getAccessToken()}"
                val request = ChangePasswordRequest(currentPassword, newPassword)

                // ✅ Call USER SERVICE (port 8085)
                val response = RetrofitClient.getUserService(this@UserProfileActivity)
                    .changePassword(token, request)

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@UserProfileActivity,
                        "✅ Password changed successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    Log.d("UserProfile", "✅ Password changed")
                } else {
                    val error = response.errorBody()?.string() ?: "Password change failed"
                    Toast.makeText(this@UserProfileActivity, "❌ $error", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("UserProfile", "❌ Exception: ${e.message}", e)
                Toast.makeText(
                    this@UserProfileActivity,
                    "❌ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}