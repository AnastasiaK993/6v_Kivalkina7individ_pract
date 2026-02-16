package com.bignerdranch.android.a7individdibi.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.android.a7individdibi.dao.SpecialtyDao
import com.bignerdranch.android.a7individdibi.dao.StudentDao
import com.bignerdranch.android.a7individdibi.dao.TeacherDao
import com.bignerdranch.android.a7individdibi.dao.UserDao
import com.bignerdranch.android.a7individdibi.data.Specialty
import com.bignerdranch.android.a7individdibi.data.Student
import com.bignerdranch.android.a7individdibi.data.User
import com.bignerdranch.android.a7individdibi.data.Teacher


@Database(
    entities = [Student::class, Specialty::class, Teacher::class, User::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun specialtyDao(): SpecialtyDao
    abstract fun teacherDao(): TeacherDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "college_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}