package com.example.learnmate.ui.notes

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.learnmate.R
import com.example.learnmate.data.model.Note
import com.example.learnmate.databinding.FragmentAddEditNoteBinding

class AddEditNoteFragment : Fragment(R.layout.fragment_add_edit_note) {

    private var _binding: FragmentAddEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NoteViewModel by viewModels()
    private var existingNote: Note? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditNoteBinding.bind(view)

        // ── Load existing note if editing ──────────────────────────────
        existingNote = arguments?.getSerializable("note") as? Note
        existingNote?.let {
            binding.tvScreenTitle.text = "Edit Note"
            binding.etNoteTitle.setText(it.title)
            binding.etNoteContent.setText(it.content)
        }

        // ── Listeners ──────────────────────────────────────────────────
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etNoteTitle.text.toString()
            val content = binding.etNoteContent.text.toString()
            viewModel.saveNote(title, content, existingNote?.id)
        }

        observeState()
    }

    private fun observeState() {
        viewModel.noteState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NoteState.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnSave.alpha = 0.6f
                }
                is NoteState.Success -> {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.alpha = 1f
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
                is NoteState.Error -> {
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