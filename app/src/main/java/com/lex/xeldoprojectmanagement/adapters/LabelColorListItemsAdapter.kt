package com.lex.xeldoprojectmanagement.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lex.xeldoprojectmanagement.databinding.ItemLabelColorBinding

class LabelColorListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<String>,
    private val mSelectedColor: String
    ) : RecyclerView.Adapter<LabelColorListItemsAdapter.ViewHolder>() {

    var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }

    class ViewHolder(val binding: ItemLabelColorBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(ItemLabelColorBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.viewMain.setBackgroundColor(Color.parseColor(item))

        if (item == mSelectedColor) {
            holder.binding.ivSelectedColor.visibility = View.VISIBLE
        } else {
            holder.binding.ivSelectedColor.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener!!.onClick(position, item)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}