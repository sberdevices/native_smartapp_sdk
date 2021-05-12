package ru.sberdevices.pub.demoapp.ui.smartapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.BEANIE
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.BOOTS
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.GLOVES
import ru.sberdevices.pub.demoapp.ui.smartapp.Clothes.JACKET
import ru.sberdevices.services.pub.demoapp.R
import ru.sberdevices.services.pub.demoapp.databinding.FragmentSmartappBinding

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

        viewModel.clothes.observe(viewLifecycleOwner) { processClothes(it) }
        viewModel.buyItems.observe(viewLifecycleOwner) { processPurchase(it) }

        view.findViewById<ImageView>(R.id.androidImageView).setOnClickListener { viewModel.sendServerAction() }
    }

    private fun processPurchase(item: BuyItems) {
        when (item) {
            BuyItems.ELEPHANT -> {
                binding.elephantImageView.isVisible = true
            }
        }
    }

    private fun processClothes(clothes: Clothes) {
        logger.debug { "processClothes $clothes" }

        when (clothes) {
            BEANIE -> {
                binding.androidBeanieImageView.isVisible = true
            }

            GLOVES -> {
                binding.rigthMittenImageView.isVisible = true
                binding.leftMittenImageView.isVisible = true
            }

            BOOTS -> {
                binding.leftBootImageView.isVisible = true
                binding.rightBootImageView.isVisible = true
            }

            JACKET -> {
                binding.jacketImageView.isVisible = true
            }
        }
    }

    companion object {
        fun newInstance() = SmartAppFragment()

        private var View.isVisible: Boolean
            get() = TODO()
            set(isVisible: Boolean) {
                if (isVisible) {
                    this.visibility = View.VISIBLE
                } else {
                    this.visibility = View.GONE
                }
            }
    }
}
