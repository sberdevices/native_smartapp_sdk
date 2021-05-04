package ru.sberdevices.pub.demoapp.ui.smartapp

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_smartapp.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.BEANIE
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.BOOTS
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.GLOVES
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.JACKET
import ru.sberdevices.services.sdk.demoapp.R

class SmartAppFragment : Fragment(R.layout.fragment_smartapp) {

    private val logger by Logger.lazy("SmartAppFragment")

    private val viewModel: SmartAppViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.clothes.observe(viewLifecycleOwner) { processClothes(it) }
        viewModel.buyItems.observe(viewLifecycleOwner) { processPurchase(it) }

        androidImageView.setOnClickListener { viewModel.sendServerAction() }
    }

    private fun processPurchase(item: BuyItems) {
        when (item) {
            BuyItems.ELEPHANT -> {
                elephantImageView.isVisible = true
            }
        }
    }

    private fun processClothes(clothes: Clothes) {
        logger.debug { "processClothes $clothes" }

        when (clothes) {
            BEANIE -> {
                androidBeanieImageView.isVisible = true
            }

            GLOVES -> {
                rigthMittenImageView.isVisible = true
                leftMittenImageView.isVisible = true
            }

            BOOTS -> {
                leftBootImageView.isVisible = true
                rightBootImageView.isVisible = true
            }

            JACKET -> {
                jacketImageView.isVisible = true
            }
        }
    }

}
