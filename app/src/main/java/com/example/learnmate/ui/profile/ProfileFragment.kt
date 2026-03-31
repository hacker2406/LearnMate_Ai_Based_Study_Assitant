package com.example.learnmate.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        loadUserInfo()
        observeProfileData()
        setupButtons()
    }

    // ── Load name + email from Firebase Auth ───────────────────────────
    private fun loadUserInfo() {
        val user = FirebaseAuth.getInstance().currentUser ?: return

        binding.tvProfileEmail.text = user.email ?: ""

        com.google.firebase.ktx.Firebase.firestore
            .collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (_binding == null) return@addOnSuccessListener
                val name = snapshot.getString("name").orEmpty().trim()
                if (name.isNotEmpty()) {
                    binding.tvProfileName.text  = name
                    binding.tvAvatarLetter.text = name.first().uppercaseChar().toString()
                } else {
                    binding.tvProfileName.text  = "Learner"
                    binding.tvAvatarLetter.text = "L"
                }
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                binding.tvProfileName.text  = "Learner"
                binding.tvAvatarLetter.text = "L"
            }
    }
    // ── Observe live profile data ──────────────────────────────────────
    private fun observeProfileData() {
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            val stats = data.stats

            // Stat chips
            binding.tvProfileXP.text     = "${stats.xp}"
            binding.tvProfileStreak.text = "${stats.streak}"
            binding.tvProfileLevel.text  = "${stats.level}"

            // Level badge + label
            binding.tvProfileLevelBadge.text  =
                "🏅 Level ${stats.level} — ${stats.levelTitle}"
            binding.tvProfileLevelLabel.text  =
                "Level ${stats.level} — ${stats.levelTitle}"

            // XP progress bar
            val xpForNext    = stats.level * 200
            val xpInLevel    = stats.xp - ((stats.level - 1) * 200)
            val progressPct  = if (xpForNext > 0)
                ((xpInLevel.toFloat() / 200f) * 100).toInt() else 0

            binding.tvProfileXPProgress.text   = "${stats.xp} / $xpForNext XP"
            binding.profileXpProgressBar.progress = progressPct.coerceIn(0, 100)

            // Activity summary
            binding.tvTotalNotes.text     = "${data.totalNotes}"
            binding.tvTotalTasksDone.text = "${data.totalTasksDone}"

            val hours   = stats.totalStudyMinutes / 60
            val minutes = stats.totalStudyMinutes % 60
            binding.tvTotalStudyTime.text = "${hours}h ${minutes}m"
        }
    }

    // ── Buttons ────────────────────────────────────────────────────────
    private fun setupButtons() {
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_auth_to_main.let {
                R.id.authFragment
            })
            // Navigate back to auth
            requireActivity().finish()
            requireActivity().startActivity(
                requireActivity().intent.also {
                    it.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )
        }

        binding.settingNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "Coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.settingAbout.setOnClickListener {
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(
                    "https://hacker2406.github.io/learnmate-privacy/"
                )
            )
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}