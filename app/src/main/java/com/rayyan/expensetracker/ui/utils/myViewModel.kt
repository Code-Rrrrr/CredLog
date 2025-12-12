package com.rayyan.expensetracker.ui.utils

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rayyan.expensetracker.data.ExpenseEntry
import com.rayyan.expensetracker.data.ExpenseDatabase
import com.rayyan.expensetracker.repository.applicationRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class myViewModel(application: Application) : AndroidViewModel(application){
    var repository : applicationRepo
    private var _filteredExpenses = MutableLiveData<List<ExpenseEntry>>()
    val filteredExpenses : LiveData<List<ExpenseEntry>> get() = _filteredExpenses

    init {
        val dao = ExpenseDatabase.Companion.getDatabase(application).expenseDao()
        repository = applicationRepo(dao)
    }

    fun getAllExpenses() = repository.allExpenses()

    //income functions
    fun getTotalIncome() = repository.getTotalIncome()

    //expense functions
    fun getTotalExpense() = repository.getTotalExpense()

    //balance functions
    fun getTotalBalance() = repository.getTotalBalance()

    //filter
    fun filterByDate(startDate: Long, endDate: Long){
        val currentList : LiveData<List<ExpenseEntry>> = repository.allExpenses()
        val list = currentList.value ?: return
        _filteredExpenses.value = list.filter {
            it.date in startDate..endDate
        }
    }
    /**
     * Delete an expense from the database
     */
    fun deleteExpense(expense: ExpenseEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(expense)
            // Or if you have a delete by ID method:
            // repository.deleteById(expense.id)
        }
    }
    fun addExpense(expense: ExpenseEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertExpense(expense)
        }
    }
    fun updateExpense(expense: ExpenseEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExpense(expense)
        }
    }
}