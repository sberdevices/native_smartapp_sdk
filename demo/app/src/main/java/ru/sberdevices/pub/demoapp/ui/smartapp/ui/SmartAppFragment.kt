package ru.sberdevices.pub.demoapp.ui.smartapp.ui

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.pub.demoapp.ui.smartapp.BuyItems
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.BEANIE
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.BOOTS
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.GLOVES
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.JACKET
import ru.sberdevices.services.pub.demoapp.R
import ru.sberdevices.services.pub.demoapp.databinding.FragmentSmartappBinding

@ExperimentalCoroutinesApi
class SmartAppFragment : Fragment() {

    private val logger by Logger.lazy("SmartAppFragment")

    private val viewModel: SmartAppViewModel by viewModel()

    private lateinit var binding: FragmentSmartappBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSmartappBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.descriptionTextView.text = Html.fromHtml(
            getString(R.string.asistant_description),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        binding.buyElephantButton.setOnClickListener { viewModel.addItemsToCartAndPay() }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launchWhenResumed {
            viewModel.buyItems.collect { processPurchase(it) }
        }
        lifecycleScope.launchWhenResumed {
            viewModel.clothes.collect { processClothes(it) }
        }
    }

    private fun processPurchase(item: BuyItems) {
        when (item) {
            BuyItems.ELEPHANT -> {
                fadeIn(binding.elephantImageView)
                binding.buyElephantButton.isVisible = false
            }
        }
    }

    private fun processClothes(clothes: Clothes?) {
        logger.debug { "processClothes $clothes" }

        when (clothes) {
            BEANIE -> {
                fadeIn(binding.androidBeanieImageView)
            }

            GLOVES -> {
                fadeIn(binding.rigthMittenImageView)
                fadeIn(binding.leftMittenImageView)
            }

            BOOTS -> {
                fadeIn(binding.leftBootImageView)
                fadeIn(binding.rightBootImageView)
            }

            JACKET -> {
                fadeIn(binding.jacketImageView)
            }
            null -> {
                binding.androidBeanieImageView.isVisible = false
                binding.rigthMittenImageView.isVisible = false
                binding.leftMittenImageView.isVisible = false
                binding.leftBootImageView.isVisible = false
                binding.rightBootImageView.isVisible = false
                binding.jacketImageView.isVisible = false
            }
        }
    }

    companion object {
        fun newInstance() = SmartAppFragment()

        fun fadeIn(view: View) {
            view.alpha = 0f
            view.isVisible = true
            view.animate().alpha(1f).start()
        }
    }
}
