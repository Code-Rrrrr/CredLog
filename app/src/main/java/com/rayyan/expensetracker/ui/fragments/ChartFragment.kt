package com.rayyan.expensetracker.ui.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.animation.Easing
import com.rayyan.expensetracker.R
import com.rayyan.expensetracker.data.ExpenseEntry
import com.rayyan.expensetracker.ui.utils.myViewModel
import com.rayyan.expensetracker.ui.utils.sharedViewModel
import java.util.*

class ChartFragment : Fragment() {

    private lateinit var chartViewModel: myViewModel
    private lateinit var sharedviewModel: sharedViewModel
    private lateinit var barChartexp: BarChart
    private lateinit var barChartinc: BarChart
    private lateinit var pieChartexp: PieChart
    private lateinit var pieChartinc: PieChart

    private var expenseList = listOf<ExpenseEntry>()
    private var filteredList = listOf<ExpenseEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chartViewModel = ViewModelProvider(this)[myViewModel::class.java]
        sharedviewModel = ViewModelProvider(requireActivity())[sharedViewModel::class.java]

        barChartexp = view.findViewById(R.id.barchartExpense)
        pieChartexp = view.findViewById(R.id.piechartExpense)

        //income charts
        barChartinc = view.findViewById(R.id.barchartincome)
        pieChartinc = view.findViewById(R.id.piechartIncome)

        // Observe expense data and update as the chips are changed or the entries are inserted or removed
        chartViewModel.getAllExpenses().observe(viewLifecycleOwner) { list ->
            expenseList = list
            updateChartsBasedOnFilter()
        }

        //animations on titles of the different card views
        val anim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        view.findViewById<TextView>(R.id.piechartHeading).startAnimation(anim)
        view.findViewById<TextView>(R.id.barchartHeading).startAnimation(anim)
        pieChartexp.startAnimation(anim)
        barChartexp.startAnimation(anim)
        pieChartinc.startAnimation(anim)
        barChartinc.startAnimation(anim)

        setUpChips()
    }

    private fun setUpChips() {
        sharedviewModel.selectedChip.observe(viewLifecycleOwner) { selected ->
            when (selected) {
                R.id.todayChip -> filterToday()
                R.id.thisWeekChip -> filterThisWeek()
                R.id.thisMonthChip -> filterThisMonth()
                else -> showAll()
            }
        }
    }

    private fun filterToday() {
        val today = Calendar.getInstance()
        filteredList = expenseList.filter { expense ->
            val calendarDate = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }
            calendarDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendarDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
        updateCharts(filteredList)
    }

    private fun filterThisWeek() {
        val now = Calendar.getInstance()
        val currentYear = now.get(Calendar.YEAR)
        val currentWeek = now.get(Calendar.WEEK_OF_YEAR)
        filteredList = expenseList.filter { expense ->
            val expenseDate = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }
            expenseDate.get(Calendar.YEAR) == currentYear &&
                    expenseDate.get(Calendar.WEEK_OF_YEAR) == currentWeek
        }
        updateCharts(filteredList)
    }

    private fun filterThisMonth() {
        val today = Calendar.getInstance()
        val currentMonth = today.get(Calendar.MONTH)
        val currentYear = today.get(Calendar.YEAR)

        filteredList = expenseList.filter { expense ->
            val calendarDate = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }
            calendarDate.get(Calendar.MONTH) == currentMonth &&
                    calendarDate.get(Calendar.YEAR) == currentYear
        }
        updateCharts(filteredList)
    }

    private fun showAll() {
        filteredList = expenseList
        updateCharts(filteredList)
    }

    private fun updateChartsBasedOnFilter() {
        // Apply current filter when data changes
        val selectedChip = sharedviewModel.selectedChip.value
        when (selectedChip) {
            R.id.todayChip -> filterToday()
            R.id.thisWeekChip -> filterThisWeek()
            R.id.thisMonthChip -> filterThisMonth()
            else -> showAll()
        }
    }

    private fun updateCharts(expenses: List<ExpenseEntry>) {
        val expenseData = expenses.filter { it.type == "expense" }
        val incomeData = expenses.filter { it.type == "income" }

        if (expenseData.isEmpty()) {
            barChartexp.visibility = View.GONE
            pieChartexp.visibility = View.GONE
        } else {
            barChartexp.visibility = View.VISIBLE
            pieChartexp.visibility = View.VISIBLE
            setUpBarChart(expenses)
            setUpPieChart(expenses)
        }

        if (incomeData.isEmpty()) {
            barChartinc.visibility = View.GONE
            pieChartinc.visibility = View.GONE
        } else {
            barChartinc.visibility = View.VISIBLE
            pieChartinc.visibility = View.VISIBLE
            setupBarChartIncome(expenses)
            setUpPieChartIncome(expenses)
        }
    }

    private fun setUpPieChart(expenses: List<ExpenseEntry>) {
        if (expenses.isEmpty()) return

        val grouped = expenses.filter { it.type == "expense" }.groupBy { it.category }
        val colorList = listOf(
            Color.parseColor("#8E24AA"),
            Color.parseColor("#BA68C8"),
            Color.parseColor("#EC407A"),
            Color.parseColor("#F48FB1"),
            Color.parseColor("#CE93D8")
        )
        val entries = grouped.entries.map { (category, list) ->
            val sum = list.sumOf { it.amount }.toFloat()
            PieEntry(sum, category)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colorList
        dataSet.sliceSpace = 2f
        dataSet.selectionShift = 6f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChartexp))
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(12f)

        pieChartexp.apply {
            this.data = data
            setUsePercentValues(true)
            description.isEnabled = false
            isRotationEnabled = true
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 48f
            transparentCircleRadius = 51f

            setDrawEntryLabels(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(11f)

            legend.isEnabled = true
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.textSize = 12f
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.textColorDayNight)
            legend.form = Legend.LegendForm.CIRCLE

            //centre pie hole
            pieChartexp.setDrawCenterText(true)
            pieChartexp.centerText = "Expenses by Category"
            pieChartexp.setCenterTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.textColorDayNight
                )
            )
            pieChartexp.setCenterTextSize(14f)

            animateY(1200, Easing.EaseInOutQuad)
        }

        pieChartexp.invalidate()
    }

    private fun setUpBarChart(expenses: List<ExpenseEntry>) {
        if (expenses.isEmpty()) return

        val grouped = expenses.filter { it.type == "expense" }.groupBy { it.category }
        val colorList = listOf(
            Color.parseColor("#8E24AA"),
            Color.parseColor("#BA68C8"),
            Color.parseColor("#EC407A"),
            Color.parseColor("#F48FB1"),
            Color.parseColor("#CE93D8")
        )
        val entries = grouped.entries.mapIndexed { index, entry ->
            val sum = entry.value.sumOf { it.amount }.toFloat()
            BarEntry(index.toFloat(), sum)
        }
        val dataSet = BarDataSet(entries, "Expenses by Category")
        dataSet.apply {
            colors = colorList
            valueTextSize = 12f
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.textColorDayNight)
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.6f
        barChartexp.data = barData

        val xAxis = barChartexp.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(grouped.keys.toList())
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.labelRotationAngle = -30f
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.textColorDayNight)
        xAxis.textSize = 10f

        barChartexp.axisRight.isEnabled = false
        barChartexp.axisLeft.axisMinimum = 0f
        barChartexp.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.textColorDayNight)

        barChartexp.description.text = ""
        barChartexp.legend.isEnabled = true
        barChartexp.legend.textColor =
            ContextCompat.getColor(requireContext(), R.color.textColorDayNight)
        barChartexp.animateY(1000)
        barChartexp.invalidate()
        barChartexp.setExtraOffsets(0f, 0f, 0f, 30f)
        barChartexp.setFitBars(true)
    }

    private fun setupBarChartIncome(expenses: List<ExpenseEntry>) {

            val grouped = expenses.filter { it.type == "income" }.groupBy { it.category }

            val barChartexp = barChartinc
            val colorList = listOf(
                Color.parseColor("#8E24AA"),
                Color.parseColor("#BA68C8"),
                Color.parseColor("#EC407A"),
                Color.parseColor("#F48FB1"),
                Color.parseColor("#CE93D8")
            )

            val entries = grouped.entries.mapIndexed { index, entry ->
                val sum = entry.value.sumOf { it.amount }.toFloat()
                BarEntry(index.toFloat(), sum)
            }

            val dataSet = BarDataSet(entries, "Expenses by Category")
            dataSet.apply {
                colors = colorList
                valueTextSize = 12f
                valueTextColor = ContextCompat.getColor(requireContext(), R.color.textColorDayNight)
            }

            val barData = BarData(dataSet)
            barData.barWidth = 0.6f
            barChartexp.data = barData

            val xAxis = barChartexp.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(grouped.keys.toList())
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.labelRotationAngle = -30f
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.textColorDayNight)
            xAxis.textSize = 10f

            barChartexp.axisRight.isEnabled = false
            barChartexp.axisLeft.axisMinimum = 0f
            barChartexp.axisLeft.textColor =
                ContextCompat.getColor(requireContext(), R.color.textColorDayNight)

            barChartexp.description.text = ""
            barChartexp.legend.isEnabled = true
            barChartexp.legend.textColor =
                ContextCompat.getColor(requireContext(), R.color.textColorDayNight)
            barChartexp.animateY(1000)
            barChartexp.invalidate()
            barChartexp.setExtraOffsets(0f, 0f, 0f, 30f)
            barChartexp.setFitBars(true)


        }


    private fun setUpPieChartIncome(expenses: List<ExpenseEntry>) {

        val pieChartexp = pieChartinc
        if (expenses.isEmpty()) return

        val grouped = expenses.filter { it.type == "income" }.groupBy { it.category }

        val colorList = listOf(
            Color.parseColor("#8E24AA"),
            Color.parseColor("#BA68C8"),
            Color.parseColor("#EC407A"),
            Color.parseColor("#F48FB1"),
            Color.parseColor("#CE93D8")
        )

        val entries = grouped.entries.map { (category, list) ->
            val sum = list.sumOf { it.amount }.toFloat()
            PieEntry(sum, category)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colorList
        dataSet.sliceSpace = 2f
        dataSet.selectionShift = 6f
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f

        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChartexp))
        data.setValueTextColor(Color.WHITE)
        data.setValueTextSize(12f)

        pieChartexp.apply {
            this.data = data

            setUsePercentValues(true)
            description.isEnabled = false
            isRotationEnabled = true
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 48f
            transparentCircleRadius = 51f

            setDrawEntryLabels(true)
            setEntryLabelColor(Color.WHITE)
            setEntryLabelTextSize(11f)

            legend.isEnabled = true
            legend.orientation = Legend.LegendOrientation.HORIZONTAL
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            legend.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            legend.textSize = 12f
            legend.textColor =
                ContextCompat.getColor(requireContext(), R.color.textColorDayNight)
            legend.form = Legend.LegendForm.CIRCLE

            //centre pie hole
            pieChartexp.setDrawCenterText(true)
            pieChartexp.centerText = "Income by Category"
            pieChartexp.setCenterTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.textColorDayNight
                )
            )
            pieChartexp.setCenterTextSize(14f)
            animateY(1200, Easing.EaseInOutQuad)
        }
        pieChartexp.invalidate()
    }
}