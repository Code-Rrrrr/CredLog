package com.rayyan.expensetracker.ui.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rayyan.expensetracker.R
import com.rayyan.expensetracker.data.ExpenseEntry
import com.rayyan.expensetracker.databinding.RecyclerItemBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var transactions: List<ExpenseEntry>,
    private val onItemClick: (ExpenseEntry) -> Unit,
    private val onItemDelete: (ExpenseEntry) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RecyclerItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = transactions[position]

        holder.binding.apply {
            title.text = item.title
            note.text = item.note ?: ""

            val millis = if (item.date < 1_000_000_000_000L) item.date * 1000L else item.date
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            date.text = dateFormat.format(Date(millis))

            amount.text = "₹"+item.amount.toString()
            amount.setTextColor(
                if (item.type.equals("income", ignoreCase = true))
                    holder.itemView.context.getColor(R.color.income)
                else
                    holder.itemView.context.getColor(R.color.expense)
            )

            val categoryIcons = mapOf(
                "food" to R.drawable.eat,
                "shopping" to R.drawable.shopping_bag,
                "transport" to R.drawable.transport,
                "entertainment" to R.drawable.ticket,
                "health" to R.drawable.health
            )
            categoryImage.setImageResource(categoryIcons[item.category] ?: R.drawable.default_ico)

            iconContainer.setCardBackgroundColor(
                if (item.type.equals("income", ignoreCase = true))
                    holder.itemView.context.getColor(R.color.income)
                else
                    holder.itemView.context.getColor(R.color.expense)
            )

            root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    override fun getItemCount() = transactions.size

    inner class ViewHolder(val binding: RecyclerItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun updateList(newList: List<ExpenseEntry>) {
        transactions = newList
        notifyDataSetChanged()
    }

    fun getItemAt(position: Int): ExpenseEntry {
        return transactions[position]
    }

    fun deleteItem(position: Int) {
        val item = transactions[position]
        onItemDelete(item)
    }
}