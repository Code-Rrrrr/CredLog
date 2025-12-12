package com.rayyan.expensetracker.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.rayyan.expensetracker.R
import com.rayyan.expensetracker.data.ExpenseEntry
import com.rayyan.expensetracker.repository.applicationRepo
import com.rayyan.expensetracker.ui.utils.myViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rayyan.expensetracker.databinding.FragmentBottomSheetAddUpdateBinding

class bottomSheetAddUpdateFragment : BottomSheetDialogFragment() {

    private var transactionToBeUpdated: ExpenseEntry? = null

    companion object {
        private const val ARG_TRANSACTION = "transaction"
        fun newInstance(transaction: ExpenseEntry? = null): bottomSheetAddUpdateFragment {
            val fragment = bottomSheetAddUpdateFragment()
            transaction?.let {
                val args = Bundle()
                args.putSerializable(ARG_TRANSACTION, it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionToBeUpdated = arguments?.getSerializable(ARG_TRANSACTION) as? ExpenseEntry
    }

    private lateinit var binding: FragmentBottomSheetAddUpdateBinding
    private lateinit var viewmodel: myViewModel
    private val categoryList = listOf(
        "Food", "Shopping", "Transport", "Entertainment", "Health", "Others"
    )

    private var selectedType = "expense" // default is set to expense

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetAddUpdateBinding.inflate(inflater, container, false)
        viewmodel = ViewModelProvider(requireActivity())[myViewModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupTypeButtons()
        populateFields()
        setupSaveButton()
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.layout_dropdown_item,
            categoryList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        binding.spinnerCategory.setAdapter(adapter)
    }

    private fun setupTypeButtons() {
        // Sets initial button states, to be the same as the current state
        updateTypeButton(isExpense = true)
        binding.rbExpense.setOnClickListener {
            selectedType = "expense"
            updateTypeButton(isExpense = true)
        }
        binding.rbIncome.setOnClickListener {
            selectedType = "income"
            updateTypeButton(isExpense = false)
        }
    }

    private fun updateTypeButton(isExpense: Boolean) {
        if (isExpense) {
            // Expense selected
            binding.rbExpense.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.expense))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                strokeWidth = 0
            }
            binding.rbIncome.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.income))
                strokeWidth = 4
            }
        } else {
            // Income selected
            binding.rbIncome.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.income))
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                strokeWidth = 0
            }
            binding.rbExpense.apply {
                setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                setTextColor(ContextCompat.getColor(requireContext(), R.color.expense))
                strokeWidth = 4
            }
        }
    }

    private fun populateFields() {
        transactionToBeUpdated?.let { existing ->
            binding.apply {

                val titleText = existing.title ?: ""
                etTitle.setText(titleText)

                val noteText = existing.note ?: ""
                etNote.setText(noteText)

                val amountText = existing.amount?.toString() ?: ""
                etAmount.setText(amountText)

                selectedType = existing.type
                updateTypeButton(isExpense = existing.type.equals("expense", ignoreCase = true))
            }
        }
    }


    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val note = binding.etNote.text.toString().trim()
            val amountText = binding.etAmount.text.toString().trim()

            // Validation
            if (title.isEmpty()) {
                binding.etTitle.error = "Title is required"
                binding.etTitle.requestFocus()
                return@setOnClickListener
            }

            if (amountText.isEmpty()) {
                binding.etAmount.error = "Amount is required"
                binding.etAmount.requestFocus()
                return@setOnClickListener
            }

            val amount = amountText.toDoubleOrNull()
            if (amount == null || amount <= 0.0) {
                binding.etAmount.error = "Enter valid amount"
                binding.etAmount.requestFocus()
                return@setOnClickListener
            }

            val category = binding.spinnerCategory.text.toString().lowercase()
            val date = transactionToBeUpdated?.date ?: System.currentTimeMillis()

            val newTransaction = ExpenseEntry(
                id = transactionToBeUpdated?.id ?: 0,
                title = title,
                amount = amount,
                category = category,
                type = selectedType,
                date = date,
                note = note.ifEmpty { null }
            )

            if (transactionToBeUpdated == null) {
                viewmodel.addExpense(newTransaction)
                Toast.makeText(context, "Transaction added successfully", Toast.LENGTH_SHORT).show()
            } else {
                viewmodel.updateExpense(newTransaction)
                Toast.makeText(context, "Transaction updated successfully", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }
}