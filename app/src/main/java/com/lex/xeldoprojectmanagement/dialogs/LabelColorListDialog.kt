package com.lex.xeldoprojectmanagement.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.adapters.LabelColorListItemsAdapter
import com.lex.xeldoprojectmanagement.databinding.DialogListBinding

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private var title: String = "",
    private var mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListItemsAdapter? = null
    lateinit var binding: DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)
        //val view = LayoutInflater.from(context).inflate(R.layout.dialog_color_list, null)

        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        setupRecyclerView(binding.root)
    }

    private fun setupRecyclerView(view: View) {
        binding.tvTitle.text = title
        adapter = LabelColorListItemsAdapter(context,list, mSelectedColor)
        binding.rvList.layoutManager = LinearLayoutManager(context)
        binding.rvList.adapter = adapter

        adapter!!.onItemClickListener = object : LabelColorListItemsAdapter.OnItemClickListener{
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color)
            }

        }
    }

    protected abstract fun onItemSelected(color: String)
}