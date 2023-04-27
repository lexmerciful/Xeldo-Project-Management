package com.lex.xeldoprojectmanagement.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ItemCardSelectedMembersBinding
import com.lex.xeldoprojectmanagement.models.SelectedMembers

class CardMemberListItemsAdapter(
    private val context: Context,
    private val selectedMembersList: ArrayList<SelectedMembers>,
    private val assignMembers: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MyViewHolder(val binding: ItemCardSelectedMembersBinding): RecyclerView.ViewHolder(binding.root)

    private var onClickListener: OnClickListener? = null

    fun SetOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemCardSelectedMembersBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return selectedMembersList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = selectedMembersList[position]

        if (holder is MyViewHolder) {
            if (position == selectedMembersList.size - 1 && assignMembers) {
                holder.binding.ivAddMember.visibility = View.VISIBLE
                holder.binding.ivSelectedMemberImage.visibility = View.GONE
            } else {
                holder.binding.ivAddMember.visibility = View.GONE
                holder.binding.ivSelectedMemberImage.visibility = View.VISIBLE

                Glide
                    .with(context)
                    .load(model.image)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.binding.ivSelectedMemberImage)
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick()
                }
            }
        }
    }
}