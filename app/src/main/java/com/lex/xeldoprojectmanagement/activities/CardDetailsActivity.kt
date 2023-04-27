package com.lex.xeldoprojectmanagement.activities

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.adapters.CardMemberListItemsAdapter
import com.lex.xeldoprojectmanagement.databinding.ActivityCardDetailsBinding
import com.lex.xeldoprojectmanagement.dialogs.LabelColorListDialog
import com.lex.xeldoprojectmanagement.dialogs.SelectMemberListDialog
import com.lex.xeldoprojectmanagement.firebase.FirestoreClass
import com.lex.xeldoprojectmanagement.models.*
import com.lex.xeldoprojectmanagement.utils.Constants

class CardDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var mBoardDetails: Board
    private var mTaskListItemPosition = -1
    private var mCardListItemPosition = -1
    private var mSelectedColor = ""
    private lateinit var mMembersDetailsList: ArrayList<Users>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentDetails()
        setupActionBar(binding.toolbarCardDetailsActivity, mBoardDetails
            .taskList[mTaskListItemPosition]
            .cards[mCardListItemPosition].name)

        binding.etNameCardDetails.setText(mBoardDetails
            .taskList[mTaskListItemPosition]
            .cards[mCardListItemPosition].name)

        // To set the edit text focus to the end
        binding.etNameCardDetails.setSelection(binding.etNameCardDetails.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListItemPosition].cards[mCardListItemPosition].labelColor

        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        binding.btnUpdateCardDetails.setOnClickListener {
            if (binding.etNameCardDetails.text.toString().isNotEmpty()){
                updateCardDetails()
            } else {
                showErrorSnackBar("Enter card name")
            }
        }

        binding.tvSelectLabelColor.setOnClickListener {
            showLabelColorListDialog()
        }

        binding.tvSelectMembers.setOnClickListener {
            cardMembersListDialog()
        }

        setupSelectedMembersList()
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun getIntentDetails() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION) && intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            mTaskListItemPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, 0)
            mCardListItemPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, 0)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMembersDetailsList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList: ArrayList<String> = ArrayList()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun setColor() {
        binding.tvSelectLabelColor.text = ""
        binding.tvSelectLabelColor.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    private fun showLabelColorListDialog() {
        val colorsList: ArrayList<String> = colorsList()

        val listDialog = object : LabelColorListDialog(
            this,
            colorsList,
            resources.getString(R.string.str_select_label_color),
            mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }

        }
        listDialog.show()
    }

    private fun cardMembersListDialog() {
        var cardAssignedMembersList = mBoardDetails
            .taskList[mTaskListItemPosition]
            .cards[mCardListItemPosition].assignedTo

        /**
         * We first check if theres any member assigned to the card, if there are we then check if
         * for every single member assigned to the board and every single member assigned to the card
         * and then check if the Board members list ID is the same with Card Members list and if true
         * make them selected in the card and if not, make them unselected in the card
         */
        if (cardAssignedMembersList.size > 0) {
            for (i in mMembersDetailsList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailsList[i].id == j) {
                        mMembersDetailsList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersDetailsList.indices) {
                mMembersDetailsList[i].selected = false
            }
        }

        val listDialog = object : SelectMemberListDialog(
            this,
            resources.getString(R.string.select_members),
            mMembersDetailsList
        ) {
            override fun onItemSelected(user: Users, action: String) {
                val mBoardDetailsAssignedTo = mBoardDetails.taskList[mTaskListItemPosition]
                    .cards[mCardListItemPosition].assignedTo
                /**
                 * if the user id passed to us is not in the BoardDeatails id already, then
                 * we add it and if it is, we remove it and set the memberDetails.selected to false
                 * since user would have been removed already and then call setupSelectedMembersList
                 * in order to refresh the Selected Members List
                 */
                if (action == Constants.SELECT) {
                    if (!mBoardDetailsAssignedTo.contains(user.id)) {
                        mBoardDetailsAssignedTo.add(user.id)
                    }
            } else {
                    mBoardDetailsAssignedTo.remove(user.id)

                    for (i in mMembersDetailsList.indices) {
                        if (mMembersDetailsList[i].id == user.id) {
                            mMembersDetailsList[i].selected = false
                        }
                    }
                }

                setupSelectedMembersList()
            }

        }
        listDialog.show()
    }

    private fun setupSelectedMembersList() {
        val cardAssignedMembersList = mBoardDetails
            .taskList[mTaskListItemPosition]
            .cards[mCardListItemPosition].assignedTo

        val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

        for (i in mMembersDetailsList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailsList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        mMembersDetailsList[i].id,
                        mMembersDetailsList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.size > 0) {
            selectedMembersList.add(SelectedMembers("",""))
            binding.tvSelectMembers.visibility = View.GONE
            binding.rvSelectedMembersList.visibility = View.VISIBLE

            val cardMemberListItemsAdapter = CardMemberListItemsAdapter(this, selectedMembersList, true)

            binding.rvSelectedMembersList.apply {
                layoutManager = GridLayoutManager(this@CardDetailsActivity, 6)
                adapter = cardMemberListItemsAdapter
            }

            cardMemberListItemsAdapter.SetOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                override fun onClick() {
                    cardMembersListDialog()
                }

            })
        } else {
            binding.tvSelectMembers.visibility = View.VISIBLE
            binding.rvSelectedMembersList.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(mBoardDetails
                    .taskList[mTaskListItemPosition]
                    .cards[mCardListItemPosition].name)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateCardDetails() {
        val card = Card(
            binding.etNameCardDetails.text.toString(),
            mBoardDetails.taskList[mTaskListItemPosition].cards[mCardListItemPosition].createdBy,
            mBoardDetails.taskList[mTaskListItemPosition].cards[mCardListItemPosition].assignedTo,
            mSelectedColor
        )

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)

        mBoardDetails.taskList[mTaskListItemPosition].cards[mCardListItemPosition] = card

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListItemPosition].cards

        cardsList.removeAt(mCardListItemPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        // To remove the fixed "ADD CARD"
        taskList.removeAt(taskList.size - 1)

        taskList[mTaskListItemPosition].cards = cardsList

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().addUpdateTaskList(this, mBoardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(resources.getString(R.string.confirmation_message_to_delete_card, cardName))
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}