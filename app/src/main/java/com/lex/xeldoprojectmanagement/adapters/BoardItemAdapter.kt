package com.lex.xeldoprojectmanagement.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lex.xeldoprojectmanagement.R
import com.lex.xeldoprojectmanagement.databinding.ItemBoardBinding
import com.lex.xeldoprojectmanagement.models.Board

open class BoardItemAdapter(private val context: Context,
private var list: ArrayList<Board>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.binding.ivBoardImage)

            holder.binding.tvName.text = model.name
            holder.binding.tvCreatedBy.text = "Created by: ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position: Int, model: Board)
    }

    private class MyViewHolder(val binding:ItemBoardBinding): RecyclerView.ViewHolder(binding.root)
}