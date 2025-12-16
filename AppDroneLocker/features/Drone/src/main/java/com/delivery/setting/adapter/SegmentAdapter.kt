package com.delivery.setting.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.delivery.setting.R
import com.delivery.setting.databinding.ItemSegmentBinding
import com.delivery.setting.model.Segment
import com.delivery.setting.model.SegmentCommandAction
import com.delivery.setting.model.SegmentDisplayStyle
import com.delivery.setting.model.displayText

class SegmentAdapter(
    private val onSegmentClick: (Segment) -> Unit,
    private val onActionClick: (Segment, SegmentCommandAction) -> Unit
) : ListAdapter<Segment, SegmentAdapter.SegmentViewHolder>(SegmentDiffCallback()) {

    private var currentLockerId: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SegmentViewHolder {
        val binding = ItemSegmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SegmentViewHolder(binding, onSegmentClick, onActionClick)
    }

    override fun onBindViewHolder(holder: SegmentViewHolder, position: Int) {
        val segment = getItem(position)
        holder.bind(segment, currentLockerId)
    }

    override fun getItemCount(): Int {
        return super.getItemCount()
    }

    fun updateSegments(segments: List<Segment>) {
        submitList(segments.toList()) // Create new list to ensure diff detection
    }

    fun updateCurrentLockerId(lockerId: String) {
        currentLockerId = lockerId
        notifyDataSetChanged()
    }

    class SegmentViewHolder(
        private val binding: ItemSegmentBinding,
        private val onSegmentClick: (Segment) -> Unit,
        private val onActionClick: (Segment, SegmentCommandAction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(segment: Segment, currentLockerId: String) {
            val displayStyle = segment.getDisplayStyle()

            // Set drone information
            binding.tvDroneName.text = segment.getDroneDisplayName()

            // Set departure and destination points
            val departureText =
                if (segment.sourceName.isNotEmpty()) segment.sourceName else segment.source
            val destinationText =
                if (segment.destName.isNotEmpty()) segment.destName else segment.dest

            binding.tvDeparturePoint.text = "Điểm cất cánh: $departureText"
            binding.tvLandingPoint.text = "Điểm hạ cánh: $destinationText"

            // Set status
            binding.tvStatus.text = "Trạng thái: ${
                segment.getStatusText()
            }"

            // Set start and end times
            val startTime = segment.getFormattedStartTime()
            val endTime = segment.getFormattedEndTime()

            binding.tvStartTime.text =
                "Bắt đầu: ${if (startTime.isNotEmpty()) startTime else "Chưa xác định"}"
            binding.tvEndTime.text =
                "Kết thúc: ${if (endTime.isNotEmpty()) endTime else "Chưa xác định"}"

            // Set status text color based on displayStyle
            val statusColor = segment.getColorHex()


            binding.tvStatus.setTextColor(ContextCompat.getColor(
                binding.root.context,
                statusColor
            ))

            // Configure Start Button
            configureStartButton(segment, segment.lockerId ?: "")

            // Configure End Button
            configureEndButton(segment, segment.lockerId ?: "")

            // Set click listener for the entire card
            binding.root.setOnClickListener {
                onSegmentClick(segment)
            }
        }

        private fun configureStartButton(segment: Segment, currentLockerId: String) {
            val segmentStatus = segment.getSegmentStatus()
            val isVisible = currentLockerId == segment.source
            val isEnabled = segmentStatus == com.delivery.setting.model.SegmentStatus.NONE

            binding.btnActionStart.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.btnActionStart.isEnabled = isEnabled
            binding.btnActionStart.alpha = if (isEnabled) 1.0f else 0.5f

            binding.btnActionStart.setOnClickListener {
                if (isEnabled) {
                    onActionClick(segment, SegmentCommandAction.START)
                }
            }
        }

        private fun configureEndButton(segment: Segment, currentLockerId: String) {
            val segmentStatus = segment.getSegmentStatus()
            val isVisible = currentLockerId == segment.dest
            val isEnabled = segmentStatus == com.delivery.setting.model.SegmentStatus.IN_PROGRESS

            binding.btnActionEnd.visibility = if (isVisible) View.VISIBLE else View.GONE
            binding.btnActionEnd.isEnabled = isEnabled
            binding.btnActionEnd.alpha = if (isEnabled) 1.0f else 0.5f

            binding.btnActionEnd.setOnClickListener {
                if (isEnabled) {
                    onActionClick(segment, SegmentCommandAction.END)
                }
            }
        }
    }

    class SegmentDiffCallback : DiffUtil.ItemCallback<Segment>() {
        override fun areItemsTheSame(oldItem: Segment, newItem: Segment): Boolean {
            return oldItem.segmentIndex == newItem.segmentIndex && oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: Segment, newItem: Segment): Boolean {
            return oldItem == newItem
        }
    }
}
