package com.cricscore.app.ui.scorecard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cricscore.app.databinding.ItemScorecardBatsmanBinding
import com.cricscore.app.domain.model.BatsmanInnings

class ScorecardBatsmanAdapter :
    ListAdapter<BatsmanInnings, ScorecardBatsmanAdapter.BatsmanViewHolder>(BatsmanDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BatsmanViewHolder {
        val binding = ItemScorecardBatsmanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BatsmanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BatsmanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BatsmanViewHolder(private val binding: ItemScorecardBatsmanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(batsman: BatsmanInnings) {
            binding.tvName.text = batsman.playerName
            
            binding.tvStatus.text = if (batsman.isOut) {
                batsman.dismissalDescription ?: "out"
            } else {
                if (batsman.balls == 0 && batsman.runs == 0) {
                    "yet to bat"
                } else {
                    "not out"
                }
            }

            binding.tvRuns.text = batsman.runs.toString()
            binding.tvBalls.text = batsman.balls.toString()
            binding.tvFours.text = batsman.fours.toString()
            binding.tvSixes.text = batsman.sixes.toString()
            binding.tvSr.text = String.format(java.util.Locale.US, "%.1f", com.cricscore.app.core.util.CricketCalculator.calculateStrikeRate(batsman.runs, batsman.balls))
        }
    }

    class BatsmanDiffCallback : DiffUtil.ItemCallback<BatsmanInnings>() {
        override fun areItemsTheSame(oldItem: BatsmanInnings, newItem: BatsmanInnings): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BatsmanInnings, newItem: BatsmanInnings): Boolean {
            return oldItem == newItem
        }
    }
}
