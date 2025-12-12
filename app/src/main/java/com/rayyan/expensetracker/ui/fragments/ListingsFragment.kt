package com.rayyan.expensetracker.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.chip.ChipGroup
import com.rayyan.expensetracker.R
import com.rayyan.expensetracker.data.ExpenseEntry
import com.rayyan.expensetracker.ui.utils.TransactionAdapter
import com.rayyan.expensetracker.ui.utils.SwipeToDeleteCallback
import com.rayyan.expensetracker.ui.utils.myViewModel
import com.rayyan.expensetracker.ui.utils.sharedViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.exp


class ListingsFragment : Fragment() {

    private lateinit var adapter: TransactionAdapter
    private lateinit var listingsViewModel: myViewModel
    private lateinit var sharedviewModel : sharedViewModel
    private var expenseList = listOf<ExpenseEntry>()
    private var filteredList = listOf<ExpenseEntry>()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_listings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listingsViewModel = ViewModelProvider(this)[myViewModel::class.java]
        sharedviewModel = ViewModelProvider(requireActivity())[sharedViewModel::class.java]

        adapter = TransactionAdapter(
            transactions = expenseList,
            onItemClick = { expense ->
                openUpdateDialog(expense)
            },
            onItemDelete = { expense ->
                deleteExpenseWithConfirmation(expense)
            }
        )

        recyclerView = view.findViewById<RecyclerView>(R.id.main_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupSwipeToDelete()
        setUpChips()

        listingsViewModel.getAllExpenses().observe(viewLifecycleOwner) { list ->
            expenseList = list
            adapter.updateList(expenseList)
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = SwipeToDeleteCallback(adapter, requireContext())
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun openUpdateDialog(expense: ExpenseEntry) {
        val bottomSheet = bottomSheetAddUpdateFragment.newInstance(expense)
        bottomSheet.show(childFragmentManager, "UpdateExpense")
    }

    private fun deleteExpenseWithConfirmation(expense: ExpenseEntry) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete '${expense.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                listingsViewModel.deleteExpense(expense)

                Snackbar.make(
                    recyclerView,
                    "Transaction deleted",
                    Snackbar.LENGTH_LONG
                ).setAction("UNDO") {
                    listingsViewModel.addExpense(expense)
                }.show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                adapter.notifyDataSetChanged()
            }
            .setOnCancelListener {
                adapter.notifyDataSetChanged()
            }
            .show()
    }

    private fun setUpChips() {
        sharedviewModel.selectedChip.observe(viewLifecycleOwner){ selected ->
            when(selected){
                R.id.todayChip -> filterToday()
                R.id.thisWeekChip -> filterThisWeek()
                R.id.thisMonthChip -> filterThisMonth()
                else -> showAll()
            }
        }
    }

    private fun filterToday() {
        val today = Calendar.getInstance()

        val filteredList = expenseList.filter{expense ->
            val calendarDate = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }

            calendarDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendarDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
        adapter.updateList(filteredList)
    }

    private fun filterThisWeek(){
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentWeek = now.get(Calendar.WEEK_OF_YEAR)

        val filteredList = expenseList.filter{expense ->
            val expenseDate = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }

            expenseDate.get(Calendar.YEAR) == currentYear &&
                    expenseDate.get(Calendar.WEEK_OF_YEAR) == currentWeek
        }

        adapter.updateList(filteredList)
    }

    private fun filterThisMonth() {
        val today = Calendar.getInstance()
        val currentMonth = today.get(Calendar.MONTH)
        val currentYear = today.get(Calendar.YEAR)

        val filteredList = expenseList.filter { expense ->
            val calendarDate = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }

            calendarDate.get(Calendar.MONTH) == currentMonth &&
                    calendarDate.get(Calendar.YEAR) == currentYear
        }

        adapter.updateList(filteredList)
    }

    private fun showAll() {
        adapter.updateList(expenseList)
    }
}