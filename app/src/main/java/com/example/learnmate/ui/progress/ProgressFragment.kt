package com.example.learnmate.ui.progress

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentProgressBinding
import com.example.learnmate.ui.home.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProgressFragment : Fragment(R.layout.fragment_progress) {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProgressViewModel by viewModels()

    // ── Share HomeViewModel to get live timer minutes ──────────────────
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProgressBinding.bind(view)

        setWeekRange()
        observeLoading()
        observeWeeklyStats()
        observeUserStats()
        observeLiveSession()

        binding.btnRefresh.setOnClickListener {
            viewModel.refresh()
        }
    }

    // ── Set week range label ───────────────────────────────────────────
    private fun setWeekRange() {
        val cal         = Calendar.getInstance()
        val dayOfWeek   = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMon = (dayOfWeek + 5) % 7
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMon)
        val weekStart   = cal.time
        cal.add(Calendar.DAY_OF_YEAR, 6)
        val weekEnd     = cal.time
        val sdf         = SimpleDateFormat("MMM dd", Locale.getDefault())
        binding.tvWeekRange.text =
            "${sdf.format(weekStart)} — ${sdf.format(weekEnd)}"
    }

    // ── Observe loading ────────────────────────────────────────────────
    private fun observeLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingBar.visibility    =
                if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility =
                if (isLoading) View.GONE else View.VISIBLE
        }
    }

    // ── Observe weekly stats ───────────────────────────────────────────
    private fun observeWeeklyStats() {
        viewModel.weeklyStats.observe(viewLifecycleOwner) { stats ->

            // This week summary
            val hours   = stats.totalStudyMinutesThisWeek / 60
            val minutes = stats.totalStudyMinutesThisWeek % 60
            binding.tvWeekStudyTime.text = "${hours}h ${minutes}m"
            binding.tvWeekTasks.text     = "${stats.tasksCompletedThisWeek}"
            binding.tvWeekQuizzes.text   = "${stats.quizzesTakenThisWeek}"

            // Bar chart
            binding.weeklyBarChart.data   = stats.studyMinutesPerDay
            binding.weeklyBarChart.labels = listOf(
                "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"
            )

            // Quiz performance
            binding.tvTotalQuizzes.text = "${stats.totalQuizzesTaken}"
            binding.tvAvgScore.text     = "${stats.averageQuizScore}%"
            binding.tvQuizXP.text       = "${stats.totalQuizXP}"
        }
    }

    // ── Observe user stats ─────────────────────────────────────────────
    private fun observeUserStats() {
        viewModel.userStats.observe(viewLifecycleOwner) { stats ->
            binding.tvProgressLevel.text      =
                "Level ${stats.level} — ${stats.levelTitle}"
            binding.tvProgressXP.text         =
                "${stats.xp} / ${stats.level * 200} XP"
            binding.tvProgressLevelBadge.text = "⚡ ${stats.xp} XP"
            binding.tvTotalXP.text            = "${stats.xp}"
            binding.tvCurrentLevel.text       = "${stats.level}"
            binding.tvProgressStreak.text     = "${stats.streak}🔥"

            val xpInLevel   = stats.xp - ((stats.level - 1) * 200)
            val progressPct = ((xpInLevel.toFloat() / 200f) * 100)
                .toInt().coerceIn(0, 100)
            binding.progressXpBar.progress = progressPct

            // ── Show today's study time (from Firestore) ───────────────
            val liveExtra  = homeViewModel.liveSessionMinutes.value ?: 0
            val totalMins  = stats.todayStudyMinutes + liveExtra
            val h          = totalMins / 60
            val m          = totalMins % 60
            updateStudyTimeDisplay(h, m)
        }
    }

    // ── Observe live session minutes from running timer ────────────────
    private fun observeLiveSession() {
        homeViewModel.liveSessionMinutes.observe(viewLifecycleOwner) { sessionMins ->
            val firestoreMins = viewModel.userStats.value?.todayStudyMinutes ?: 0
            val totalMins     = firestoreMins + sessionMins
            val h             = totalMins / 60
            val m             = totalMins % 60
            updateStudyTimeDisplay(h, m)

            // Also update the weekly bar chart today bar with live data
            viewModel.weeklyStats.value?.let { weekly ->
                val todayIndex   = getCurrentDayIndex()
                val updatedData  = weekly.studyMinutesPerDay.toMutableList()
                if (todayIndex < updatedData.size) {
                    updatedData[todayIndex] = firestoreMins + sessionMins
                }
                binding.weeklyBarChart.data = updatedData
            }
        }
    }

    // ── Update study time display ──────────────────────────────────────
    private fun updateStudyTimeDisplay(hours: Int, minutes: Int) {
        binding.tvWeekStudyTime.text =
            if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    // ── Get current day index (Mon=0 .. Sun=6) ─────────────────────────
    private fun getCurrentDayIndex(): Int {
        val cal = Calendar.getInstance()
        return (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}