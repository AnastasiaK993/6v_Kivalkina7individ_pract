package com.bignerdranch.android.a7individdibi.dao

import androidx.room.*
import com.bignerdranch.android.a7individdibi.data.Student
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY fullName")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE groupName = :groupName")
    fun getStudentsByGroup(groupName: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE fullName LIKE '%' || :searchQuery || '%' OR groupName LIKE '%' || :searchQuery || '%'")
    fun searchStudents(searchQuery: String): Flow<List<Student>>

    @Insert
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("SELECT * FROM students WHERE fullName LIKE '%' || :searchQuery || '%' OR groupName LIKE '%' || :searchQuery || '%'")
    suspend fun searchStudentsDirect(searchQuery: String): List<Student>
    @Query("SELECT * FROM students")
    suspend fun getAllStudentsDirect(): List<Student>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentByIdDirect(id: Int): Student?

}