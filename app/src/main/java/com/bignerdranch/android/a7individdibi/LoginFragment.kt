package com.bignerdranch.android.a7individdibi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.a7individdibi.database.AppDatabase
import com.bignerdranch.android.a7individdibi.databinding.FragmentLoginBinding
import com.bignerdranch.android.a7individdibi.data.User
import com.bignerdranch.android.a7individdibi.data.Teacher
import com.bignerdranch.android.a7individdibi.data.Specialty
import com.bignerdranch.android.a7individdibi.data.Student
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())

        lifecycleScope.launch {
            createBaseDataIfNeeded(db)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val selectedRole = when (binding.radioGroup.checkedRadioButtonId) {
                R.id.radioAdmissions -> "admissions"
                R.id.radioTeacher -> "teacher"
                R.id.radioStudent -> "student"
                else -> ""
            }


            if (email.isEmpty() || password.isEmpty() || selectedRole.isEmpty()) {  // Проверка на пустоту
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            if (password.length < 3) {// Проверка длины пароля
                binding.etPassword.error = "Пароль не менее 3 символов"
                return@setOnClickListener
            }

            lifecycleScope.launch {// Ищем в базе

                val existingUser = db.userDao().getUserByEmail(email)

                if (existingUser != null) {

                    if (existingUser.password == password && existingUser.role == selectedRole) {

                        loginUser(existingUser.email, existingUser.role)
                    } else {

                        Toast.makeText(requireContext(),
                            "Неверный пароль или роль для данного email",
                            Toast.LENGTH_LONG).show()
                    }
                } else {

                    val newUser = User(
                        email = email,
                        password = password,
                        role = selectedRole
                    )
                    db.userDao().insertUser(newUser)

                    Toast.makeText(requireContext(),
                        "Новый аккаунт создан",
                        Toast.LENGTH_LONG).show()

                    loginUser(email, selectedRole)
                }
            }
        }
    }

    private fun loginUser(email: String, role: String) {

        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("is_logged_in", true)
            putString("user_role", role)
            putString("user_email", email)
            apply()
        }

        Toast.makeText(requireContext(), "Здравствуйте, $role", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
    }

    private suspend fun createBaseDataIfNeeded(db: AppDatabase) {

        val specialties = db.specialtyDao().getAllSpecialtiesDirect()
        if (specialties.isEmpty()) {
            val spec1 = Specialty(name = "Программирование", maxHours = 1440)
            val spec2 = Specialty(name = "Дизайн", maxHours = 1440)
            val spec3 = Specialty(name = "Строители", maxHours = 1440)
            db.specialtyDao().insertSpecialty(spec1)
            db.specialtyDao().insertSpecialty(spec2)
            db.specialtyDao().insertSpecialty(spec3)
        }

        val teachers = db.teacherDao().getAllTeachersDirect()
        if (teachers.isEmpty()) {//если пусто
            val teacher1 = Teacher(
                fullName = "Игорь Всеволод Маринин",
                totalHoursPerYear = 1200,
                specialtyIds = listOf(1, 2)
            )
            val teacher2 = Teacher(
                fullName = "Шишкин Енисей Александрович",
                totalHoursPerYear = 1600,
                specialtyIds = listOf(3)
            )
            db.teacherDao().insertTeacher(teacher1)
            db.teacherDao().insertTeacher(teacher2)
        }

        val students = db.studentDao().getAllStudentsDirect()
        if (students.isEmpty()) {//если нет студентов совсем
            val student1 = Student(
                fullName = "Иванов Иван Иванович",
                groupName = "Пр-11",
                course = 2,
                specialtyId = 1,
                photoUri = "student1",
                birthDate = "15.05.2005",
                isBudget = true
            )
            db.studentDao().insertStudent(student1)

        }

        val users = db.userDao().getAllUsers()
        if (users.isEmpty()) {
            val testUser = User(
                email = "test@test.com",
                password = "123456",
                role = "admissions"
            )
            db.userDao().insertUser(testUser)

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}