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

    private var membersListItemsAdapter: MembersListItemsAdapter? = null
    lateinit var binding: DialogListBinding

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
            membersListItemsAdapter = MembersListItemsAdapter(context, assignedMembersList)
            binding.rvList.apply {
                adapter = membersListItemsAdapter
                layoutManager = LinearLayoutManager(context)
            }

            membersListItemsAdapter!!.onClickListener = object : MembersListItemsAdapter.OnClickListener{
                override fun onClick(position: Int, user: Users, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }

            }
        }

    }

    protected abstract fun onItemSelected(user: Users, action: String)

}