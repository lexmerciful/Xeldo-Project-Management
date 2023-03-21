package com.lex.xeldoprojectmanagement.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ItemMemberBinding
import com.lex.xeldoprojectmanagement.models.Users
import java.io.IOException

open class MembersListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Users>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemMemberBinding.inflate(
            LayoutInflater.from(context),
            parent,
            false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder){
            try {
                Glide
                    .with(context)
                    .load(model.image)
                    .circleCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(holder.binding.ivMemberImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            holder.binding.tvMemberName.text = model.name
            holder.binding.tvMemberEmail.text = model.email
        }
    }

    class MyViewHolder(val binding: ItemMemberBinding): RecyclerView.ViewHolder(binding.root)
}