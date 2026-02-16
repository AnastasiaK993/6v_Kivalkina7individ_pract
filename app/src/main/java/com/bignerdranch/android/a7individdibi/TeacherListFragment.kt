package com.bignerdranch.android.a7individdibi

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.a7individdibi.database.AppDatabase
import com.bignerdranch.android.a7individdibi.databinding.FragmentTeacherListBinding
import com.bignerdranch.android.a7individdibi.data.Teacher
import kotlinx.coroutines.launch

class TeacherListFragment : Fragment() {

    private var _binding: FragmentTeacherListBinding? = null
    private val binding get() = _binding!!
    private var userRole: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeacherListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("user_role", "") ?: ""

        val db = AppDatabase.getInstance(requireContext())


        if (userRole == "admissions") {
            binding.btnAddTeacher.visibility = View.VISIBLE
            binding.btnAddTeacher.text = "+ Добавить преподавателя"
        } else {
            binding.btnAddTeacher.visibility = View.GONE
        }


        val adapter = TeacherAdapter(
            userRole = userRole,
            onEditClick = if (userRole == "admissions") { { teacher ->
                val bundle = Bundle().apply {
                    putInt("teacher_id", teacher.id)
                }
                findNavController().navigate(R.id.action_teacherListFragment_to_addEditTeacherFragment, bundle)
            } } else { null },
            onDeleteClick = if (userRole == "admissions") { { teacher ->
                lifecycleScope.launch {
                    db.teacherDao().deleteTeacher(teacher)
                    Toast.makeText(requireContext(), "Преподаватель удален", Toast.LENGTH_SHORT).show()
                }
            } } else { null }
        )
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            db.teacherDao().getAllTeachers().collect { teachers ->
                adapter.submitList(teachers)
            }
        }

        if (userRole == "admissions") {
            binding.btnAddTeacher.setOnClickListener {
                findNavController().navigate(R.id.action_teacherListFragment_to_addEditTeacherFragment)
            }
        }
    }

    inner class TeacherAdapter(
        private val userRole: String,
        private val onEditClick: ((Teacher) -> Unit)?,
        private val onDeleteClick: ((Teacher) -> Unit)?
    ) : androidx.recyclerview.widget.ListAdapter<Teacher, TeacherAdapter.ViewHolder>(
        object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Teacher>() {
            override fun areItemsTheSame(oldItem: Teacher, newItem: Teacher) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Teacher, newItem: Teacher) = oldItem == newItem
        }
    ) {
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvName: TextView = itemView.findViewById(R.id.tvTeacherName)
            val tvHours: TextView = itemView.findViewById(R.id.tvTeacherHours)
            val tvSpecialties: TextView = itemView.findViewById(R.id.tvTeacherSpecialties)
            val btnEdit: View = itemView.findViewById(R.id.btnEditTeacher)
            val btnDelete: View = itemView.findViewById(R.id.btnDeleteTeacher)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_teacher, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val teacher = getItem(position)

            val specialtiesText = "Специальности: ${teacher.specialtyIds.joinToString()}"

            holder.tvName.text = teacher.fullName
            holder.tvHours.text = "Часов/год: ${teacher.totalHoursPerYear}"
            holder.tvSpecialties.text = specialtiesText

            if (userRole == "admissions") {
                holder.btnEdit.visibility = View.VISIBLE
                holder.btnDelete.visibility = View.VISIBLE

                holder.btnEdit.setOnClickListener { onEditClick?.invoke(teacher) }
                holder.btnDelete.setOnClickListener { onDeleteClick?.invoke(teacher) }
            } else {
                holder.btnEdit.visibility = View.GONE
                holder.btnDelete.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}