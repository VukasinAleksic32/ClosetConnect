package com.example.closetconnect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var buttonRegister: Button
    private lateinit var textViewLogin:TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_page)

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        initializeViews()

        // Set up register button click listener
        buttonRegister.setOnClickListener {
            registerUser()
        }
        textViewLogin.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }
    }

    private fun initializeViews() {
        editTextFullName = findViewById(R.id.editTextFullName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        buttonRegister = findViewById(R.id.buttonRegister)
        textViewLogin = findViewById(R.id.textViewLogin)
    }

    private fun registerUser() {
        // TRIM ! SIGURNOSNI MEHANIZAM !
        val fullName = editTextFullName.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val confirmPassword = editTextConfirmPassword.text.toString().trim()

        // Validate input
        if (!validateInput(fullName, email, password, confirmPassword)) {
            return
        }

        // Create user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Get the current user
                    val firebaseUser = auth.currentUser

                    // Create user document in Firestore
                    firebaseUser?.let { user ->
                        val userDocument = hashMapOf(
                            "uid" to user.uid,
                            "fullName" to fullName,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )

                        // Save user to Firestore
                        firestore.collection("users")
                            .document(user.uid)
                            .set(userDocument)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Registration Successful",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Navigate to next screen or login
                                navigateToMainScreen()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Firestore Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun validateInput(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        // Full Name validation
        if (fullName.isEmpty()) {
            editTextFullName.error = "Full name is required"
            return false
        }

        // Email validation
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "Enter a valid email address"
            return false
        }

        // Password validation
        if (password.isEmpty() || password.length < 6) {
            editTextPassword.error = "Password must be at least 6 characters"
            return false
        }

        // Confirm password
        if (password != confirmPassword) {
            editTextConfirmPassword.error = "Passwords do not match"
            return false
        }

        return true
    }

    private fun navigateToMainScreen() {
        // Replace with your actual main screen or login screen
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}