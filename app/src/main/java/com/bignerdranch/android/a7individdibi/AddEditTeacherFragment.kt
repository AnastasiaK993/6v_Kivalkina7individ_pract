package com.bignerdranch.android.a7individdibi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bignerdranch.android.a7individdibi.database.AppDatabase
import com.bignerdranch.android.a7individdibi.databinding.FragmentAddEditTeacherBinding
import com.bignerdranch.android.a7individdibi.data.Teacher
import kotlinx.coroutines.launch

class AddEditTeacherFragment : Fragment() {

    private var _binding: FragmentAddEditTeacherBinding? = null
    private val binding get() = _binding!!
    private var teacherId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        teacherId = arguments?.getInt("teacher_id", 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTeacherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = AppDatabase.getInstance(requireContext())

        if (teacherId > 0) {
            binding.tvTitle.text = "Редактирование преподавателя"
            lifecycleScope.launch {
                db.teacherDao().getAllTeachers().collect { teachers ->
                    val teacher = teachers.find { it.id == teacherId }
                    teacher?.let {
                        binding.etFullName.setText(it.fullName)
                        binding.etHours.setText(it.totalHoursPerYear.toString())
                        binding.etSpecialtyIds.setText(it.specialtyIds.joinToString())
                    }
                }
            }
        }


        binding.btnSave.setOnClickListener {
            val fullName = binding.etFullName.text.toString()
            val hoursStr = binding.etHours.text.toString()
            val specialtyIdsStr = binding.etSpecialtyIds.text.toString()

            if (fullName.isEmpty() || hoursStr.isEmpty() || specialtyIdsStr.isEmpty()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hours = hoursStr.toIntOrNull()
            if (hours == null) {
                Toast.makeText(requireContext(), "Введите корректное число часов", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Преобразуем строку "1,2,3" в список чисел
            val specialtyIds = try {
                specialtyIdsStr.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .map { it.toInt() }
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Введите корректные id специальностей", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (specialtyIds.isEmpty()) {
                Toast.makeText(requireContext(), "Введите хотя бы один id специальности", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Проверка на превышение нагрузки (более 1440 часов)
            if (hours > 1440) {
                Toast.makeText(requireContext(),
                    "Нагрузка более 1440 часов => Положена премия!",
                    Toast.LENGTH_LONG).show()
            }

            lifecycleScope.launch {
                val teacher = Teacher(
                    id = teacherId,
                    fullName = fullName,
                    totalHoursPerYear = hours,
                    specialtyIds = specialtyIds
                )

                if (teacherId > 0) {
                    db.teacherDao().updateTeacher(teacher)
                    Toast.makeText(requireContext(), "Преподаватель обновлен", Toast.LENGTH_SHORT).show()
                } else {
                    db.teacherDao().insertTeacher(teacher)
                    Toast.makeText(requireContext(), "Преподаватель добавлен", Toast.LENGTH_SHORT).show()
                }

                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}