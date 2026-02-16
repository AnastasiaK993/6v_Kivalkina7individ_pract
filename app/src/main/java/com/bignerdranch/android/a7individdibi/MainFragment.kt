package com.bignerdranch.android.a7individdibi

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerdranch.android.a7individdibi.database.AppDatabase
import com.bignerdranch.android.a7individdibi.databinding.FragmentMainBinding
import com.bignerdranch.android.a7individdibi.databinding.ItemStudentCarouselBinding
import com.bignerdranch.android.a7individdibi.data.Student
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var userRole: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        userRole = sharedPref.getString("user_role", "") ?: ""

        val db = AppDatabase.getInstance(requireContext())


        binding.carouselRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val carouselAdapter = CarouselAdapter()
        binding.carouselRecyclerView.adapter = carouselAdapter

        loadAllStudents(db, carouselAdapter)


        setupSearchView(db, carouselAdapter)


        setupButtons()
    }

    private fun loadAllStudents(db: AppDatabase, adapter: CarouselAdapter) {
        lifecycleScope.launch {
            try {
                db.studentDao().getAllStudents().collect { students ->
                    adapter.submitList(students)
                    Log.d("SEARCH", "Загружено студентов: ${students.size}")
                }
            } catch (e: Exception) {
                Log.e("SEARCH", "Ошибка загрузки: ${e.message}")
            }
        }
    }

    private fun setupSearchView(db: AppDatabase, adapter: CarouselAdapter) {
        binding.searchView.isFocusable = true
        binding.searchView.isFocusableInTouchMode = true

        binding.searchView.setOnClickListener {
            binding.searchView.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
        }


        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(db, adapter, query)
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(db, adapter, newText)
                return true
            }
        })
    }

    private fun search(db: AppDatabase, adapter: CarouselAdapter, query: String?) {
        lifecycleScope.launch {
            if (query.isNullOrEmpty()) {
                db.studentDao().getAllStudents().collect { students ->
                    adapter.submitList(students)
                    Log.d("SEARCH", "Показаны все: ${students.size}")
                }
            } else {
                db.studentDao().searchStudents(query).collect { students ->
                    adapter.submitList(students)
                    Log.d("SEARCH", "Найдено по запросу '$query': ${students.size}")
                }
            }
        }
    }

    private fun setupButtons() {
        when (userRole) {
            "admissions" -> {
                binding.btnToStudents.visibility = View.VISIBLE
                binding.btnToTeachers.visibility = View.VISIBLE
                binding.btnToStudents.setOnClickListener {
                    findNavController().navigate(R.id.action_mainFragment_to_studentListFragment)
                }
                binding.btnToTeachers.setOnClickListener {
                    findNavController().navigate(R.id.action_mainFragment_to_teacherListFragment)
                }
            }
            "teacher" -> {
                binding.btnToStudents.visibility = View.VISIBLE
                binding.btnToTeachers.visibility = View.GONE
                binding.btnToStudents.setOnClickListener {
                    findNavController().navigate(R.id.action_mainFragment_to_studentListFragment)
                }
            }
            "student" -> {
                binding.btnToStudents.visibility = View.VISIBLE
                binding.btnToTeachers.visibility = View.GONE
                binding.btnToStudents.setOnClickListener {
                    findNavController().navigate(R.id.action_mainFragment_to_studentListFragment)
                }
            }
            else -> {
                binding.btnToStudents.visibility = View.GONE
                binding.btnToTeachers.visibility = View.GONE
            }
        }
    }

    inner class CarouselAdapter :
        androidx.recyclerview.widget.ListAdapter<Student, CarouselAdapter.ViewHolder>(
            object : androidx.recyclerview.widget.DiffUtil.ItemCallback<Student>() {
                override fun areItemsTheSame(oldItem: Student, newItem: Student) = oldItem.id == newItem.id
                override fun areContentsTheSame(oldItem: Student, newItem: Student) = oldItem == newItem
            }
        ) {

        inner class ViewHolder(val binding: ItemStudentCarouselBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemStudentCarouselBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val student = getItem(position)
            holder.binding.apply {
                tvName.text = student.fullName
                tvGroup.text = "Группа: ${student.groupName}"

                val photoResId = resources.getIdentifier(
                    student.photoUri,
                    "drawable",
                    requireContext().packageName
                )

                Picasso.get()
                    .load(if (photoResId != 0) photoResId else R.drawable.ic_default_student)
                    .placeholder(R.drawable.ic_default_student)
                    .error(R.drawable.ic_default_student)
                    .into(ivStudentPhoto)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}