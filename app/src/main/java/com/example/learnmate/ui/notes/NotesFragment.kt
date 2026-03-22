package com.example.learnmate.ui.notes

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnmate.R
import com.example.learnmate.data.model.Note
import com.example.learnmate.databinding.FragmentNotesBinding
import com.example.learnmate.ui.notes.AddEditNoteFragment
import com.example.learnmate.ui.notes.NoteAdapter
import com.example.learnmate.ui.notes.NoteState
import com.example.learnmate.ui.notes.NoteViewModel

class NotesFragment : Fragment(R.layout.fragment_notes) {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteViewModel by viewModels()
    private lateinit var adapter: NoteAdapter
    private var allNotes: List<Note> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentNotesBinding.bind(view)

        setupRecyclerView()
        setupSearch()
        setupFab()
        observeNotes()
        observeState()
    }

    // ── RecyclerView ───────────────────────────────────────────────────
    private fun setupRecyclerView() {
        adapter = NoteAdapter(
            onNoteClick = { note -> openAddEditNote(note) },
            onNoteDelete = { note -> viewModel.deleteNote(note) }
        )
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = adapter
    }

    // ── Search ─────────────────────────────────────────────────────────
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener { text ->
            val filtered = if (text.isNullOrBlank()) allNotes
            else allNotes.filter {
                it.title.contains(text, ignoreCase = true) ||
                        it.content.contains(text, ignoreCase = true)
            }
            adapter.submitList(filtered)
        }
    }

    // ── FAB ────────────────────────────────────────────────────────────
    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            openAddEditNote(null)
        }
    }

    // ── Open Add/Edit screen ───────────────────────────────────────────
    private fun openAddEditNote(note: Note?) {
        val fragment = AddEditNoteFragment().apply {
            note?.let {
                arguments = Bundle().apply {
                    putSerializable("note", it)
                }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    // ── Observe notes list ─────────────────────────────────────────────
    private fun observeNotes() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            allNotes = notes
            adapter.submitList(notes)

            val count = notes.size
            binding.tvNotesCount.text = "$count ${if (count == 1) "note" else "notes"}"
            binding.emptyState.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
            binding.rvNotes.visibility = if (notes.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    // ── Observe save/delete state ──────────────────────────────────────
    private fun observeState() {
        viewModel.noteState.observe(viewLifecycleOwner) { state ->
            // Errors shown here if delete fails silently
            if (state is NoteState.Error) {
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