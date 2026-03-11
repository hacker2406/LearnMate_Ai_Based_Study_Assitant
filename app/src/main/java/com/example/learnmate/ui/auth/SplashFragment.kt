package com.example.learnmate.ui.auth

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.fragment.app.Fragment
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentSplashBinding
import androidx.navigation.fragment.findNavController

class SplashFragment : Fragment(R.layout.fragment_splash) {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val animators = mutableListOf<ObjectAnimator>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSplashBinding.bind(view)

        applyGradientText()
        animateLogo()
        animateTextFadeIn()
        animateDots()

        view.postDelayed({
            findNavController().navigate(R.id.authFragment)
        }, 4000)
    }

    private fun applyGradientText() {
        binding.tvAppName.post {
            val width = binding.tvAppName.paint.measureText(
                binding.tvAppName.text.toString()
            )
            binding.tvAppName.paint.shader = LinearGradient(
                0f, 0f,
                width, binding.tvAppName.textSize,
                intArrayOf(
                    Color.parseColor("#7C3AED"),
                    Color.parseColor("#2563EB")
                ),
                null,
                Shader.TileMode.CLAMP
            )
            binding.tvAppName.invalidate()
        }
    }

    private fun animateLogo() {
        val interpolator = AccelerateDecelerateInterpolator()
        val pulse = ObjectAnimator.ofPropertyValuesHolder(
            binding.ivLogo,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.07f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.07f, 1f)
        ).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            setInterpolator(interpolator)
        }
        pulse.start()
        animators.add(pulse)
    }

    private fun animateDots() {
        val dots = listOf(binding.dot1, binding.dot2, binding.dot3)

        dots.forEachIndexed { index, view ->
            val animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0.2f, 1f, 0.2f).apply {
                duration = 900
                repeatCount = ObjectAnimator.INFINITE
                startDelay = (index * 200).toLong()
            }
            animator.start()
            animators.add(animator)
        }
    }

    private fun animateTextFadeIn() {
        val interpolator = AccelerateDecelerateInterpolator()
        listOf(
            Pair(binding.tvWelcome,  300L),
            Pair(binding.tvAppName,  700L),
            Pair(binding.tvTagline, 1100L)
        ).forEach { (textView, delay) ->
            textView.alpha = 0f
            textView.animate()
                .alpha(1f)
                .setDuration(900)
                .setStartDelay(delay)
                .setInterpolator(interpolator)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animators.forEach { it.cancel() }
        animators.clear()
        _binding = null
    }
}