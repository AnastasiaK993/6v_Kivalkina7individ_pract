package com.bignerdranch.android.a7individdibi.dao

import androidx.room.*
import com.bignerdranch.android.a7individdibi.data.Teacher
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<Teacher>>


    @Query("SELECT * FROM teachers")
    suspend fun getAllTeachersDirect(): List<Teacher>

    @Insert
    suspend fun insertTeacher(teacher: Teacher)

    @Update
    suspend fun updateTeacher(teacher: Teacher)

    @Delete
    suspend fun deleteTeacher(teacher: Teacher)

}