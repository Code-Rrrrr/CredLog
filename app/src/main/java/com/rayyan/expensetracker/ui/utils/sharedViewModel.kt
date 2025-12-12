package com.rayyan.expensetracker.ui.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class sharedViewModel : ViewModel() {
    private val _selectedChip = MutableLiveData<Int>()
    var selectedChip : LiveData<Int> = _selectedChip

    fun setSelectedChip(chipId : Int){
        _selectedChip.value = chipId
    }
}