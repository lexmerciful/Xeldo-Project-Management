package com.lex.xeldoprojectmanagement.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ActivityMembersBinding
import com.lex.xeldoprojectmanagement.models.Board
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
    }
}