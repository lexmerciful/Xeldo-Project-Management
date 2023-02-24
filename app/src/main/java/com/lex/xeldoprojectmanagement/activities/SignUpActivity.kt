package com.lex.xeldoprojectmanagement.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ActivitySignUpBinding
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.models.Users

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        hideStatusBar()
        setupActionBar()

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_arrow_back_24)
        }

        binding.toolbarSignUpActivity.setNavigationOnClickListener {
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

    fun userRegisteredSuccess(){
        hideProgressDialog()
        Toast.makeText(this,
            "You have successfully registered", Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun registerUser(){
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        if (validateForm(name, email, password, confirmPassword)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                task ->

                    if (task.isSuccessful){
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = Users(firebaseUser.uid, name, registeredEmail)
                        FirestoreClass().registerUser(this, user)
                    }else{
                        Toast.makeText(this,
                            task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun validateForm(name: String, email:String, password: String, confirmPassword: String): Boolean{
        return when {
            name.length < 3 ->{
                showErrorSnackBar("Please enter a valid name")
                false
            }
            TextUtils.isEmpty(email) || !email.contains("@") ->{
                showErrorSnackBar("Please enter a valid email address")
                false
            }
            TextUtils.isEmpty(password) ->{
                showErrorSnackBar("Please enter your password")
                false
            }
            password.length < 5 ->{
                showErrorSnackBar("Password is too weak")
                false
            }
            confirmPassword != password ->{
                showErrorSnackBar("Password does not match")
                false
            }
            else -> true
        }
    }
}