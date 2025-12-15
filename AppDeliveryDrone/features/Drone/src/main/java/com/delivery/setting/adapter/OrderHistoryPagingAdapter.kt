package com.delivery.setting.adapter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.delivery.setting.R
import com.delivery.setting.databinding.ItemOrderHistoryBinding
import com.delivery.setting.model.OrderHistoryItem
import com.delivery.setting.model.OrderStatus
import com.delivery.setting.ui.createorder.DeliveryPriority
import com.delivery.setting.ui.createorder.Product
import org.maplibre.android.style.expressions.Expression.ceil
import kotlin.math.ceil

val Int.dp: Int get() =
    (this * Resources.getSystem().displayMetrics.density).toInt()
val Int.dpF: Float
    get() = this * Resources.getSystem().displayMetrics.density
class OrderHistoryPagingAdapter(
    private var productsMap: Map<String, List<Product>>,
    private val onItemClick: (OrderHistoryItem) -> Unit,
) : PagingDataAdapter<OrderHistoryItem, OrderHistoryPagingAdapter.OrderHistoryViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): OrderHistoryViewHolder {
        val binding =
            ItemOrderHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return OrderHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val updated = payloads[0] as OrderHistoryItem
            holder.bind(updated)   // update UI nhanh, không redraw toàn item
            return
        }

        // nếu không có payload, bind đầy đủ item
        val item = getItem(position)
        if (item != null) holder.bind(item)
    }


    override fun onBindViewHolder(
        holder: OrderHistoryViewHolder,
        position: Int,
    ) {
        val item = getItem(position)
        item?.let { holder.bind(it) }
    }

    /*@SuppressLint("NotifyDataSetChanged")
    fun updateProductsMap(newMap: Map<String, List<Product>>) {
        productsMap = newMap
        notifyDataSetChanged()
    }*/
    fun updateProductsMap(newMap: Map<String, List<Product>>) {
        val oldMap = productsMap
        productsMap = newMap

        val snapshot = snapshot()

        snapshot.items.forEachIndexed { index, oldItem ->
            val oldProducts = oldMap[oldItem.id]
            val newProducts = newMap[oldItem.id]

            if (oldProducts != newProducts) {
                notifyItemChanged(index)
            }
        }
    }

    fun applyOrderUpdates(updatedOrders: Map<String, OrderHistoryItem>) {
        val snapshot = snapshot()
        snapshot.items.forEachIndexed { index, oldItem ->
            val newItem = updatedOrders[oldItem.id]
            if (newItem != null && newItem != oldItem) {
                /*snapshot.items[index] = newItem
                notifyItemChanged(index)*/

                notifyItemChanged(index, newItem)
            }
        }
    }

    inner class OrderHistoryViewHolder(
        private val binding: ItemOrderHistoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val item = getItem(adapterPosition)
                if (item != null && item.status != OrderStatus.DELIVERED /*&& item.status != OrderStatus.CANCEL*/) {
                    onItemClick(item)
                }
            }
        }

        fun bind(item: OrderHistoryItem) {
            binding.apply {
                tvOrderId.text = "#" + item.intId
                tvFromLocation.text = item.sourceLocker
                //tvFromAddress.text = "" // Không cần hiển thị địa chỉ nữa vì đã có tên locker
                tvToLocation.text = item.destLocker
                //tvToAddress.text = "" // Không cần hiển thị địa chỉ nữa vì đã có tên locker
                tvPackageType.text = item.packageType
                tvPackageWeight.text = item.packageWeight
                tvOrderStatus.text = getStatusText(item.status)
                tvOderDate.text = formatOrderDate(item.orderDate, item.orderTime)

                tvOrderId.setTextColor(binding.root.context.getColor(android.R.color.holo_red_dark))
                //tvOrderId.setTextSize(18.0f)
                if (item.status == OrderStatus.DELIVERED) {
                    tvExpectedDate.text = "Đã giao: ${formatOrderDate(item.deliveryDate, item.deliveryTime)}"
                } else if (item.status == OrderStatus.PENDING) {
                    //tvExpectedDate.text = ""
                    tvExpectedDate.text = ""
                } else if (item.status == OrderStatus.CANCEL) {
                    tvExpectedDate.text = "Đang tạm dừng"
                } else {
                    //tvExpectedDate.text = "Dự kiến: ${formatOrderDate(item.deliveryDate, item.deliveryTime)}"
                    tvExpectedDate.text = "Giao trong: ${kotlin.math.ceil((item.eta ?: 300L).toDouble()/60.0).toInt()} phút"
                }

                // Set status color based on order status
                val statusColor =
                    when (item.status) {
                        OrderStatus.PENDING -> android.R.color.holo_blue_dark
                        OrderStatus.CONFIRMED -> android.R.color.holo_orange_light
                        OrderStatus.IN_DELIVERY -> android.R.color.holo_orange_dark
                        OrderStatus.DELIVERED -> android.R.color.holo_green_dark
                        OrderStatus.CANCEL -> android.R.color.holo_red_dark
                    }
                tvOrderStatus.setTextColor(binding.root.context.getColor(statusColor))
                tvExpectedDate.setTextColor(binding.root.context.getColor(statusColor))

                if (item.status == OrderStatus.CANCEL) {
                    tvOrderStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_dark))
                }

                when (item.priority) {
                    DeliveryPriority.STANDARD.value -> {
                        binding.tvProperty.text = binding.root.context.getString(com.delivery.core.R.string.string_standard)
                    }
                    DeliveryPriority.EXPRESS.value -> {
                        binding.tvProperty.text = binding.root.context.getString(com.delivery.core.R.string.string_express)
                    }
                }

                // 2️⃣ RENDER SẢN PHẨM
                val products = productsMap[item.id] ?: emptyList()
                renderSelectedProducts(products)

                //startMovingDotAnimation()
                startGrabStyleRouteAnimation()
            }
        }

        private fun renderSelectedProducts(products: List<Product>) {
            val grid = binding.glSelectedProducts
            grid.removeAllViews()

            val inflater = LayoutInflater.from(binding.root.context)

            products.forEach { product ->
                val itemView = inflater.inflate(
                    R.layout.item_selected_product_vertical,
                    grid,
                    false
                )

                itemView.findViewById<ImageView>(R.id.ivIcon)
                    .setImageResource(product.iconRes)

                itemView.findViewById<TextView>(R.id.tvName)
                    .text = product.name

                grid.addView(itemView)
            }
        }


        private fun getStatusText(status: OrderStatus): String {
            if (status == OrderStatus.CANCEL) { // FOR DEMO
                return "Đang giao"
            }
            return status.displayName
        }

        private fun formatOrderDate(
            date: String,
            time: String?,
        ): String {
            return if (time != null) {
                "$date • $time"
            } else {
                date
            }
        }

        private fun startGrabStyleRouteAnimation() {
            val movingDot = binding.viewMovingDot
            val trailContainer = binding.trailContainer

            movingDot.post {

                val dotsCount = 6
                val dotSizePx = 7.dp
                val containerWidth = trailContainer.width.toFloat()

                val spacing = (containerWidth - dotSizePx) / (dotsCount - 1)


                // reset container
                trailContainer.removeAllViews()
                val dots = mutableListOf<View>()

                // TẠO TOÀN BỘ CHẤM TRẮNG
                val centerY = trailContainer.height / 2f - dotSizePx / 2f

                for (i in 0 until dotsCount) {
                    val dot = View(trailContainer.context).apply {
                        layoutParams = FrameLayout.LayoutParams(dotSizePx.toInt(), dotSizePx.toInt())
                        x = i * spacing
                        y = centerY
                        background = ContextCompat.getDrawable(context, R.drawable.bg_dot_future)
                        alpha = 1f
                    }
                    trailContainer.addView(dot)
                    dots.add(dot)
                }

                fun animateRoute() {
                    movingDot.translationX = 0f
                    movingDot.alpha = 1f

                    val animator = ObjectAnimator.ofFloat(movingDot, "translationX", 0f, containerWidth)
                    animator.duration = 5_000
                    animator.interpolator = LinearInterpolator()

                    animator.addUpdateListener { anim ->

                        val currentX = anim.animatedValue as Float
                        val currentIndex = (currentX / spacing).toInt().coerceIn(0, dotsCount)

                        dots.forEachIndexed { index, dot ->

                            dot.background = when {
                                index < currentIndex ->
                                    ContextCompat.getDrawable(binding.root.context, R.drawable.bg_dot_passed) // Đen

                                index == currentIndex ->
                                    ContextCompat.getDrawable(binding.root.context, R.drawable.bg_dot_current) // Xanh

                                else ->
                                    ContextCompat.getDrawable(binding.root.context, R.drawable.bg_dot_future) // Trắng
                            }
                        }
                    }

                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            movingDot.alpha = 0f
                            animateRoute() // loop lại
                        }
                    })

                    animator.start()
                }

                animateRoute()
            }
        }


        private fun startMovingDotAnimation() {
            val dot = binding.viewMovingDot
            val trailContainer = binding.trailContainer

            // Lấy vị trí start → end
            dot.post {
                val startX = 0f
                val endX = trailContainer.width.toFloat()

                fun animate15s() {
                    dot.translationX = startX
                    dot.alpha = 1f
                    trailContainer.removeAllViews() // reset trail

                    val animator = ObjectAnimator.ofFloat(dot, "translationX", startX, endX)
                    animator.duration = 6_000 // 10 giây
                    animator.interpolator = LinearInterpolator()

                    animator.addUpdateListener { anim ->
                        val currentX = anim.animatedValue as Float
                        addTrailDot(currentX)
                    }

                    animator.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            // Reset để chạy lại từ đầu
                            dot.alpha = 0f
                            animate15s()
                        }
                    })

                    animator.start()
                }

                animate15s()
            }
        }

        private fun addTrailDot(xPos: Float) {
            val trailContainer = binding.trailContainer
            val dot = View(binding.root.context).apply {
                background = ContextCompat.getDrawable(binding.root.context, R.drawable.bg_circle_trail)
                layoutParams = FrameLayout.LayoutParams(6.dp, 6.dp)
                x = xPos
                y = (trailContainer.height / 2 - 3.dp).toFloat()
                alpha = 0f
            }

            trailContainer.addView(dot)

            dot.animate()
                .alpha(1f)
                .setDuration(150)
                .withEndAction {
                    dot.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction {
                            trailContainer.removeView(dot)
                        }
                }
                .start()
        }

    }

    companion object {
        private val DiffCallback =
            object : DiffUtil.ItemCallback<OrderHistoryItem>() {
                override fun areItemsTheSame(
                    oldItem: OrderHistoryItem,
                    newItem: OrderHistoryItem,
                ): Boolean {
                    return oldItem.id == newItem.id
                }

                override fun areContentsTheSame(
                    oldItem: OrderHistoryItem,
                    newItem: OrderHistoryItem,
                ): Boolean {
                    return oldItem == newItem
                }
            }
    }
}
