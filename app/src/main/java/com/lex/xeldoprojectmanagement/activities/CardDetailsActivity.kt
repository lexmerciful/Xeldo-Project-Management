package com.lex.xeldoprojectmanagement.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ActivityCardDetailsBinding
import com.lex.xeldoprojectmanagement.models.Board
import com.lex.xeldoprojectmanagement.utils.Constants

class CardDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityCardDetailsBinding
    private lateinit var mBoardDetails: Board
    private var taskListItemPosition = -1
    private var cardListItemPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getIntentDetails()
        setupActionBar(binding.toolbarCardDetailsActivity, mBoardDetails
            .taskList[taskListItemPosition]
            .cards[cardListItemPosition].name)
    }

    private fun getIntentDetails() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION) && intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)){
            taskListItemPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, 0)
            cardListItemPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, 0)
        }
    }
}