package com.example.learnmate.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnmate.R
import com.example.learnmate.data.model.Note
import com.example.learnmate.databinding.FragmentQuizBinding

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizViewModel by activityViewModels()

    private var isTopicMode = true
    private var selectedNote: Note? = null
    private var numQuestions = 5

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizBinding.bind(view)

        setupSourceSelector()
        setupQuestionCount()
        setupNotesPicker()
        setupGenerateButton()
        observeState()
    }

    // ── Source selector ────────────────────────────────────────────────
    private fun setupSourceSelector() {
        binding.sourceTopicBtn.setOnClickListener {
            isTopicMode = true
            binding.sourceTopicBtn.setBackgroundResource(R.drawable.bg_tab_active)
            binding.sourceTopicBtn.setTextColor(Color.WHITE)
            binding.sourceNoteBtn.setBackgroundResource(R.drawable.bg_tab_container)
            binding.sourceNoteBtn.setTextColor(Color.parseColor("#9CA3AF"))
            binding.topicInputCard.visibility = View.VISIBLE
            binding.notesPickerCard.visibility = View.GONE
        }

        binding.sourceNoteBtn.setOnClickListener {
            isTopicMode = false
            binding.sourceNoteBtn.setBackgroundResource(R.drawable.bg_tab_active)
            binding.sourceNoteBtn.setTextColor(Color.WHITE)
            binding.sourceTopicBtn.setBackgroundResource(R.drawable.bg_tab_container)
            binding.sourceTopicBtn.setTextColor(Color.parseColor("#9CA3AF"))
            binding.topicInputCard.visibility = View.GONE
            binding.notesPickerCard.visibility = View.VISIBLE
        }
    }

    // ── Question count selector ────────────────────────────────────────
    private fun setupQuestionCount() {
        val buttons = mapOf(5 to binding.num5, 10 to binding.num10, 15 to binding.num15)

        buttons.forEach { (count, view) ->
            view.setOnClickListener {
                numQuestions = count
                buttons.forEach { (_, v) ->
                    v.setBackgroundResource(R.drawable.bg_tab_container)
                    v.setTextColor(Color.parseColor("#9CA3AF"))
                }
                view.setBackgroundResource(R.drawable.bg_tab_active)
                view.setTextColor(Color.WHITE)
            }
        }
    }

    // ── Notes picker ───────────────────────────────────────────────────
    private fun setupNotesPicker() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            if (notes.isEmpty()) {
                binding.notesPickerCard.visibility = View.GONE
                return@observe
            }
            val adapter = NotePickerAdapter(notes) { note ->
                selectedNote = note
            }
            binding.rvNotesPicker.layoutManager = LinearLayoutManager(requireContext())
            binding.rvNotesPicker.adapter = adapter
        }
    }

    // ── Generate button ────────────────────────────────────────────────
    private fun setupGenerateButton() {
        binding.btnGenerate.setOnClickListener {
            binding.tvError.visibility = View.GONE

            if (isTopicMode) {
                val topic = binding.etTopic.text.toString().trim()
                if (topic.isEmpty()) {
                    binding.tvError.text = "Please enter a topic"
                    binding.tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                viewModel.generateFromTopic(topic, numQuestions)
            } else {
                val note = selectedNote
                if (note == null) {
                    binding.tvError.text = "Please select a note"
                    binding.tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                viewModel.generateFromNote(note, numQuestions)
            }
        }
    }

    // ── Observe quiz state ─────────────────────────────────────────────
    private fun observeState() {
        viewModel.quizState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is QuizState.Loading -> {
                    binding.btnGenerate.visibility = View.GONE
                    binding.loadingView.visibility = View.VISIBLE
                    binding.tvError.visibility = View.GONE
                }

                is QuizState.Playing -> {
                    binding.btnGenerate.visibility = View.VISIBLE
                    binding.loadingView.visibility = View.GONE
                    // Navigate to play screen
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.mainFragmentContainer, QuizPlayFragment())
                        .addToBackStack(null)
                        .commit()
                }

                is QuizState.Error -> {
                    binding.btnGenerate.visibility = View.VISIBLE
                    binding.loadingView.visibility = View.GONE
                    binding.tvError.text = state.message
                    binding.tvError.visibility = View.VISIBLE
                }

                else -> {
                    binding.btnGenerate.visibility = View.VISIBLE
                    binding.loadingView.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}