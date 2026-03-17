package com.example.learnmate.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        loadUserName()
    }

    private fun loadUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        Firebase.firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val fullName = snapshot.getString("name").orEmpty().trim()
                if (fullName.isNotEmpty()) {
                    binding.tvUserName.text = fullName.substringBefore(" ")
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
