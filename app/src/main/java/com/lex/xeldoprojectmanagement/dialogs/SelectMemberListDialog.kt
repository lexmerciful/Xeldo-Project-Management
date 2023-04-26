package com.lex.xeldoprojectmanagement.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.lex.xeldoprojectmanagement.adapters.MembersListItemsAdapter
import com.lex.xeldoprojectmanagement.databinding.DialogListBinding
import com.lex.xeldoprojectmanagement.models.Users

abstract class SelectMemberListDialog(
    context: Context,
    private var title: String = "",
    private var assignedMembersList: ArrayList<Users>
) : Dialog(context) {

    private var adapter: MembersListItemsAdapter? = null
    private lateinit var binding: DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogListBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)

        setupRecyclerView(binding.root)
    }

    private fun setupRecyclerView(view: View) {
        binding.tvTitle.text = title

        if (assignedMembersList.size > 0){
            adapter = MembersListItemsAdapter(context, assignedMembersList)
            binding.rvList.apply {
                adapter = adapter
                layoutManager = LinearLayoutManager(context)
            }

            adapter!!.onClickListener = object : MembersListItemsAdapter.OnClickListener{
                override fun onClick(position: Int, users: Users, action: String) {
                    dismiss()
                    onItemSelected(users, action)
                }

            }
        }

    }

    protected abstract fun onItemSelected(users: Users, action: String)

}