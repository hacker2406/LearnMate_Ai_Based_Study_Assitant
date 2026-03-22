package com.example.learnmate.ui.planner

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnmate.R
import com.example.learnmate.data.model.Task
import com.example.learnmate.databinding.FragmentPlannerBinding

class PlannerFragment : Fragment(R.layout.fragment_planner) {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private var allTasks: List<Task> = emptyList()
    private var activeFilter = "ALL"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPlannerBinding.bind(view)

        setupRecyclerView()
        setupFilters()
        setupFab()
        observeTasks()
        observeState()
    }

    // ── RecyclerView ───────────────────────────────────────────────────
    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onToggleComplete = { task -> viewModel.toggleComplete(task) },
            onTaskDelete = { task -> viewModel.deleteTask(task) }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = adapter
    }

    // ── Filter chips ───────────────────────────────────────────────────
    private fun setupFilters() {
        val filters = mapOf(
            "ALL"     to binding.filterAll,
            "TODAY"   to binding.filterToday,
            "PENDING" to binding.filterPending,
            "DONE"    to binding.filterDone
        )

        filters.forEach { (key, view) ->
            view.setOnClickListener {
                activeFilter = key
                updateFilterUI(filters, key)
                applyFilter(allTasks)
            }
        }
    }

    private fun updateFilterUI(
        filters: Map<String, android.widget.TextView>,
        active: String
    ) {
        filters.forEach { (key, view) ->
            if (key == active) {
                view.setBackgroundResource(R.drawable.bg_tab_active)
                view.setTextColor(Color.WHITE)
            } else {
                view.setBackgroundResource(R.drawable.bg_tab_container)
                view.setTextColor(Color.parseColor("#9CA3AF"))
            }
        }
    }

    private fun applyFilter(tasks: List<Task>) {
        val today = System.currentTimeMillis()
        val startOfDay = today - (today % 86400000)
        val endOfDay = startOfDay + 86400000

        val filtered = when (activeFilter) {
            "TODAY"   -> tasks.filter {
                it.dueDate in startOfDay..endOfDay
            }
            "PENDING" -> tasks.filter { !it.isCompleted }
            "DONE"    -> tasks.filter { it.isCompleted }
            else      -> tasks
        }
        adapter.submitList(filtered)
        binding.emptyState.visibility =
            if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvTasks.visibility =
            if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    // ── FAB ────────────────────────────────────────────────────────────
    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, AddTaskFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // ── Observe tasks ──────────────────────────────────────────────────
    private fun observeTasks() {
        viewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            allTasks = tasks
            applyFilter(tasks)

            val total = tasks.size
            val completed = tasks.count { it.isCompleted }
            val progress = if (total > 0) (completed * 100) / total else 0

            binding.tvTaskCount.text = "$total ${if (total == 1) "task" else "tasks"}"
            binding.tvCompletedCount.text = "$completed completed"
            binding.taskProgressBar.progress = progress
        }
    }

    // ── Observe state ──────────────────────────────────────────────────
    private fun observeState() {
        viewModel.taskState.observe(viewLifecycleOwner) { state ->
            if (state is TaskState.Error) {
                android.widget.Toast.makeText(
                    requireContext(), state.message, android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}