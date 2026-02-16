package com.bignerdranch.android.a7individdibi

import android.app.DatePickerDialog
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
import com.bignerdranch.android.a7individdibi.databinding.FragmentAddEditStudentBinding
import com.bignerdranch.android.a7individdibi.data.Student
import kotlinx.coroutines.launch
import java.util.Calendar

class AddEditStudentFragment : Fragment() {

    private var _binding: FragmentAddEditStudentBinding? = null
    private val binding get() = _binding!!
    private var studentId: Int = 0
    private var mode: String = ""
    private var userRole: String = ""
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studentId = arguments?.getInt("student_id", 0) ?: 0
        mode = arguments?.getString("mode", "") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditStudentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("user_role", "") ?: ""
        userEmail = sharedPref.getString("user_email", "") ?: ""

        val db = AppDatabase.getInstance(requireContext())

        setupTitle()


        if (mode == "create_for_student") {
            lifecycleScope.launch {
                try {
                    val allStudents = db.studentDao().getAllStudentsDirect()
                    val studentEmailPart = userEmail.substringBefore("@").lowercase()
                    val existingStudent = allStudents.find { student ->
                        student.fullName.lowercase().contains(studentEmailPart)
                    }

                    if (existingStudent != null) {

                        Toast.makeText(requireContext(),
                            "У вас уже есть анкета! Нельзя создать вторую.",
                            Toast.LENGTH_LONG).show()
                        findNavController().popBackStack()
                        return@launch
                    }
                } catch (e: Exception) {

                }
            }
        }


        if (studentId > 0) {
            loadStudentData(db)
        } else if (mode == "create_for_student") {

            binding.etFullName.setText("Студент " + userEmail.substringBefore("@"))
        }

        binding.etBirthDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveStudent(db)
        }
    }

    private fun setupTitle() {
        when {
            mode == "create_for_student" -> {
                binding.tvTitle.text = "Создание моей анкеты"
            }
            mode == "edit_for_student" -> {
                binding.tvTitle.text = "Редактирование моей анкеты"
            }
            studentId > 0 -> {
                binding.tvTitle.text = "Редактирование студента"
            }
            else -> {
                binding.tvTitle.text = "Добавление студента"
            }
        }
    }

    private fun loadStudentData(db: AppDatabase) {
        lifecycleScope.launch {
            try {
                val student = db.studentDao().getStudentByIdDirect(studentId)
                student?.let {
                    binding.etFullName.setText(it.fullName)
                    binding.etGroup.setText(it.groupName)
                    binding.etCourse.setText(it.course.toString())
                    binding.etSpecialtyId.setText(it.specialtyId.toString())
                    binding.etBirthDate.setText(it.birthDate)
                    binding.etPhotoUri.setText(it.photoUri)
                    binding.cbBudget.isChecked = it.isBudget
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки данных", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val date = "$dayOfMonth.${month + 1}.$year"
                binding.etBirthDate.setText(date)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun saveStudent(db: AppDatabase) {
        val fullName = binding.etFullName.text.toString()
        val group = binding.etGroup.text.toString()
        val course = binding.etCourse.text.toString().toIntOrNull() ?: 0
        val specialtyId = binding.etSpecialtyId.text.toString().toIntOrNull() ?: 0
        val birthDate = binding.etBirthDate.text.toString()
        val photoUri = binding.etPhotoUri.text.toString()
        val isBudget = binding.cbBudget.isChecked

        if (fullName.isEmpty() || group.isEmpty() || course == 0 || specialtyId == 0 || birthDate.isEmpty() || photoUri.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                if (studentId > 0) {

                    val student = Student(
                        id = studentId,
                        fullName = fullName,
                        groupName = group,
                        course = course,
                        specialtyId = specialtyId,
                        photoUri = photoUri,
                        birthDate = birthDate,
                        isBudget = isBudget
                    )
                    db.studentDao().updateStudent(student)

                    val message = if (mode == "edit_for_student") "Ваша анкета обновлена" else "Студент обновлен"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                } else {

                    val student = Student(
                        fullName = fullName,
                        groupName = group,
                        course = course,
                        specialtyId = specialtyId,
                        photoUri = photoUri,
                        birthDate = birthDate,
                        isBudget = isBudget
                    )
                    db.studentDao().insertStudent(student)

                    val message = if (mode == "create_for_student") "Ваша анкета создана!" else "Студент добавлен"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }


                findNavController().popBackStack()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка сохранения: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}