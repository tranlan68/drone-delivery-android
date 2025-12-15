package com.delivery.uservht.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.delivery.vht.databinding.ItemProductBinding

class ProductAdapter(
    private val products: List<Product>,
    private val onClick: (Product, Boolean) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductVH>() {

    private val selectedIds = mutableSetOf<String>()

    inner class ProductVH(val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.txtName.text = product.name
            binding.imgProduct.setImageResource(product.iconRes)

            // Update the selection state of the MaterialCardView
            binding.productCard.isSelected = selectedIds.contains(product.id)

            binding.root.setOnClickListener {
                it.isSelected = !it.isSelected
                val newState = !selectedIds.contains(product.id)

                if (newState) {
                    selectedIds.add(product.id)
                } else {
                    selectedIds.remove(product.id)
                }

                notifyItemChanged(adapterPosition)
                onClick(product, newState)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductVH {
        return ProductVH(
            ItemProductBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductVH, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size
}
