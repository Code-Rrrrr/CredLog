package com.rayyan.expensetracker.repository

import com.rayyan.expensetracker.data.ExpenseDAO
import com.rayyan.expensetracker.data.ExpenseEntry

class applicationRepo(private val expensedao: ExpenseDAO) {

    fun allExpenses() = expensedao.getAllExpenses()

    suspend fun insertExpense(expense : ExpenseEntry){
        expensedao.insertExpense(expense)
    }

    suspend fun updateExpense(expense : ExpenseEntry){
        expensedao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense : ExpenseEntry){
        expensedao.deleteExpense(expense)
    }

    //income functions
    fun getTotalIncome() = expensedao.getTotalIncome()

    //expense functions
    fun getTotalExpense() = expensedao.getTotalExpense()

    //balance functions
    fun getTotalBalance() = expensedao.getTotalBalance()
}