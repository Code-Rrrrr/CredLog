package com.rayyan.expensetracker.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDAO {

    @Insert
    suspend fun insertExpense(expense : ExpenseEntry)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateExpense(expense: ExpenseEntry)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntry)

    //List Queries
    @Query("SELECT * FROM expense_table order by date desc")
    fun getAllExpenses() : LiveData<List<ExpenseEntry>>

    //Income Queries
    @Query("SELECT SUM(amount) FROM expense_table WHERE type = 'income' ORDER BY date DESC")
    fun getTotalIncome() : LiveData<Double>

    //Expense Queries
    @Query("SELECT SUM(amount) FROM expense_table WHERE type = 'expense' ORDER BY date DESC")
    fun getTotalExpense() : LiveData<Double>

    //Balance Queries
    @Query("""
        SELECT (
        IFNULL(SUM(case when type = 'income' then amount end),0) -
        ifnull(sum(case when type = 'expense' then amount end), 0)
        )
        from expense_table
    """)
    fun getTotalBalance() : LiveData<Double>
}