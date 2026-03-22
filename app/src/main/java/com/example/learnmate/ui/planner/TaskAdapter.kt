package com.example.learnmate.ui.planner

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learnmate.data.model.Task
import com.example.learnmate.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(
    private val onToggleComplete: (Task) -> Unit,
    private val onTaskDelete: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {

            binding.tvTaskTitle.text = task.title
            binding.tvTaskDescription.text = task.description
            binding.tvTaskDate.text = if (task.dueDate > 0)
                "Due: ${formatDate(task.dueDate)}" else "No due date"

            // ── Priority badge ─────────────────────────────────────────
            binding.tvPriority.text = task.priority
            binding.tvPriority.setBackgroundColor(
                when (task.priority) {
                    "HIGH"   -> 0xFFFFE4E4.toInt()
                    "MEDIUM" -> 0xFFFFF3CD.toInt()
                    else     -> 0xFFE4F4E4.toInt()
                }
            )
            binding.tvPriority.setTextColor(
                when (task.priority) {
                    "HIGH"   -> 0xFFDC2626.toInt()
                    "MEDIUM" -> 0xFFD97706.toInt()
                    else     -> 0xFF16A34A.toInt()
                }
            )

            // ── Completed state ────────────────────────────────────────
            if (task.isCompleted) {
                binding.tvTaskTitle.paintFlags =
                    binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.alpha = 0.5f
                binding.tvTaskDescription.alpha = 0.5f
                binding.checkTask.isChecked = true
            } else {
                binding.tvTaskTitle.paintFlags =
                    binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.alpha = 1f
                binding.tvTaskDescription.alpha = 1f
                binding.checkTask.isChecked = false
            }

            // ── Click listeners ────────────────────────────────────────
            binding.checkTask.setOnClickListener { onToggleComplete(task) }
            binding.btnDeleteTask.setOnClickListener { onTaskDelete(task) }
        }

        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem
    }
}