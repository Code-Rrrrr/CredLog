package com.rayyan.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
// ========== EDIT START: Add Serializable import ==========
import java.io.Serializable

@Entity(tableName = "expense_table")
data class ExpenseEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val type: String, // "income" or "expense"
    val date: Long,
    val note: String? = null
) : Serializable
