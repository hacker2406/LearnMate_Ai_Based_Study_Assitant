package com.example.learnmate.ui.quiz

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.learnmate.databinding.ItemQuizOptionBinding

class OptionsAdapter(
    private val options: List<String>,
    private val onOptionClick: (Int) -> Unit
) : RecyclerView.Adapter<OptionsAdapter.OptionViewHolder>() {

    private var selectedIndex: Int? = null
    private var correctIndex: Int?  = null
    private var isAnswered = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemQuizOptionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(options[position], position)
    }

    override fun getItemCount() = options.size

    // ── Reveal answer colors ───────────────────────────────────────────
    fun revealAnswer(selected: Int, correct: Int) {
        selectedIndex = selected
        correctIndex  = correct
        isAnswered    = true
        notifyDataSetChanged()
    }

    // ── Reset for next question ────────────────────────────────────────
    fun reset() {
        selectedIndex = null
        correctIndex  = null
        isAnswered    = false
        notifyDataSetChanged()
    }

    inner class OptionViewHolder(
        private val binding: ItemQuizOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(option: String, index: Int) {
            binding.tvOption.text = option

            // ── Option letter (A/B/C/D) ────────────────────────────────
            binding.tvOptionLetter.text = when (index) {
                0 -> "A"
                1 -> "B"
                2 -> "C"
                else -> "D"
            }

            // ── Color based on answer state ────────────────────────────
            when {
                !isAnswered -> {
                    // Default state
                    binding.root.setCardBackgroundColor(Color.WHITE)
                    binding.tvOptionLetter.setBackgroundResource(
                        com.example.learnmate.R.drawable.bg_tab_container
                    )
                    binding.tvOptionLetter.setTextColor(
                        Color.parseColor("#7C3AED")
                    )
                    binding.tvOption.setTextColor(Color.parseColor("#1F1F2E"))
                }
                index == correctIndex -> {
                    // Correct answer — green
                    binding.root.setCardBackgroundColor(Color.parseColor("#F0FDF4"))
                    binding.tvOptionLetter.setBackgroundColor(
                        Color.parseColor("#16A34A")
                    )
                    binding.tvOptionLetter.setTextColor(Color.WHITE)
                    binding.tvOption.setTextColor(Color.parseColor("#16A34A"))
                }
                index == selectedIndex -> {
                    // Wrong selected answer — red
                    binding.root.setCardBackgroundColor(Color.parseColor("#FEF2F2"))
                    binding.tvOptionLetter.setBackgroundColor(
                        Color.parseColor("#DC2626")
                    )
                    binding.tvOptionLetter.setTextColor(Color.WHITE)
                    binding.tvOption.setTextColor(Color.parseColor("#DC2626"))
                }
                else -> {
                    // Other options — dimmed
                    binding.root.setCardBackgroundColor(Color.WHITE)
                    binding.tvOptionLetter.setBackgroundResource(
                        com.example.learnmate.R.drawable.bg_tab_container
                    )
                    binding.tvOptionLetter.setTextColor(
                        Color.parseColor("#9CA3AF")
                    )
                    binding.tvOption.setTextColor(Color.parseColor("#9CA3AF"))
                }
            }

            // ── Click only if not answered yet ─────────────────────────
            binding.root.setOnClickListener {
                if (!isAnswered) onOptionClick(index)
            }
        }
    }
}