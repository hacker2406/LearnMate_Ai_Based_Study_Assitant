package com.example.learnmate.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentMainBinding
import com.example.learnmate.ui.home.HomeFragment
import com.example.learnmate.ui.notes.NotesFragment
import com.example.learnmate.ui.planner.PlannerFragment
import com.example.learnmate.ui.profile.ProfileFragment

class MainFragment : Fragment(R.layout.fragment_main) {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMainBinding.bind(view)

        loadFragment(HomeFragment())

        binding.navHome.setOnClickListener {
            loadFragment(HomeFragment())
        }

        binding.navNotes.setOnClickListener {
            loadFragment(NotesFragment())
        }

        binding.navPlanner.setOnClickListener {
            loadFragment(PlannerFragment())
        }

        binding.navProfile.setOnClickListener {
            loadFragment(ProfileFragment())
        }

        binding.navAi.setOnClickListener {
            // AI screen later
        }
    }

    private fun loadFragment(fragment: Fragment) {

        childFragmentManager.beginTransaction()
            .replace(R.id.mainFragmentContainer, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}