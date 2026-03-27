package com.example.learnmate.ui.progress

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentProgressBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ProgressFragment : Fragment(R.layout.fragment_progress) {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProgressBinding.bind(view)

        setWeekRange()
        observeLoading()
        observeWeeklyStats()
        observeUserStats()

        binding.btnRefresh.setOnClickListener {
            viewModel.refresh()
        }
    }

    // ── Set week range label ───────────────────────────────────────────
    private fun setWeekRange() {
        val cal = Calendar.getInstance()
        val dayOfWeek    = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMon  = (dayOfWeek + 5) % 7
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMon)
        val weekStart    = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val weekEnd      = cal.time
        val sdf          = SimpleDateFormat("MMM dd", Locale.getDefault())
        binding.tvWeekRange.text = "${sdf.format(weekStart)} — ${sdf.format(weekEnd)}"
    }

    // ── Observe loading ────────────────────────────────────────────────
    private fun observeLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingBar.visibility   =
                if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility =
                if (isLoading) View.GONE else View.VISIBLE
        }
    }

    // ── Observe weekly stats ───────────────────────────────────────────
    private fun observeWeeklyStats() {
        viewModel.weeklyStats.observe(viewLifecycleOwner) { stats ->

            // ── This week summary ──────────────────────────────────────
            val hours   = stats.totalStudyMinutesThisWeek / 60
            val minutes = stats.totalStudyMinutesThisWeek % 60
            binding.tvWeekStudyTime.text = "${hours}h ${minutes}m"
            binding.tvWeekTasks.text     = "${stats.tasksCompletedThisWeek}"
            binding.tvWeekQuizzes.text   = "${stats.quizzesTakenThisWeek}"

            // ── Bar chart ──────────────────────────────────────────────
            binding.weeklyBarChart.data   = stats.studyMinutesPerDay
            binding.weeklyBarChart.labels = listOf(
                "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
            )

            // ── Quiz performance ───────────────────────────────────────
            binding.tvTotalQuizzes.text = "${stats.totalQuizzesTaken}"
            binding.tvAvgScore.text     = "${stats.averageQuizScore}%"
            binding.tvQuizXP.text       = "${stats.totalQuizXP}"
        }
    }

    // ── Observe user stats ─────────────────────────────────────────────
    private fun observeUserStats() {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->

            // XP + Level card
            binding.tvProgressLevel.text =
                "Level ${stats.level} — ${stats.levelTitle}"
            binding.tvProgressXP.text    =
                "${stats.xp} / ${stats.level * 200} XP"
            binding.tvProgressLevelBadge.text = "⚡ ${stats.xp} XP"
            binding.tvTotalXP.text       = "${stats.xp}"
            binding.tvCurrentLevel.text  = "${stats.level}"
            binding.tvProgressStreak.text = "${stats.streak}🔥"

            // XP progress bar
            val xpInLevel   = stats.xp - ((stats.level - 1) * 200)
            val progressPct = ((xpInLevel.toFloat() / 200f) * 100)
                .toInt().coerceIn(0, 100)
            binding.progressXpBar.progress = progressPct
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}