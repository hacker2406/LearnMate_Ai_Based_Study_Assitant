package com.example.learnmate.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setGreeting()
        loadUserName()
        setupMoodPicker()
        setupToolCards()
        observeHomeData()
        observeTimer()

        // Update streak when home opens
        viewModel.updateStreakOnOpen()
    }

    // ── Greeting ───────────────────────────────────────────────────────
    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else      -> "Good evening,"
        }
    }

    // ── Load user name from Firestore ──────────────────────────────────
    private fun loadUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (_binding == null) return@addOnSuccessListener
                val fullName = snapshot.getString("name").orEmpty().trim()
                binding.tvUserName.text = if (fullName.isNotEmpty())
                    "${fullName.substringBefore(" ")} 👋"
                else "Learner 👋"
            }
            .addOnFailureListener {
                if (_binding != null) binding.tvUserName.text = "Learner 👋"
            }
    }

    // ── Observe combined home data ─────────────────────────────────────
    private fun observeHomeData() {
        viewModel.homeData.observe(viewLifecycleOwner) { data ->
            val stats = data.stats

            // ── Stat chips ─────────────────────────────────────────────────
            binding.tvStreakChip.text = "🔥  ${stats.streak} day streak"
            binding.tvXpChip.text    = "⚡  ${stats.xp} XP"
            binding.tvLevelChip.text = "🏅  Lv. ${stats.level}"

            // ── XP progress bar ────────────────────────────────────────────
            val xpForNext = stats.level * 200
            binding.tvXpLabel.text        = "Level ${stats.level} — ${stats.levelTitle}"
            binding.tvXpValues.text       = "${stats.xp} / $xpForNext XP"
            binding.xpProgressBar.max     = xpForNext
            binding.xpProgressBar.progress = stats.xp

            // ── TODAY card 1 — STUDIED ─────────────────────────────────────
            val studiedHours   = stats.todayStudyMinutes / 60
            val studiedMinutes = stats.todayStudyMinutes % 60
            binding.cardStudy.tvCardIcon.text  = "⏱️"
            binding.cardStudy.tvCardValue.text = if (studiedHours > 0)
                "${studiedHours}h ${studiedMinutes}m" else "${studiedMinutes}m"
            binding.cardStudy.tvCardLabel.text = "STUDIED"

            // ── TODAY card 2 — TASKS DONE ──────────────────────────────────
            binding.cardTask.tvCardIcon.text  = "✅"
            binding.cardTask.tvCardValue.text =
                "${data.completedTodayCount} / ${data.totalTasksCount}"
            binding.cardTask.tvCardLabel.text = "TASKS DONE"

            // ── TODAY card 3 — QUIZ PTS ────────────────────────────────────
            binding.cardQuiz.tvCardIcon.text  = "🧠"
            binding.cardQuiz.tvCardValue.text = "${data.quizXpToday}"
            binding.cardQuiz.tvCardLabel.text = "QUIZ PTS"

            // ── Tool cards — live counts ───────────────────────────────────
            binding.tvNotesCount.text =
                "${data.notesCount} ${if (data.notesCount == 1) "note" else "notes"}"
            binding.tvTasksDue.text =
                "${data.pendingTasksCount} tasks due"
        }
    }

    // ── Observe focus timer ────────────────────────────────────────────
    private fun observeTimer() {
        viewModel.timerState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is TimerState.Idle -> {
                    binding.focusProgressBar.progress   = 0
                    binding.focusProgressBar.centerText = "25:00"
                    binding.tvTimerInfo.text            = "Ready to focus?"
                    binding.tvTimerSub.text             = "25 min Pomodoro session"
                    binding.btnTimerToggle.text         = "Start"
                }
                is TimerState.Running -> {
                    val mins     = state.remainingSeconds / 60
                    val secs     = state.remainingSeconds % 60
                    val progress = ((25 * 60 - state.remainingSeconds) * 100) / (25 * 60)
                    binding.focusProgressBar.progress   = progress
                    binding.focusProgressBar.centerText = "%02d:%02d".format(mins, secs)
                    binding.tvTimerInfo.text            = "Focus session active 🔥"
                    binding.tvTimerSub.text             = "%02d:%02d remaining".format(mins, secs)
                    binding.btnTimerToggle.text         = "Pause"
                }
                is TimerState.Paused -> {
                    val mins     = state.remainingSeconds / 60
                    val secs     = state.remainingSeconds % 60
                    binding.focusProgressBar.centerText = "%02d:%02d".format(mins, secs)
                    binding.tvTimerInfo.text            = "Session paused ⏸"
                    binding.tvTimerSub.text             = "%02d:%02d remaining".format(mins, secs)
                    binding.btnTimerToggle.text         = "Resume"
                }
                is TimerState.Completed -> {
                    binding.focusProgressBar.progress   = 100
                    binding.focusProgressBar.centerText = "Done!"
                    binding.tvTimerInfo.text            = "Session complete! +30 XP 🎉"
                    binding.tvTimerSub.text             = "Take a 5 min break"
                    binding.btnTimerToggle.text         = "Start"
                }
            }
        }

        binding.btnTimerToggle.setOnClickListener {
            when (viewModel.timerState.value) {
                is TimerState.Running -> viewModel.pauseTimer()
                else                  -> viewModel.startTimer()
            }
        }
    }

    // ── Mood picker ────────────────────────────────────────────────────
    private fun setupMoodPicker() {
        val chips = listOf(
            binding.moodRough,
            binding.moodOkay,
            binding.moodGood,
            binding.moodGreat
        )
        chips.forEach { chip ->
            chip.setOnClickListener {
                chips.forEach { it.setBackgroundResource(R.drawable.bg_mood_chip) }
                chip.setBackgroundResource(R.drawable.bg_mood_chip_active)
            }
        }
    }

    // ── Tool card navigation ───────────────────────────────────────────
    private fun setupToolCards() {
        binding.toolNotes.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer,
                    com.example.learnmate.ui.notes.NotesFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.toolPlanner.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer,
                    com.example.learnmate.ui.planner.PlannerFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.toolQuiz.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer,
                    com.example.learnmate.ui.quiz.QuizFragment())
                .addToBackStack(null)
                .commit()
        }
        binding.toolProgress.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer,
                    com.example.learnmate.ui.progress.ProgressFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}