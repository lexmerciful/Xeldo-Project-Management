package com.lex.xeldoprojectmanagement.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.adapters.BoardItemAdapter
import com.lex.xeldoprojectmanagement.databinding.ActivityMainBinding
import com.lex.xeldoprojectmanagement.databinding.NavHeaderMainBinding
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.models.Board
import com.lex.xeldoprojectmanagement.models.Users
import com.lex.xeldoprojectmanagement.utils.Constants

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object{
        //const val MY_PROFILE_REQUEST_CODE = 11
    }

    private lateinit var mUsername: String
    private lateinit var binding: ActivityMainBinding

    private lateinit var mSharedPreferences: SharedPreferences
    private var callingActivity = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupActionBar()
        binding.navView.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(
            Constants.XELDOPROJECT_PREFERENCES, Context.MODE_PRIVATE
        )

        // This is to get the value whether the token is updated or not
        val tokenUpdated = mSharedPreferences.getBoolean(
            Constants.FCM_TOKEN_UPDATED, false)

        /**
         * If we have a updated token, load the user data and if not
         * get a updated token, we get it from the instance and call the
         * updateFCMToken and pass the userHashMap and update the user profile
         * with the updated token
         */
        if (tokenUpdated){
            showProgressDialog(resources.getString(R.string.finalizing))
            FirestoreClass().loadUserData(this, true)
        } else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener(this@MainActivity){
                updateFCMToken(it)
            }
        }

        binding.mainInclude.fabCreateBoard.setOnClickListener {
            val intent = Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUsername)
            callingActivity = Constants.CREATE_BOARD
            resultLauncher.launch(intent)
        }

        FirestoreClass().loadUserData(this, true)

    }

    fun populateBoardsListToUI(boardsList: ArrayList<Board>){
        hideProgressDialog()

        if (boardsList.size > 0){
            binding.mainInclude.mainContentInclude.rvBoardsList.visibility = View.VISIBLE
            binding.mainInclude.mainContentInclude.tvNoBoardsAvailable.visibility = View.GONE

            binding.mainInclude.mainContentInclude.rvBoardsList.layoutManager = LinearLayoutManager(this)
            binding.mainInclude.mainContentInclude.rvBoardsList.setHasFixedSize(true)

            val adapter = BoardItemAdapter(this, boardsList)
            binding.mainInclude.mainContentInclude.rvBoardsList.adapter = adapter

            adapter.setOnClickListener(object: BoardItemAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }

            })

        }else{
            binding.mainInclude.mainContentInclude.rvBoardsList.visibility = View.GONE
            binding.mainInclude.mainContentInclude.tvNoBoardsAvailable.visibility = View.VISIBLE
        }
    }

    private fun setupActionBar(){
        setSupportActionBar(binding.mainInclude.toolbarMainActivity)
        binding.mainInclude.toolbarMainActivity.title = resources.getString(R.string.dashboardTitle)
        binding.mainInclude.toolbarMainActivity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        binding.mainInclude.toolbarMainActivity.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer(){
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.nav_my_profile ->{
                val intent = (Intent(this,
                    ProfileActivity::class.java))
                callingActivity = Constants.PROFILE
                resultLauncher.launch(intent)
            }

            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && callingActivity == Constants.PROFILE) {
            // There are no request codes
            //val data: Intent? = result.data
            FirestoreClass().loadUserData(this)
        }else if (result.resultCode == Activity.RESULT_OK && callingActivity == Constants.CREATE_BOARD){
            FirestoreClass().getBoardsList(this)
        }
        else{
            Log.e("Cancelled: ","CANCELLED")
        }
    }

    fun updateNavigationUserDetails(user: Users?, readBoardsList: Boolean) {
        hideProgressDialog()
        val headerView = binding.navView.getHeaderView(0)
        val headerBinding = NavHeaderMainBinding.bind(headerView)
        mUsername = user!!.name

        Glide
            .with(this)
            .load(user!!.image)
            .circleCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(headerBinding.navUserImage)

        headerBinding.tvUsername.text =user.name

        if (readBoardsList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    /**
     * This is called when we have a updated token and we get the updated
     * token when the user is registered or logs in
     */
    fun tokenUpdateSuccess() {
        hideProgressDialog()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()
        showProgressDialog(resources.getString(R.string.finalizing))
        FirestoreClass().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this, userHashMap)
    }

}