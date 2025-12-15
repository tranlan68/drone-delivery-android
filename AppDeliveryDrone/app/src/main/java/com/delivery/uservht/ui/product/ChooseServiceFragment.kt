package com.delivery.uservht.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.delivery.setting.ui.orderhistory.OrderListFragment
import androidx.navigation.fragment.findNavController
import com.delivery.vht.R
import com.delivery.vht.databinding.FragmentChooseServiceBinding

class ChooseServiceFragment : Fragment() {

    private var _binding: FragmentChooseServiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // CLICK BUTTON
        binding.btnChooseService.setOnClickListener {
            openDeliveryScreen()
        }
//        binding.ivArrow.setOnClickListener {
//            openDeliveryScreen()
//        }
//        binding.ivArrow.setOnClickListener { view ->
//            view.animate()
//                .scaleX(0.88f)
//                .scaleY(0.88f)
//                .setDuration(70)
//                .withEndAction {
//                    view.animate().scaleX(1f).scaleY(1f).setDuration(70)
//                }
//
//            openDeliveryScreen()
//        }
    }

    private fun openDeliveryScreen() {
        // Nếu dùng Navigation Component:
        findNavController().navigate(
            R.id.action_global_to_homeFragment
        )

        // Nếu dùng custom navigation:
        // appNavigation.openDeliveryTracking(Bundle())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
