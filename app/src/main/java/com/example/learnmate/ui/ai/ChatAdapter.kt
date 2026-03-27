package com.example.learnmate.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.learnmate.data.model.ChatMessage
import com.example.learnmate.data.model.MessageType
import com.example.learnmate.databinding.ItemMessageBotBinding
import com.example.learnmate.databinding.ItemMessageUserBinding

class ChatAdapter : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT  = 2
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_USER) {
            UserViewHolder(
                ItemMessageUserBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        } else {
            BotViewHolder(
                ItemMessageBotBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is BotViewHolder  -> holder.bind(message)
        }
    }

    // ── User message ViewHolder ────────────────────────────────────────
    inner class UserViewHolder(
        private val binding: ItemMessageUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvUserMessage.text = message.text
        }
    }

    // ── Bot message ViewHolder ─────────────────────────────────────────
    inner class BotViewHolder(
        private val binding: ItemMessageBotBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvBotMessage.text = message.text
            // Show special icon based on message type
            binding.tvBotIcon.text = when (message.type) {
                MessageType.SUMMARY    -> "📝"
                MessageType.QUIZ       -> "🧠"
                MessageType.STUDY_PLAN -> "📚"
                else                   -> "🤖"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem == newItem
    }
}