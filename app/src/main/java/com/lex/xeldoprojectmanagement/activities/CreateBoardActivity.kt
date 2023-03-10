package com.lex.xeldoprojectmanagement.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ActivityCreateBoardBinding
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.models.Board
import com.lex.xeldoprojectmanagement.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    companion object{
        //lateinit var resultLauncher: ActivityResultLauncher<Intent>
        private const val READ_STORAGE_PERMISSION_CODE = 1
        //private const val PICK_IMAGE_REQUEST_CODE = 2
    }

    private var mBoardImageURL: String = ""

    private lateinit var mUsername: String
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var binding: ActivityCreateBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)){
            mUsername = intent.getStringExtra(Constants.NAME).toString()
        }

        binding.ivBoardImage.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                    showImageChooser(this)
                }else{
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        READ_STORAGE_PERMISSION_CODE
                    )
                }
        }

        binding.btnCreate.setOnClickListener {
            if (mSelectedImageFileUri != null){
                uploadBoardImage()
            }else{
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun createBoard(){
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        var board = Board(
            binding.etBoardName.text.toString(),
            mBoardImageURL,
            mUsername,
            assignedUsersArrayList)

        FirestoreClass().createBoard(this, board)
    }

    private fun uploadBoardImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (mSelectedImageFileUri != null){

            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(
                    "BOARD_IMAGE ${System.currentTimeMillis()}.${getFileExtension(this, mSelectedImageFileUri)}")

            sRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener {
                    taskSnapshot ->

                hideProgressDialog()
                Log.i("Board Image URL", taskSnapshot.metadata!!.reference!!.downloadUrl.toString())

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    Log.i("Download Board Img URI", uri.toString())

                    mBoardImageURL = uri.toString()

                    createBoard()
                }
            }.addOnFailureListener {
                    exception ->
                Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()

                hideProgressDialog()
            }
        }
    }

    fun boardCreatedSuccessfully(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar(){
        setSupportActionBar(binding.toolbarCreateBoardActivity)
        binding.toolbarCreateBoardActivity.title = resources.getString(R.string.create_board_title)

        val actionBar = supportActionBar
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_arrow_back_24)
        }

        binding.toolbarCreateBoardActivity.setNavigationOnClickListener {
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
                showImageChooser(this)
            }
        }else{
            Toast.makeText(this,
                "Storage permission denied!!!",
                Toast.LENGTH_LONG).show()
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val data: Intent? = result.data
        if (result.resultCode == Activity.RESULT_OK && data!!.data != null) {
            // There are no request codes
            mSelectedImageFileUri = data.data

            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .circleCrop()
                    .format(DecodeFormat.PREFER_RGB_565)
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding.ivBoardImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}