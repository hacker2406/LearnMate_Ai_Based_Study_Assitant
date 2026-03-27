package com.example.learnmate.ui.quiz

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.learnmate.data.model.Note
import com.example.learnmate.databinding.ItemNotePickerBinding

class NotePickerAdapter(
    private val notes: List<Note>,
    private val onNoteSelected: (Note) -> Unit
) : RecyclerView.Adapter<NotePickerAdapter.NotePickerViewHolder>() {

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotePickerViewHolder {
        val binding = ItemNotePickerBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotePickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotePickerViewHolder, position: Int) {
        holder.bind(notes[position], position == selectedPosition)
    }

    override fun getItemCount() = notes.size

    inner class NotePickerViewHolder(
        private val binding: ItemNotePickerBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note, isSelected: Boolean) {
            binding.tvNotePickerTitle.text   = note.title.ifEmpty { "Untitled" }
            binding.tvNotePickerPreview.text = note.content.take(60).ifEmpty { "No content" }

            if (isSelected) {
                binding.root.setCardBackgroundColor(Color.parseColor("#F5F3FF"))
                binding.tvNotePickerTitle.setTextColor(Color.parseColor("#7C3AED"))
                binding.tvSelectedCheck.visibility = android.view.View.VISIBLE
            } else {
                binding.root.setCardBackgroundColor(Color.WHITE)
                binding.tvNotePickerTitle.setTextColor(Color.parseColor("#1F1F2E"))
                binding.tvSelectedCheck.visibility = android.view.View.GONE
            }

            binding.root.setOnClickListener {
                val prev = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onNoteSelected(note)
            }
        }
    }
}