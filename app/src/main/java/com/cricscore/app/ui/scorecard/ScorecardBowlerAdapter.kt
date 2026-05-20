package com.cricscore.app.ui.scorecard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cricscore.app.databinding.ItemScorecardBowlerBinding
import com.cricscore.app.domain.model.BowlerInnings

class ScorecardBowlerAdapter :
    ListAdapter<BowlerInnings, ScorecardBowlerAdapter.BowlerViewHolder>(BowlerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BowlerViewHolder {
        val binding = ItemScorecardBowlerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BowlerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BowlerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BowlerViewHolder(private val binding: ItemScorecardBowlerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bowler: BowlerInnings) {
            binding.tvName.text = bowler.playerName
            binding.tvOvers.text = com.cricscore.app.core.util.CricketCalculator.ballsToOversString(bowler.ballsBowled)
            binding.tvMaidens.text = bowler.maidens.toString()
            binding.tvRuns.text = bowler.runsConceded.toString()
            binding.tvWickets.text = bowler.wickets.toString()
            binding.tvEcon.text = String.format(java.util.Locale.US, "%.2f", com.cricscore.app.core.util.CricketCalculator.calculateEconomyRate(bowler.runsConceded, bowler.ballsBowled))
        }
    }

    class BowlerDiffCallback : DiffUtil.ItemCallback<BowlerInnings>() {
        override fun areItemsTheSame(oldItem: BowlerInnings, newItem: BowlerInnings): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BowlerInnings, newItem: BowlerInnings): Boolean {
            return oldItem == newItem
        }
    }
}
