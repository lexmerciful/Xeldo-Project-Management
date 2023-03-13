package com.lex.xeldoprojectmanagement.adapters

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.lex.xeldoprojectmanagement.databinding.ItemTaskBinding
import com.lex.xeldoprojectmanagement.models.Task

open class TaskListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Task>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewBinding = ItemTaskBinding.inflate(LayoutInflater.from(context),parent,false)
        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins((15.toDp().toPx()), 0, (40.toDp()).toPx(), 0)

        viewBinding.root.layoutParams = layoutParams
        return MyViewHolder(viewBinding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder){
            if (position == list.size - 1){
                holder.binding.tvAddTaskList.visibility = View.VISIBLE
                holder.binding.llTaskItem.visibility = View.GONE
            }else{
                holder.binding.tvAddTaskList.visibility = View.GONE
                holder.binding.llTaskItem.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private fun Int.toDp(): Int = (this/ Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this* Resources.getSystem().displayMetrics.density).toInt()

    class MyViewHolder(val binding: ItemTaskBinding): RecyclerView.ViewHolder(binding.root)
}