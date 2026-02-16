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
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.a7individdibi.databinding.FragmentStudentListBinding
import com.bignerdranch.android.a7individdibi.databinding.ItemStudentBinding
import com.bignerdranch.android.a7individdibi.database.AppDatabase
import com.bignerdranch.android.a7individdibi.data.Student
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class StudentListFragment : Fragment() {

    private var _binding: FragmentStudentListBinding? = null
    private val binding get() = _binding!!
    private var userRole: String = ""
    private var userEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем роль и email пользователя
        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("user_role", "") ?: ""
        userEmail = sharedPref.getString("user_email", "") ?: ""

        val db = AppDatabase.getInstance(requireContext())


        when (userRole) {
            "student" -> {
                setupStudentView(db)
            }
            "teacher" -> {

                setupStaffView(db, canDelete = true, canEdit = true)
            }
            "admissions" -> {

                setupStaffView(db, canDelete = true, canEdit = true)
            }
            else -> {
                binding.recyclerView.visibility = View.GONE
                binding.btnAddStudent.visibility = View.GONE
            }
        }
    }

    private fun setupStudentView(db: AppDatabase) {
        lifecycleScope.launch {
            try {

                val allStudents = db.studentDao().getAllStudentsDirect()


                val studentEmailPart = userEmail.substringBefore("@").lowercase()
                val myStudent = allStudents.find { student ->
                    student.fullName.lowercase().contains(studentEmailPart)
                }

                if (myStudent != null) {

                    binding.btnAddStudent.visibility = View.GONE


                    val adapter = StudentAdapterForStudent(
                        onEditClick = { student ->
                            val bundle = Bundle().apply {
                                putInt("student_id", student.id)
                                putString("mode", "edit_for_student")
                            }
                            findNavController().navigate(R.id.action_studentListFragment_to_addEditStudentFragment, bundle)
                        }
                    )
                    binding.recyclerView.adapter = adapter
                    adapter.submitList(listOf(myStudent))

                    Toast.makeText(requireContext(), "Это ваша анкета", Toast.LENGTH_SHORT).show()
                } else {

                    binding.btnAddStudent.visibility = View.VISIBLE
                    binding.btnAddStudent.text = "Создать мою анкету"

                    val adapter = StudentAdapterForStudent(
                        onEditClick = {}
                    )
                    binding.recyclerView.adapter = adapter
                    adapter.submitList(emptyList())

                    binding.btnAddStudent.setOnClickListener {
                        val bundle = Bundle().apply {
                            putString("mode", "create_for_student")
                        }
                        findNavController().navigate(R.id.action_studentListFragment_to_addEditStudentFragment, bundle)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupStaffView(db: AppDatabase, canDelete: Boolean, canEdit: Boolean) {
        // Показываем кнопку добавления
        binding.btnAddStudent.visibility = View.VISIBLE
        binding.btnAddStudent.text = "Добавить студента"

        binding.btnAddStudent.setOnClickListener {
            findNavController().navigate(R.id.action_studentListFragment_to_addEditStudentFragment)
        }

        val adapter = StudentAdapterForStaff(
            onEditClick = if (canEdit) { { student ->
                val bundle = Bundle().apply {
                    putInt("student_id", student.id)
                }
                findNavController().navigate(R.id.action_studentListFragment_to_addEditStudentFragment, bundle)
            } } else { null },
            onDeleteClick = if (canDelete) { { student ->
                lifecycleScope.launch {
                    try {
                        db.studentDao().deleteStudent(student)
                        Toast.makeText(requireContext(), "Студент удален", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Ошибка удаления", Toast.LENGTH_SHORT).show()
                    }
                }
            } } else { null }
        )

        binding.recyclerView.adapter = adapter


        lifecycleScope.launch {
            try {
                db.studentDao().getAllStudents().collect { students ->
                    adapter.submitList(students)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Ошибка загрузки списка", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class StudentAdapterForStaff(
        private val onEditClick: ((Student) -> Unit)?,
        private val onDeleteClick: ((Student) -> Unit)?
    ) : androidx.recyclerview.widget.ListAdapter<Student, StudentAdapterForStaff.ViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Student>() {
            override fun areItemsTheSame(oldItem: Student, newItem: Student) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Student, newItem: Student) = oldItem == newItem
        }
    ) {
        inner class ViewHolder(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = getItem(position)
            holder.binding.apply {
                tvName.text = student.fullName
                tvGroup.text = "Группа: ${student.groupName}"
                tvCourse.text = "Курс: ${student.course}"
                tvBudget.text = if (student.isBudget) "Бюджет" else "Внебюджет"
                tvBudget.setTextColor(if (student.isBudget) android.graphics.Color.GREEN else android.graphics.Color.RED)

                val photoResId = resources.getIdentifier(student.photoUri, "drawable", requireContext().packageName)
                Picasso.get()
                    .load(if (photoResId != 0) photoResId else R.drawable.ic_default_student)
                    .placeholder(R.drawable.ic_default_student)
                    .error(R.drawable.ic_default_student)
                    .into(ivStudentPhoto)

                if (onEditClick != null) {
                    btnEdit.visibility = View.VISIBLE
                    btnEdit.setOnClickListener { onEditClick.invoke(student) }
                } else {
                    btnEdit.visibility = View.GONE
                }

                if (onDeleteClick != null) {
                    btnDelete.visibility = View.VISIBLE
                    btnDelete.setOnClickListener { onDeleteClick.invoke(student) }
                } else {
                    btnDelete.visibility = View.GONE
                }
            }
        }
    }

    inner class StudentAdapterForStudent(
        private val onEditClick: (Student) -> Unit
    ) : androidx.recyclerview.widget.ListAdapter<Student, StudentAdapterForStudent.ViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Student>() {
            override fun areItemsTheSame(oldItem: Student, newItem: Student) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Student, newItem: Student) = oldItem == newItem
        }
    ) {
        inner class ViewHolder(val binding: ItemStudentBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = getItem(position)
            holder.binding.apply {
                tvName.text = student.fullName
                tvGroup.text = "Группа: ${student.groupName}"
                tvCourse.text = "Курс: ${student.course}"
                tvBudget.text = if (student.isBudget) "Бюджет" else "Внебюджет"
                tvBudget.setTextColor(if (student.isBudget) android.graphics.Color.GREEN else android.graphics.Color.RED)

                val photoResId = resources.getIdentifier(student.photoUri, "drawable", requireContext().packageName)
                Picasso.get()
                    .load(if (photoResId != 0) photoResId else R.drawable.ic_default_student)
                    .placeholder(R.drawable.ic_default_student)
                    .error(R.drawable.ic_default_student)
                    .into(ivStudentPhoto)

                btnEdit.visibility = View.VISIBLE
                btnDelete.visibility = View.GONE
                btnEdit.setOnClickListener { onEditClick(student) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}