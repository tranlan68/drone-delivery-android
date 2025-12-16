package com.delivery.setting.ui.location

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.setOnSafeClickListener
import com.delivery.permission.PermissionListener
import com.delivery.permission.PermissionStatus
import com.delivery.permission.requestPermission
import com.delivery.setting.R
import com.delivery.setting.adapter.SearchResultAdapter
import com.delivery.setting.databinding.FragmentLocationSearchBinding
import com.delivery.setting.model.SearchResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LocationSearchFragment :
    BaseFragment<FragmentLocationSearchBinding, LocationSearchViewModel>(R.layout.fragment_location_search),
    PermissionListener {

    private var searchResultAdapter: SearchResultAdapter? = null
    private val viewModel: LocationSearchViewModel by viewModels()

    override fun getVM() = viewModel

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        setupRecyclerView()
        setupSearchInput()
    }

    private fun setupRecyclerView() {
        searchResultAdapter = SearchResultAdapter { searchResult ->
            // Handle search result selection
            viewModel.selectLocation(searchResult)
            // Navigate back with result
            requireActivity().onBackPressed()
        }

        binding.recyclerSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchResultAdapter
        }
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    private fun setupSearchInput() {
        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val query = binding.etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.searchLocations(query)
            }
            false
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchResults.collect { results ->
                    searchResultAdapter?.submitList(results)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
                    binding.progressBar.visibility = if (isLoading) {
                        android.view.View.VISIBLE
                    } else {
                        android.view.View.GONE
                    }
                }
            }
        }
    }

    override fun setOnClick() {
        super.setOnClick()

        binding.ivBack.setOnSafeClickListener {
            requireActivity().onBackPressed()
        }

        binding.layoutCurrentLocation.setOnSafeClickListener {
            requestPermission(LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    override fun onPermissionGranted(requestCode: Int?) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            viewModel.getCurrentLocation()
        }
    }

    override fun onPermissionDenied(
        requestCode: Int?,
        permissions: List<PermissionStatus>,
        isDoNotAskAgain: Boolean
    ) {
        // Handle permission denied
    }

    override fun onDestroyView() {
        searchResultAdapter = null
        super.onDestroyView()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
