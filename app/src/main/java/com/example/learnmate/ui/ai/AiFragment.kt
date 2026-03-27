package com.example.learnmate.ui.ai

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learnmate.R
import com.example.learnmate.databinding.FragmentAiBinding

class AiFragment : Fragment(R.layout.fragment_ai) {

    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AiViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAiBinding.bind(view)

        setupRecyclerView()
        setupModeSelector()
        setupInput()
        observeMessages()
        observeLoading()

        // Show welcome message on first open
        if (viewModel.messages.value.isNullOrEmpty()) {
            viewModel.setMode(AiMode.CHAT)
        }
    }

    // ── RecyclerView ───────────────────────────────────────────────────
    private fun setupRecyclerView() {
        adapter = ChatAdapter()
        val layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvChat.layoutManager = layoutManager
        binding.rvChat.adapter = adapter
    }

    // ── Mode selector ──────────────────────────────────────────────────
    private fun setupModeSelector() {
        val modes = mapOf(
            AiMode.CHAT       to binding.modeChat,
            AiMode.SUMMARIZE  to binding.modeSummarize,
            AiMode.QUIZ       to binding.modeQuiz,
            AiMode.STUDY_PLAN to binding.modeStudyPlan
        )

        modes.forEach { (mode, view) ->
            view.setOnClickListener {
                viewModel.setMode(mode)
                updateModeUI(modes, mode)
                updateHint(mode)
            }
        }
    }

    private fun updateModeUI(
        modes: Map<AiMode, TextView>,
        active: AiMode
    ) {
        modes.forEach { (mode, view) ->
            if (mode == active) {
                view.setBackgroundResource(R.drawable.bg_tab_active)
                view.setTextColor(Color.WHITE)
            } else {
                view.setBackgroundResource(R.drawable.bg_stat_chip)
                view.setTextColor(Color.parseColor("#CCEEFF"))
            }
        }
    }

    private fun updateHint(mode: AiMode) {
        binding.etMessage.hint = when (mode) {
            AiMode.CHAT       -> "Ask anything..."
            AiMode.SUMMARIZE  -> "Paste text to summarize..."
            AiMode.QUIZ       -> "Enter a topic for quiz (e.g. Photosynthesis)..."
            AiMode.STUDY_PLAN -> "Enter subject to study (e.g. Data Structures)..."
        }
    }

    // ── Input + Send ───────────────────────────────────────────────────
    private fun setupInput() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.sendMessage(message)
                binding.etMessage.setText("")
                hideKeyboard()
            }
        }

        binding.btnClearChat.setOnClickListener {
            viewModel.clearChat()
            viewModel.setMode(AiMode.CHAT)
        }
    }

    // ── Observe messages ───────────────────────────────────────────────
    private fun observeMessages() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages.toList()) {
                // Scroll to bottom after new message
                if (messages.isNotEmpty()) {
                    binding.rvChat.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
    }

    // ── Observe loading ────────────────────────────────────────────────
    private fun observeLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingIndicator.visibility =
                if (isLoading) View.VISIBLE else View.GONE
            binding.btnSend.isEnabled = !isLoading
            binding.btnSend.alpha = if (isLoading) 0.5f else 1f
        }
    }

    // ── Hide keyboard ──────────────────────────────────────────────────
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(
            android.content.Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etMessage.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
