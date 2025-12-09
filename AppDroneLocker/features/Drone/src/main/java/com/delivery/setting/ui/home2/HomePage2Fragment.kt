package com.delivery.setting.ui.home2

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.pref.RxPreferences
import com.delivery.core.utils.Constants
import com.delivery.core.utils.toast
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.adapter.SegmentAdapter
import com.delivery.setting.databinding.FragmentHomePage2Binding
import com.delivery.setting.model.Segment
import com.delivery.setting.model.SegmentCommandAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomePage2Fragment :
    BaseFragment<FragmentHomePage2Binding, HomePage2ViewModel>(R.layout.fragment_home_page2) {

    @Inject
    lateinit var appNavigation: DemoNavigation

    @Inject
    lateinit var rxPreferences: RxPreferences

    private var segmentAdapter: SegmentAdapter? = null
    private var currentLockerId: String = ""

    private val viewModel: HomePage2ViewModel by viewModels()

    override fun getVM(): HomePage2ViewModel = viewModel

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.viewModel = viewModel
        initializeCurrentLockerId()
        setupRecyclerView()
        viewModel.handleEvent(HomePage2Event.LoadData)
    }

    private fun setupRecyclerView() {
        segmentAdapter = SegmentAdapter(
            onSegmentClick = { segment ->
               // handleSegmentClick(segment)
            },
            onActionClick = { segment, segmentCommand ->
                handleSegmentActionClick(segment, segmentCommand)
            }
        )

        binding.recyclerViewSegments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = segmentAdapter
        }
    }

    private fun setupObservers() {
        observeUiState()
        observeSegments()
        observeLoadingState()
        observeUiEvents()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    handleViewState(state)
                }
            }
        }
    }

    private fun observeSegments() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.segments.collect { segments ->
                    Timber.d("Collected ${segments.size} segments")
                    updateSegmentsList(segments)
                }
            }
        }
    }

    private fun observeLoadingState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateLoadingState(state.isLoading)
                }
            }
        }
    }

    private fun observeUiEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEvent.collect { event ->
                    handleViewEvent(event)
                }
            }
        }
    }

    override fun bindingStateView() {
        super.bindingStateView()
        setupObservers()
    }

    override fun setOnClick() {
        super.setOnClick()
        // No additional click listeners needed for this fragment
    }

    private fun handleViewState(state: HomePage2ViewState) {
        handleErrorState(state.error)
        updateSegmentsTitle(state.pendingSegmentsCount)
    }

    private fun handleErrorState(error: String?) {
        if (error != null) {
            error.toast(requireContext())
        }
    }

    private fun handleViewEvent(event: HomePage2ViewEvent) {
        when (event) {
            is HomePage2ViewEvent.ShowMessage -> {
                showToastMessage(event.message)
            }
            is HomePage2ViewEvent.ShowMessageRes -> {
                showToastMessageRes(event.messageResId, event.args)
            }
            is HomePage2ViewEvent.NavigateToSegmentDetail -> {
                navigateToSegmentDetail(event.segmentId)
            }
        }
    }

    private fun showToastMessage(message: String) {
        message.toast(requireContext())
    }

    private fun showToastMessageRes(messageResId: Int, args: Any?) {
        val message = if (args != null) {
            getString(messageResId, args)
        } else {
            getString(messageResId)
        }
        message.toast(requireContext())
    }

    private fun navigateToSegmentDetail(segmentId: String) {
        "Xem chi tiết segment: $segmentId".toast(requireContext())
    }

    private fun updateSegmentsList(segments: List<Segment>) {
        segmentAdapter?.updateSegments(segments)
        updateEmptyStateVisibility(segments.isEmpty())
    }

    private fun updateEmptyStateVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            showSegmentsEmptyState()
        } else {
            hideSegmentsEmptyState()
        }
    }

    private fun showSegmentsEmptyState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.recyclerViewSegments.visibility = View.GONE
    }

    private fun hideSegmentsEmptyState() {
        binding.layoutEmptyState.visibility = View.GONE
        binding.recyclerViewSegments.visibility = View.VISIBLE
    }

    private fun updateSegmentsTitle(pendingCount: Int) {
        val title = getString(R.string.pending_segments_title, pendingCount)
        binding.tvSegmentsTitle.text = title
    }

    private fun initializeCurrentLockerId() {
        lifecycleScope.launch {
            try {
                currentLockerId = rxPreferences.getSelectedLockerId().first() ?: ""
            } catch (e: Exception) {
                currentLockerId = ""
                Timber.e(e, "Failed to load current locker ID")
            }
        }
    }

    private fun handleSegmentClick(segment: Segment) {
        val bundle = bundleOf().apply {
            putSerializable(Constants.BundleKeys.SEGMENT_DETAIL, segment)
        }
        appNavigation.openSegmentDetail(bundle)
    }

    private fun handleSegmentActionClick(segment: Segment, dataAction: SegmentCommandAction) {
        viewModel.handleEvent(HomePage2Event.SegmentAction(segment,dataAction))
    }

    private fun updateLoadingState(isLoading: Boolean) {
        if (isLoading) {
            showHideLoading(true)
        } else {
            showHideLoading(false)
        }
    }

    override fun onDestroyView() {
        segmentAdapter = null
        super.onDestroyView()
    }
}
