package com.example.learnmate.ui.quiz

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentQuizPlayBinding

class QuizPlayFragment : Fragment(R.layout.fragment_quiz_play) {

    private var _binding: FragmentQuizPlayBinding? = null
    private val binding get() = _binding!!

    // ── Shared ViewModel with QuizFragment ─────────────────────────────
    private val viewModel: QuizViewModel by activityViewModels()
    private lateinit var optionsAdapter: OptionsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizPlayBinding.bind(view)

        binding.rvOptions.layoutManager = LinearLayoutManager(requireContext())

        // Load first question immediately
        loadCurrentQuestion()

        observeQuestion()
        observeAnswer()
        observeState()

        binding.btnNext.setOnClickListener {
            viewModel.nextQuestion()
        }
    }

    // ── Load question directly ─────────────────────────────────────────
    private fun loadCurrentQuestion() {
        val questions = viewModel.questions.value ?: return
        val index     = viewModel.currentIndex.value ?: 0
        if (questions.isEmpty() || index >= questions.size) return

        val question = questions[index]
        val total    = questions.size

        binding.tvQuizTopic.text         = viewModel.currentTopic
        binding.tvQuestion.text          = question.question
        binding.tvQuestionCount.text     = "${index + 1} / $total"
        binding.tvScore.text             = "⚡ Score: ${viewModel.score.value ?: 0}"
        binding.quizProgressBar.progress = ((index + 1) * 100) / total
        binding.btnNext.visibility         = View.GONE
        binding.explanationCard.visibility = View.GONE

        optionsAdapter = OptionsAdapter(question.options) { selectedIndex ->
            viewModel.submitAnswer(selectedIndex)
        }
        binding.rvOptions.adapter = optionsAdapter
    }

    // ── Observe index changes (next question) ──────────────────────────
    private fun observeQuestion() {
        viewModel.currentIndex.observe(viewLifecycleOwner) {
            loadCurrentQuestion()
        }
    }

    // ── Observe selected answer ────────────────────────────────────────
    private fun observeAnswer() {
        viewModel.selectedAnswer.observe(viewLifecycleOwner) { selected ->
            if (selected == null) return@observe
            if (!::optionsAdapter.isInitialized) return@observe

            val questions = viewModel.questions.value ?: return@observe
            val index     = viewModel.currentIndex.value ?: return@observe
            if (index >= questions.size) return@observe

            val question = questions[index]

            optionsAdapter.revealAnswer(selected, question.correctIndex)

            binding.tvExplanation.text         = question.explanation
            binding.explanationCard.visibility = View.VISIBLE

            val isLast = index + 1 >= questions.size
            binding.btnNext.text       = if (isLast) "See Results 🎉" else "Next →"
            binding.btnNext.visibility = View.VISIBLE
            binding.tvScore.text       = "⚡ Score: ${viewModel.score.value ?: 0}"
        }
    }

    // ── Observe quiz state ─────────────────────────────────────────────
    private fun observeState() {
        viewModel.quizState.observe(viewLifecycleOwner) { state ->
            if (state is QuizState.Finished) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.mainFragmentContainer, QuizResultFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}