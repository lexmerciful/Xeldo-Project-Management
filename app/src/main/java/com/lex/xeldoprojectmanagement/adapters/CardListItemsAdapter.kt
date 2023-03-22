package com.lex.xeldoprojectmanagement.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lex.xeldoprojectmanagement.databinding.ItemCardBinding
import com.lex.xeldoprojectmanagement.models.Board
import com.lex.xeldoprojectmanagement.models.Card

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            ItemCardBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            holder.binding.tvCardName.text = model.name

            holder.itemView.setOnClickListener {
                if (onClickListener != null){
                    onClickListener!!.onClick(position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener{
        fun onClick(position: Int)
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    class MyViewHolder(val binding: ItemCardBinding): RecyclerView.ViewHolder(binding.root)
}