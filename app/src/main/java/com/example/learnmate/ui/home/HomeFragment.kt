package com.example.learnmate.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setGreeting()
        loadUserName()
        setupMoodPicker()
        setupToolCards()
    }

    // ── Greeting changes based on time of day ──────────────────────────
    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good morning,"
            hour < 17 -> "Good afternoon,"
            else      -> "Good evening,"
        }
    }

    // ── Load first name from Firestore ─────────────────────────────────
    private fun loadUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Firebase.firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                // Guard: check binding is still alive (user may have navigated away)
                if (_binding == null) return@addOnSuccessListener

                val fullName = snapshot.getString("name").orEmpty().trim()
                binding.tvUserName.text = if (fullName.isNotEmpty()) {
                    "${fullName.substringBefore(" ")} 👋"
                } else {
                    "Learner 👋"   // fallback if name missing in Firestore
                }
            }
            .addOnFailureListener {
                // Silently fall back — don't crash if Firestore is unreachable
                if (_binding != null) {
                    binding.tvUserName.text = "Learner 👋"
                }
            }
    }

    // ── Mood picker: highlight selected chip ───────────────────────────
    private fun setupMoodPicker() {
        val chips = listOf(
            binding.moodRough,
            binding.moodOkay,
            binding.moodGood,
            binding.moodGreat
        )

        chips.forEach { chip ->
            chip.setOnClickListener {
                // Reset all to inactive
                chips.forEach { it.setBackgroundResource(R.drawable.bg_mood_chip) }
                // Highlight selected
                chip.setBackgroundResource(R.drawable.bg_mood_chip_active)
            }
        }
    }

    // ── Tool card click listeners ──────────────────────────────────────
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
            // TODO: navigate to QuizFragment
        }
        binding.toolProgress.setOnClickListener {
            // TODO: navigate to ProgressFragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}