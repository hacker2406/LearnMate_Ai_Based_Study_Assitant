package com.example.learnmate.ui.planner

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentAddTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTaskFragment : Fragment(R.layout.fragment_add_task) {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()

    private var selectedDueDate: Long = 0L
    private var selectedPriority: String = "MEDIUM"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddTaskBinding.bind(view)

        setupPrioritySelector()
        setupDatePicker()
        setupButtons()
        observeState()
    }

    // ── Priority selector ──────────────────────────────────────────────
    private fun setupPrioritySelector() {
        updatePriorityUI("MEDIUM")

        binding.priorityLow.setOnClickListener {
            selectedPriority = "LOW"
            updatePriorityUI("LOW")
        }
        binding.priorityMedium.setOnClickListener {
            selectedPriority = "MEDIUM"
            updatePriorityUI("MEDIUM")
        }
        binding.priorityHigh.setOnClickListener {
            selectedPriority = "HIGH"
            updatePriorityUI("HIGH")
        }
    }

    private fun updatePriorityUI(selected: String) {
        // Reset all to inactive
        listOf(binding.priorityLow, binding.priorityMedium, binding.priorityHigh)
            .forEach {
                it.setBackgroundResource(R.drawable.bg_tab_container)
                it.setTextColor(android.graphics.Color.parseColor("#9CA3AF"))
            }

        // Highlight selected
        val activeView = when (selected) {
            "LOW"  -> binding.priorityLow
            "HIGH" -> binding.priorityHigh
            else   -> binding.priorityMedium
        }
        activeView.setBackgroundResource(R.drawable.bg_tab_active)
        activeView.setTextColor(android.graphics.Color.WHITE)
    }

    // ── Date picker ────────────────────────────────────────────────────
    private fun setupDatePicker() {
        binding.btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDueDate = calendar.timeInMillis
                    binding.tvDueDate.text = SimpleDateFormat(
                        "MMM dd, yyyy", Locale.getDefault()
                    ).format(Date(selectedDueDate))
                    binding.tvDueDate.setTextColor(
                        android.graphics.Color.parseColor("#1F1F2E")
                    )
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    // ── Buttons ────────────────────────────────────────────────────────
    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTaskTitle.text.toString()
            val description = binding.etTaskDescription.text.toString()
            viewModel.saveTask(title, description, selectedDueDate, selectedPriority)
        }
    }

    // ── Observe state ──────────────────────────────────────────────────
    private fun observeState() {
        viewModel.taskState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TaskState.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnSave.alpha = 0.6f
                }
                is TaskState.Success -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.alpha = 1f
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                is TaskState.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.alpha = 1f
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.alpha = 1f
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}