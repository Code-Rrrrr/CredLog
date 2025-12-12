package com.rayyan.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ExpenseEntry::class], version = 1, exportSchema = true )
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao() : ExpenseDAO

    companion object {
        @Volatile
        private var INSTANCE : ExpenseDatabase? = null

        fun getDatabase(context : Context) : ExpenseDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase :: class.java,
                    "expense_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}