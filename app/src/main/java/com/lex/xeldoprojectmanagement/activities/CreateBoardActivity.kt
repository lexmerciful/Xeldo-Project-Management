package com.lex.xeldoprojectmanagement.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ActivityCreateBoardBinding

class CreateBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupActionBar()
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
}