package com.cricscore.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cricscore.app.databinding.ItemRecentMatchBinding
import com.cricscore.app.domain.model.Match
import com.cricscore.app.domain.model.MatchStatus
import java.text.SimpleDateFormat
import java.util.*

class RecentMatchAdapter(
    private val onItemClick: (Match) -> Unit
) : ListAdapter<Match, RecentMatchAdapter.MatchViewHolder>(MatchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemRecentMatchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MatchViewHolder(private val binding: ItemRecentMatchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Match) {
            binding.tvTeams.text = "${match.team1} vs ${match.team2}"
            
            // Format Date
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDate.text = sdf.format(Date(match.createdAt))

            // Format Status/Result
            binding.tvResult.text = when (match.status) {
                MatchStatus.UPCOMING -> "Toss Pending"
                MatchStatus.FIRST_INNINGS -> "Live · 1st Innings in progress"
                MatchStatus.SECOND_INNINGS -> "Live · 2nd Innings in progress"
                MatchStatus.COMPLETED -> {
                    if (match.isTied) {
                        "Match Tied"
                    } else if (match.winnerTeam != null) {
                        val margin = if (match.winMarginRuns > 0) {
                            "${match.winMarginRuns} runs"
                        } else {
                            "${match.winMarginWickets} wickets"
                        }
                        "${match.winnerTeam} won by $margin"
                    } else {
                        "Match Completed"
                    }
                }
            }

            // Set color of status
            val context = binding.root.context
            val textColor = if (match.status == MatchStatus.COMPLETED) {
                context.getColor(com.cricscore.app.R.color.colorTextSecondary)
            } else {
                context.getColor(com.cricscore.app.R.color.colorPrimary)
            }
            binding.tvResult.setTextColor(textColor)

            binding.root.setOnClickListener {
                onItemClick(match)
            }
        }
    }

    class MatchDiffCallback : DiffUtil.ItemCallback<Match>() {
        override fun areItemsTheSame(oldItem: Match, newItem: Match): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Match, newItem: Match): Boolean {
            return oldItem == newItem
        }
    }
}
