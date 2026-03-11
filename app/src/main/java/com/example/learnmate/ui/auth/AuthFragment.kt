package com.example.learnmate.ui.auth

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentAuthBinding

class AuthFragment : Fragment(R.layout.fragment_auth) {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    private var isLoginActive = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAuthBinding.bind(view)

        animateEntrance()
        applyBrandGradient()
        setupToggle()
        setupButtons()
        observeAuthState()
    }

    // ---------------------------------------------------
    // ANIMATIONS
    // ---------------------------------------------------

    private fun animateEntrance() {

        binding.authCard.translationY = 100f
        binding.authCard.alpha = 0f
        binding.authCard.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(550)
            .setStartDelay(150)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        ObjectAnimator.ofPropertyValuesHolder(
            binding.ivLogo,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.07f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.07f, 1f)
        ).apply {
            duration = 2200
            repeatCount = ObjectAnimator.INFINITE
            setInterpolator(AccelerateDecelerateInterpolator())
            start()
        }
    }

    private fun applyBrandGradient() {
        binding.tvBrandName.post {
            val width = binding.tvBrandName.paint
                .measureText(binding.tvBrandName.text.toString())

            binding.tvBrandName.paint.shader = LinearGradient(
                0f, 0f, width, binding.tvBrandName.textSize,
                intArrayOf(
                    Color.parseColor("#C4B5FD"),
                    Color.parseColor("#93C5FD")
                ),
                null,
                Shader.TileMode.CLAMP
            )
            binding.tvBrandName.invalidate()
        }
    }

    // ---------------------------------------------------
    // TOGGLE
    // ---------------------------------------------------

    private fun setupToggle() {

        binding.btnLogin.setOnClickListener {
            if (!isLoginActive) switchTo(true)
        }

        binding.btnRegister.setOnClickListener {
            if (isLoginActive) switchTo(false)
        }
    }

    private fun switchTo(login: Boolean) {

        isLoginActive = login

        val showForm = if (login) binding.loginForm else binding.registerForm
        val hideForm = if (login) binding.registerForm else binding.loginForm
        val activeTab = if (login) binding.btnLogin else binding.btnRegister
        val inactiveTab = if (login) binding.btnRegister else binding.btnLogin

        activeTab.setBackgroundResource(R.drawable.bg_tab_active)
        activeTab.setTextColor(Color.WHITE)

        inactiveTab.setBackgroundColor(Color.TRANSPARENT)
        inactiveTab.setTextColor(Color.parseColor("#9CA3AF"))

        hideForm.visibility = View.GONE
        showForm.visibility = View.VISIBLE
    }

    // ---------------------------------------------------
    // BUTTONS
    // ---------------------------------------------------

    private fun setupButtons() {

        binding.btnSignIn.setOnClickListener {
            loginUser()
        }

        binding.btnCreateAccount.setOnClickListener {
            registerUser()
        }
    }

    private fun loginUser() {

        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        viewModel.login(email, password)
    }

    private fun registerUser() {

        val name = binding.etRegisterName.text.toString().trim()
        val email = binding.etRegisterEmail.text.toString().trim()
        val password = binding.etRegisterPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.registerNameLayout.error = "Full name required"
            return
        } else {
            binding.registerNameLayout.error = null
        }

        if (!validateInput(email, password)) return

        viewModel.register(name, email, password)
    }

    private fun validateInput(email: String, password: String): Boolean {

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Email required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password.length < 6) {
            Toast.makeText(requireContext(), "Password must be 6+ characters", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // ---------------------------------------------------
    // OBSERVER
    // ---------------------------------------------------

    private fun observeAuthState() {

        viewModel.authState.observe(viewLifecycleOwner) { state ->

            val activeButton = if (isLoginActive) {
                binding.btnSignIn
            } else {
                binding.btnCreateAccount
            }

            when (state) {

                is AuthState.Loading -> {
                    activeButton.isEnabled = false
                    activeButton.text = "Please wait..."
                }

                is AuthState.Success -> {
                    activeButton.isEnabled = true
                    activeButton.text =
                        if (isLoginActive) "Sign In →" else "Create Account →"

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_SHORT
                    ).show()

                    // TODO: Navigate to home screen next
                    findNavController().navigate(R.id.action_auth_to_main)
                }

                is AuthState.Error -> {
                    activeButton.isEnabled = true
                    activeButton.text =
                        if (isLoginActive) "Sign In →" else "Create Account →"

                    Toast.makeText(
                        requireContext(),
                        state.message,
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}