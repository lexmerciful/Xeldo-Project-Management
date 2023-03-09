package com.lex.xeldoprojectmanagement.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ActivityProfileBinding
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.models.Users
import com.lex.xeldoprojectmanagement.utils.Constants
import java.io.IOException

class ProfileActivity : BaseActivity() {

    companion object{
        private const val READ_STORAGE_PERMISSION_CODE = 1
        private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserDetails: Users
    private var mProfileImageURL: String = ""
    private lateinit var mCurrentUser: Users

    private var editProfile = false
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupActionBar()

        FirestoreClass().loadUserData(this)

        binding.ivProfileUserImage.setOnClickListener {
            if (editProfile){
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                    showImageChooser()
                }else{
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_STORAGE_PERMISSION_CODE)
                }
            }

        }

        binding.btnEditProfile.setOnClickListener {
            editProfileSet()
        }

        binding.btnUpdate.setOnClickListener {
            if(binding.etName.text?.isEmpty() == true){
                showErrorSnackBar("Name can't be empty")
                return@setOnClickListener
            }
            if (mSelectedImageFileUri != null){
                mCurrentUser = Users(getCurrentUserID())
                uploadUserImage(mCurrentUser)
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

    }

    private fun editProfileSet() {
        if (!editProfile) {
            binding.btnEditProfile.setTextColor(ContextCompat.getColor(this, R.color.editBtn))
            binding.etName.isEnabled = true
            binding.etMobile.isEnabled = true
            binding.btnUpdate.visibility = View.VISIBLE
            editProfile = true
        } else {
            binding.btnEditProfile.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.etName.isEnabled = false
            binding.etMobile.isEnabled = false
            binding.btnUpdate.visibility = View.GONE
            editProfile = false
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarMyProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
            actionBar.title = resources.getString(R.string.my_profile)
        }

        binding.toolbarMyProfileActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImageChooser()
            }
        }else{
            Toast.makeText(this,
                "Storage permission denied!!!",
                Toast.LENGTH_LONG).show()
        }
    }

    private fun showImageChooser(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null){
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this@ProfileActivity)
                    .load(mSelectedImageFileUri)
                    .circleCrop()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(binding.ivProfileUserImage)
            }catch (e: IOException){
                e.printStackTrace()
            }

        }
    }

    fun setUserDataInUI(user: Users){

        mUserDetails = user

        Glide
            .with(this)
            .load(user.image)
            .circleCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding.ivProfileUserImage)

        binding.etName.setText(user.name)
        binding.etEmail.setText(user.email)
        binding.etDobProfile.setText(user.dob)
        if (user.mobile != 0L){
            binding.etMobile.setText(user.mobile.toString())
        }
        if (user.gender == "MALE"){
            binding.rbMaleProfile.isChecked = true
            binding.rbFemaleProfile.visibility = View.GONE
        }else if (user.gender == "FEMALE"){
            binding.rbFemaleProfile.isChecked = true
            binding.rbMaleProfile.visibility = View.GONE
        }else{
            binding.rbMaleProfile.isChecked = false
            binding.rbFemaleProfile.isChecked = false
        }

    }

    private fun updateUserProfileData(){
        val userHashMap = HashMap<String, Any>()
        var anyChangesMade = false

        if (mProfileImageURL.isNotEmpty() && mProfileImageURL != mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
            anyChangesMade = true
        }

        if (binding.etName.text.toString() != mUserDetails.name){
            userHashMap[Constants.NAME] = binding.etName.text.toString()
            anyChangesMade = true
        }

        if (binding.etMobile.text.toString() != mUserDetails.mobile.toString() && binding.etMobile.text.toString().isNotEmpty()){
            userHashMap[Constants.MOBILE] = binding.etMobile.text.toString().toLong()
            anyChangesMade = true
        }

        if (anyChangesMade){
            FirestoreClass().updateUserProfileData(this, userHashMap)
            hideProgressDialog()
        }else{
            hideProgressDialog()
        }
    }

    private fun uploadUserImage(user: Users){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null){

            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "${user.id}.${getFileExtension(mSelectedImageFileUri)}")

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                taskSnapshot ->

                hideProgressDialog()
                Log.i("Firebase Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    Log.i("Downloadable Image URI", uri.toString())

                    mProfileImageURL = uri.toString()
                    updateUserProfileData()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()

                hideProgressDialog()
            }
        }
    }

    //To get the image file extension
    private fun getFileExtension(uri: Uri?): String?{
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

}