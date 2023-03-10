package com.lex.xeldoprojectmanagement.activities

import android.app.Activity
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupActionBar()
        binding.navView.setNavigationItemSelectedListener(this)

        binding.mainInclude.fabCreateBoard.setOnClickListener {
            val intent = Intent(this,CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUsername)
            startActivity(intent)
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
                resultLauncher.launch(intent)
            }

            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()

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
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            //val data: Intent? = result.data
            FirestoreClass().loadUserData(this)
        }else{
            Log.e("Cancelled: ","CANCELLED")
        }
    }

    fun updateNavigationUserDetails(user: Users?, readBoardsList: Boolean) {
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

}