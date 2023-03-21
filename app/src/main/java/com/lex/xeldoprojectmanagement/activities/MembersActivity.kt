package com.lex.xeldoprojectmanagement.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.adapters.MembersListItemsAdapter
import com.lex.xeldoprojectmanagement.databinding.ActivityMembersBinding
import com.lex.xeldoprojectmanagement.databinding.DialogAddMemberBinding
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.models.Board
import com.lex.xeldoprojectmanagement.models.Users
import com.lex.xeldoprojectmanagement.utils.Constants

class MembersActivity : BaseActivity() {

    private lateinit var binding: ActivityMembersBinding

    private lateinit var mBoardDetails: Board

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMembersBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupActionBar(binding.toolbarMembersActivity, resources.getString(R.string.members))

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(this, mBoardDetails.assignedTo)
    }

    fun setupMembersListAdapter(list: ArrayList<Users>){
        hideProgressDialog()

        binding.rvMembersList.layoutManager = LinearLayoutManager(this)
        binding.rvMembersList.setHasFixedSize(true)

        val adapter = MembersListItemsAdapter(this, list)
        binding.rvMembersList.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_add_member ->{
                dialogAddMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogAddMember(){
        val dialogBinding: DialogAddMemberBinding = DialogAddMemberBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.setContentView(dialogBinding.root)
        dialogBinding.tvAdd.setOnClickListener {
            val email = dialogBinding.etEmailSearchMember.text.toString()

            if (email.isNotEmpty()){
                dialog.dismiss()

            }else{
                showErrorSnackBar(resources.getString(R.string.please_enter_member_email))
            }
        }
        dialogBinding.tvCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}