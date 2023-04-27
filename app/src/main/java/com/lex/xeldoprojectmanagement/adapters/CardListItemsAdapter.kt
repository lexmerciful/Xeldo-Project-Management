package com.lex.xeldoprojectmanagement.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lex.xeldoprojectmanagement.activities.TaskListActivity
import com.lex.xeldoprojectmanagement.databinding.ItemCardBinding
import com.lex.xeldoprojectmanagement.models.Card
import com.lex.xeldoprojectmanagement.models.SelectedMembers

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

            if (model.labelColor.isNotEmpty()) {
                holder.binding.viewLabelColor.visibility = View.VISIBLE
                holder.binding.viewLabelColor.setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                holder.binding.viewLabelColor.visibility = View.GONE
            }

            holder.binding.tvCardName.text = model.name

            if ((context as TaskListActivity).mAssignedMemberDetailList.size > 0) {
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                for (i in context.mAssignedMemberDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.mAssignedMemberDetailList[i].id == j) {
                            val selectedMember = SelectedMembers(
                                context.mAssignedMemberDetailList[i].id,
                                context.mAssignedMemberDetailList[i].image
                            )
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if (selectedMembersList.size > 0) {
                    /**
                     * If the creator is the only selected member in the list, hide the recycler view showing
                     * the members, this is to avoid having a big card all through the card list but if the
                     * selected member is more than one or if the only selected member is not the creator of the
                     * card, then show the selected member list recycler view
                     */
                    if (selectedMembersList.size == 1 && selectedMembersList[0].id == model.createdBy) {
                        holder.binding.rvCardSelectedMembersList.visibility = View.GONE
                    } else {
                        holder.binding.rvCardSelectedMembersList.visibility = View.VISIBLE

                        val cardMemberListItemsAdapter = CardMemberListItemsAdapter(
                            context, selectedMembersList, false
                        )
                        holder.binding.rvCardSelectedMembersList.apply {
                            layoutManager = GridLayoutManager(context, 4)
                            adapter = cardMemberListItemsAdapter
                        }

                        cardMemberListItemsAdapter.SetOnClickListener(object : CardMemberListItemsAdapter.OnClickListener{
                            override fun onClick() {
                                if (onClickListener != null){
                                    onClickListener!!.onClick(position)
                                }
                            }

                        })
                    }
                } else {
                    holder.binding.rvCardSelectedMembersList.visibility = View.GONE
                }
            }

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