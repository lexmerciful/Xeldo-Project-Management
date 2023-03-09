package com.lex.xeldoprojectmanagement.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ActivitySignUpBinding
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.models.Users
import java.text.SimpleDateFormat
import java.util.*

class SignUpActivity : BaseActivity() {

    private lateinit var binding: ActivitySignUpBinding

    private var gender = ""
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        hideStatusBar()
        setupActionBar()

        dateSetupListener()

        binding.btnSignUp.setOnClickListener {
            registerUser()
        }

        binding.etDob.setOnClickListener {
            val dpd = DatePickerDialog(this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH))

            dpd.datePicker.maxDate = System.currentTimeMillis() - 86400000
            dpd.show()
        }

        binding.rgGender.setOnCheckedChangeListener { _, checkedId ->
            gender = if (checkedId == R.id.rb_male){
                binding.rbMale.text.toString()
            }else{
                binding.rbFemale.text.toString()
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun dateSetupListener() {
        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            updateDateInView()
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

    private fun updateDateInView(){
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding.etDob.setText(sdf.format(cal.time).toString())
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
        val dob = binding.etDob.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val termsCondition = binding.cbTermsCondition

        if (validateForm(name, email, dob, gender, password, confirmPassword, termsCondition)){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                task ->

                    if (task.isSuccessful){
                        val firebaseUser: FirebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = Users(firebaseUser.uid, name, registeredEmail, dob = dob, gender = gender)
                        FirestoreClass().registerUser(this, user)
                    }else{
                        hideProgressDialog()
                        Toast.makeText(this,
                            task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun validateForm(name: String, email:String, dob: String, gender: String, password: String, confirmPassword: String, termsCondition: CheckBox): Boolean{
        var yearCheck = 0
        if (dob.isNotEmpty()){
            yearCheck = dob.substring(6).toInt()
        }
        val yearMax = 2013
        return when {
            name.length < 3 ->{
                showErrorSnackBar("Please enter a valid name")
                false
            }
            TextUtils.isEmpty(email) || !email.contains("@") ->{
                showErrorSnackBar("Please enter a valid email address")
                false
            }
            TextUtils.isEmpty(dob) || yearCheck > yearMax ->{
                showErrorSnackBar("Please select a valid date of birth")
                false
            }
            TextUtils.isEmpty(gender) ->{
                showErrorSnackBar("Please select your gender")
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
            !termsCondition.isChecked ->{
                showErrorSnackBar("Please accept terms and condition")
                false
            }
            else -> true
        }
    }
}