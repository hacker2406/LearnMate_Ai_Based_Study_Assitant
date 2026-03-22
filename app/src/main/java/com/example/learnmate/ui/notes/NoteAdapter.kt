package com.example.learnmate.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learnmate.data.model.Note
import com.example.learnmate.databinding.ItemNoteBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteDelete: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(
        private val binding: ItemNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.tvNoteTitle.text = note.title.ifEmpty { "Untitled" }
            binding.tvNotePreview.text = note.content.ifEmpty { "No content" }
            binding.tvNoteDate.text = formatDate(note.timestamp)
            binding.root.setOnClickListener { onNoteClick(note) }
            binding.btnDeleteNote.setOnClickListener { onNoteDelete(note) }
        }

        private fun formatDate(timestamp: Long): String {
            return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                .format(Date(timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) =
            oldItem == newItem
    }
}