package com.delivery.uservht.ui.product

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.pref.RxPreferences
import com.delivery.setting.DemoNavigation
import com.delivery.vht.R
import com.delivery.vht.databinding.ProductListFragmentBinding
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProductListFragment :
    BaseFragment<ProductListFragmentBinding, ProductListViewModel>(R.layout.product_list_fragment) {

    @Inject
    lateinit var appNavigation: DemoNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private val viewModel: ProductListViewModel by viewModels()

    private lateinit var adapter: ProductAdapter

    // DS sản phẩm được chọn
    private val selectedProducts = mutableListOf<Product>()

    override fun getVM() = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        selectedProducts.clear()
        setupProductList()
        setupConfirmButton()
    }

    private fun setupProductList() {
        val fakeProducts = listOf(
            Product("CLASSIC_COCKTAIL", "Cocktail truyền thống", R.drawable.classic_cocktail),
            Product("BEET_COCKTAIL", "Cocktail củ dền", R.drawable.beet_coktail),
            Product("LEMON_COCKTAIL", "Cocktail chanh", R.drawable.lemon_cocktail),
            Product("RASPBERRY_LEMONADE_COCKTAIL", "Cocktail mâm xôi – chanh", R.drawable.raspberry_lemonade_cocktail),
            Product("COCKTAIL_TRIO", "Set 3 Cocktail", R.drawable.cocktail_trio),
            Product("ORANGE_JUICE", "Nước ép cam", R.drawable.orange_juice),
            Product("GUAVA_JUICE", "Nước ép ổi", R.drawable.guava_juice),
            Product("CAFE_HIGHLAND", "Cafe Highland", R.drawable.highland_coffee),
            Product("CAFE", "Cafe đen ", R.drawable.cafe),
            Product("HAMBURGER", "Hamburger", R.drawable.hamburger),
            Product("OAT", "Oatmeal", R.drawable.oatmeal),
            Product("PIZZA", "Pizza", R.drawable.pizza),
            Product("POTATO", "Potato", R.drawable.potato),
            Product("SNACK", "Snack", R.drawable.snack)
        )

        adapter = ProductAdapter(fakeProducts) { product, isSelected ->
            if (isSelected) selectedProducts.add(product)
            else selectedProducts.remove(product)

            binding.btnConfirm.isEnabled = selectedProducts.isNotEmpty()
            if (selectedProducts.isNotEmpty()) {
                binding.btnConfirm.visibility = View.VISIBLE
            } else {
                binding.btnConfirm.visibility = View.GONE
            }
        }

        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)

        // Set spacing
        val spacing = (10 * resources.displayMetrics.density).toInt()
        binding.rvProducts.addItemDecoration(
            GridSpacingItemDecoration(2, spacing, true)
        )

        binding.rvProducts.adapter = adapter
    }

    private fun setupConfirmButton() {
        binding.btnConfirm.setOnClickListener {
            lifecycleScope.launch {
                val gson = Gson()
                val json = gson.toJson(selectedProducts)
                rxPreferences.setProducts(json)
                // Điều hướng về HomeFragment (giống code cũ)
                navigationToHome()
            }
        }
    }

    private fun navigationToHome() {
        appNavigation.openCreateOrder()
        //findNavController().navigate(R.id.action_productListFragment_to_homeFragment)
    }
}