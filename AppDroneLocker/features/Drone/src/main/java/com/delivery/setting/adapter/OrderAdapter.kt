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
import com.delivery.setting.databinding.ItemOrderBinding
import com.delivery.setting.model.Order
import com.delivery.setting.model.OrderDisplayStyle
import com.delivery.setting.model.buttonTitle
import com.delivery.setting.model.displayText

class OrderAdapter(
    private val onOrderClick: (Order) -> Unit,
    private val onActionClick: (Order,OrderDisplayStyle) -> Unit,
    private val currentLockerId: String = ""
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding, onOrderClick, onActionClick, currentLockerId)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        return count
    }

    fun updateOrders(orders: List<Order>) {
        submitList(orders.toList()) // Create new list to ensure diff detection
    }

    class OrderViewHolder(
        private val binding: ItemOrderBinding,
        private val onOrderClick: (Order) -> Unit,
        private val onActionClick: (Order, OrderDisplayStyle) -> Unit,
        private val currentLockerId: String
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(order: Order) {
            val displayStyle = order.getDisplayStyle(currentLockerId)
            
            // Set order ID
            binding.tvOrderId.text = "#${order.id.take(12)}"
            
            // Set drone type (simplified - you can enhance this based on your data)
            binding.tvDroneType.text = "Máy bay: ${order.segments.firstOrNull()?.droneId ?: "Không xác định"}"
            
            // Set size and weight
            binding.tvSizeWeight.text = "${order.getSizeText()} • ${order.weight} kg"
            
            // Set source and destination
            val sourceText = if (order.sourceName.isNotEmpty()) order.sourceName else order.source
            val destText = if (order.destName.isNotEmpty()) order.destName else order.dest
            binding.tvSourceDest.text = "Từ: $sourceText → Đến: $destText"
            
            // Set status and date using displayStyle
            binding.tvStatusDate.text = "${displayStyle.displayText} • ${order.getFormattedDate()}"
            
            // Set status text color based on displayStyle
            val statusColor = when (displayStyle) {
                com.delivery.setting.model.OrderDisplayStyle.SEND -> ContextCompat.getColor(binding.root.context, R.color.orange)
                com.delivery.setting.model.OrderDisplayStyle.SENT -> ContextCompat.getColor(binding.root.context, R.color.blue)
                com.delivery.setting.model.OrderDisplayStyle.WAITING -> ContextCompat.getColor(binding.root.context, R.color.orange)
                com.delivery.setting.model.OrderDisplayStyle.UNLOAD -> ContextCompat.getColor(binding.root.context, R.color.green)
                com.delivery.setting.model.OrderDisplayStyle.DONE -> ContextCompat.getColor(binding.root.context, R.color.gray)
            }
            binding.tvStatusDate.setTextColor(statusColor)
            
            // Set accent background (left border) based on displayStyle
            val accentBackground = when (displayStyle) {
                com.delivery.setting.model.OrderDisplayStyle.SEND -> R.drawable.bg_accent_send
                com.delivery.setting.model.OrderDisplayStyle.SENT -> R.drawable.bg_accent_sent
                com.delivery.setting.model.OrderDisplayStyle.WAITING -> R.drawable.bg_accent_waiting
                com.delivery.setting.model.OrderDisplayStyle.UNLOAD -> R.drawable.bg_accent_unload
                com.delivery.setting.model.OrderDisplayStyle.DONE -> R.drawable.bg_accent_done
            }
            binding.viewAccent.setBackgroundResource(accentBackground)
            
            // Set action button based on displayStyle
            val buttonText = displayStyle.buttonTitle
            if (displayStyle != com.delivery.setting.model.OrderDisplayStyle.DONE) {
                binding.btnAction.text = buttonText
                binding.btnAction.visibility = View.VISIBLE
                
                // Set button background and text color based on displayStyle
                when (displayStyle) {
                    com.delivery.setting.model.OrderDisplayStyle.SEND -> {
                        // Bấm được, màu cam
                        binding.btnAction.setBackgroundResource(R.drawable.bg_action_button_send)
                        binding.btnAction.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                        binding.btnAction.isEnabled = true
                        binding.btnAction.alpha = 1.0f
                    }
                    com.delivery.setting.model.OrderDisplayStyle.SENT -> {
                        // Không bấm được, màu cam
                        binding.btnAction.setBackgroundResource(R.drawable.bg_action_button_sent)
                        binding.btnAction.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                        binding.btnAction.isEnabled = false
                        binding.btnAction.alpha = 0.6f
                    }
                    com.delivery.setting.model.OrderDisplayStyle.WAITING -> {
                        // Làm mờ, không bấm được, màu xanh
                        binding.btnAction.setBackgroundResource(R.drawable.bg_action_button_waiting)
                        binding.btnAction.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                        binding.btnAction.isEnabled = false
                        binding.btnAction.alpha = 0.5f
                    }
                    com.delivery.setting.model.OrderDisplayStyle.UNLOAD -> {
                        // Bấm được, màu xanh
                        binding.btnAction.setBackgroundResource(R.drawable.bg_action_button_unload)
                        binding.btnAction.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
                        binding.btnAction.isEnabled = true
                        binding.btnAction.alpha = 1.0f
                    }
                    com.delivery.setting.model.OrderDisplayStyle.DONE -> {
                        // Không hiển thị nút
                        binding.btnAction.visibility = View.GONE
                    }
                }
                
                // Set click listener only for enabled buttons
                if (binding.btnAction.isEnabled) {
                    binding.btnAction.setOnClickListener {
                        onActionClick(order,displayStyle)
                    }
                } else {
                    binding.btnAction.setOnClickListener(null)
                }
            } else {
                binding.btnAction.visibility = View.GONE
            }
            
            // Set click listener for the entire card
            binding.root.setOnClickListener {
                onOrderClick(order)
            }
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }
    }
}
