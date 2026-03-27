package com.example.learnmate.ui.quiz

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentQuizResultBinding
import com.example.learnmate.ui.home.HomeFragment

class QuizResultFragment : Fragment(R.layout.fragment_quiz_result) {

    private var _binding: FragmentQuizResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentQuizResultBinding.bind(view)

        displayResults()
        setupButtons()
    }

    private fun displayResults() {
        val score = viewModel.score.value ?: 0
        val total = viewModel.questions.value?.size ?: 0
        val xp    = score * 10
        val pct   = if (total > 0) (score * 100) / total else 0

        binding.tvFinalScore.text    = "$score / $total"
        binding.tvXpEarned.text      = "⚡ +$xp XP earned!"
        binding.tvResultTopic.text   = "Topic: ${viewModel.currentTopic}"
        binding.scoreProgressBar.progress = pct

        // Score emoji + label
        binding.tvScoreEmoji.text = when {
            pct == 100 -> "🏆"
            pct >= 80  -> "🎉"
            pct >= 60  -> "👍"
            pct >= 40  -> "📚"
            else       -> "💪"
        }

        binding.tvPerformanceLabel.text = when {
            pct == 100 -> "Perfect score! 🏆"
            pct >= 80  -> "Great job! 🌟"
            pct >= 60  -> "Good effort! 👍"
            pct >= 40  -> "Keep studying! 📚"
            else       -> "Don't give up! 💪"
        }
    }

    private fun setupButtons() {
        binding.btnPlayAgain.setOnClickListener {
            viewModel.resetQuiz()
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, QuizFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.btnBackHome.setOnClickListener {
            // Pop back to home
            parentFragmentManager.popBackStack(null,
                androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, HomeFragment())
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}