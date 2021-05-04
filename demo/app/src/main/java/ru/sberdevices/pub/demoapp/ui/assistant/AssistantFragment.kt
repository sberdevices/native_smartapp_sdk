package ru.sberdevices.pub.demoapp.ui.assistant

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_assistant.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.sberdevices.common.extensions.enterImmersiveMode
import ru.sberdevices.common.extensions.exitImmersiveMode
import ru.sberdevices.services.sdk.demoapp.R

class AssistantFragment : Fragment(R.layout.fragment_assistant) {

    private val viewModel: AssistantViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.shadeState.observe(viewLifecycleOwner) { show ->
            if (show) {
                exitImmersiveMode()
            } else {
                enterImmersiveMode()
            }
        }

        shadeShowButton.setOnClickListener { viewModel.setShadeShown(true) }
        shadeHideButton.setOnClickListener { viewModel.setShadeShown(false) }
    }

    private fun enterImmersiveMode() {
        requireActivity().window.enterImmersiveMode()
    }

    private fun exitImmersiveMode() {
        requireActivity().window.exitImmersiveMode()
    }
}
