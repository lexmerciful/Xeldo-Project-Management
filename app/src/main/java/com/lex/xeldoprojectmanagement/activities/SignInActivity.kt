package com.lex.xeldoprojectmanagement.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ActivitySignInBinding
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.models.Users

class SignInActivity : BaseActivity() {

    private lateinit var binding: ActivitySignInBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignInBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        hideStatusBar()
        setupActionBar()

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.btnSignIn.setOnClickListener {
            signInUser()
        }

        binding.tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarSignInActivity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_arrow_back_24)
        }

        binding.toolbarSignInActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun hideStatusBar(){
        // To hide the status bar and make the splash screen a full screen activity.
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    /**
     * Function to get the user details from the firestore database after authentication.
     */
    fun signInSuccess(users: Users){
        hideProgressDialog()
        startActivity(Intent(this@SignInActivity, MainActivity::class.java))
        finish()
    }

    /**
     * Function for Sign-In using the registered user using the email and password.
     */
    private fun signInUser(){
        // Get the text and trim the space
        val email = binding.etEmailSignIn.text.toString().trim()
        val password = binding.etPasswordSignIn.text.toString()
        if (validateForm(email, password)){
            showProgressDialog(resources.getString(R.string.please_wait))

            // Sign-In using FirebaseAuth
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                task ->
                run {
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Log.d("SIGN IN SUCCESS", "$user signInWithCustomToken:success")

                        // Calling the FirestoreClass signInUser function to get the data of user from database.
                        FirestoreClass().loadUserData(this)
                    }else{
                        Log.w("SIGN IN FAILED", "signInWithCustomToken:failure", task.exception)
                        Toast.makeText(baseContext, "Login authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Function to validate the entries of user.
     */
    private fun validateForm(email:String, password: String): Boolean{
        return when {
            TextUtils.isEmpty(email) || !email.contains("@") ->{
                showErrorSnackBar("Please enter a valid email address")
                false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please enter your password")
                false
            }
            password.length < 5 ->{
                showErrorSnackBar("Please enter your valid password")
                false
            }
            else -> true
        }
    }
}