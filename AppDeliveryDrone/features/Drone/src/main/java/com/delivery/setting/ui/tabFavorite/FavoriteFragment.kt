package com.delivery.setting.ui.tabFavorite

import androidx.fragment.app.viewModels
import com.delivery.core.base.fragment.BaseFragment
import com.delivery.core.utils.setLanguage
import com.delivery.core.utils.setOnSafeClickListener
import com.delivery.setting.DemoNavigation
import com.delivery.setting.R
import com.delivery.setting.databinding.FragmentFavoriteBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FavoriteFragment :
    BaseFragment<FragmentFavoriteBinding, FavoriteViewModel>(R.layout.fragment_favorite) {
    private val viewModel: FavoriteViewModel by viewModels()

    @Inject
    lateinit var appNavigation: DemoNavigation

    override fun getVM(): FavoriteViewModel = viewModel

    override fun setOnClick() {
        super.setOnClick()

        binding.btnVietNam.setOnSafeClickListener {
            changeLanguage("vi")
        }

        binding.btnEnglish.setOnSafeClickListener {
            changeLanguage("en")
        }
    }

    private fun changeLanguage(language: String) {
        requireContext().setLanguage(language)
        viewModel.setLanguage(language)

        binding.btnVietNam.text = getString(com.delivery.core.R.string.viet_nam)
        binding.btnEnglish.text = getString(com.delivery.core.R.string.english)
    }
}
