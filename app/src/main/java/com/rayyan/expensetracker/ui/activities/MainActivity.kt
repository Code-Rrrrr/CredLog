package com.rayyan.expensetracker.ui.activities

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.rayyan.expensetracker.data.ExpenseDatabase
import com.rayyan.expensetracker.repository.applicationRepo
import com.rayyan.expensetracker.ui.fragments.ChartFragment
import com.rayyan.expensetracker.ui.fragments.ListingsFragment
import com.rayyan.expensetracker.ui.fragments.PreferencesFragment
import com.rayyan.expensetracker.ui.fragments.bottomSheetAddUpdateFragment
import com.rayyan.expensetracker.ui.utils.myViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.rayyan.expensetracker.R
import com.rayyan.expensetracker.ui.utils.sharedViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModelMain: myViewModel
    private lateinit var sharedvieModel : sharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applyNightMode()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialising View Model
        val dao = ExpenseDatabase.getDatabase(application).expenseDao()
        val repo = applicationRepo(dao)

        viewModelMain = ViewModelProvider(this)[myViewModel::class.java]
        sharedvieModel = ViewModelProvider(this)[sharedViewModel::class.java]

        loadFragment(ListingsFragment())

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.listings -> loadFragment(ListingsFragment())
                R.id.charts -> loadFragment(ChartFragment())
                R.id.preferences -> loadFragment(PreferencesFragment())
                R.id.addItem -> bottomSheetAddUpdateFragment().show(supportFragmentManager,"Bottom Sheet")
            }
            true
        }

        // Initialise cards at top
        val balanceCard = findViewById<TextView>(R.id.balance_textView)
        val incomeCard = findViewById<TextView>(R.id.income_textView)
        val expenseCard = findViewById<TextView>(R.id.expense_textView)

        viewModelMain.getTotalBalance().observe(this) { balance ->
            balanceCard.text = "₹${balance ?: 0.0}"
        }

        viewModelMain.getTotalIncome().observe(this) { income ->
            incomeCard.text = "₹${income ?: 0.0}"
        }

        viewModelMain.getTotalExpense().observe(this) { expense ->
            expenseCard.text = "₹${expense ?: 0.0}"
        }

        //passing the clicked chip's id via the shared viewmodel
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup_horizontal)
        chipGroup.children.forEach { view ->
            if (view is Chip) {
                view.setOnClickListener {
                    sharedvieModel.setSelectedChip(view.id)
                }
            }
        }

        //setting default to be all
        val defaultChip = findViewById<Chip>(R.id.allChip)
        defaultChip.isChecked = true
        sharedvieModel.setSelectedChip(defaultChip.id)
    }

    private fun applyNightMode() {
        val sharedPreferences = getSharedPreferences(
            "ExpenseTrackerPrefs",
            Context.MODE_PRIVATE
        )
        val isNightMode = sharedPreferences.getBoolean(
            "night_mode",
            false
        )

        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


    private fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.framelayout, fragment)
            .commit()
    }
}

