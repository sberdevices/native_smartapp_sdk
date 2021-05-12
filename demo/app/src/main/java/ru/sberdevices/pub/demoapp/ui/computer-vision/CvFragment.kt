package ru.sberdevices.pub.demoapp.ui.`computer-vision`

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.sberdevices.pub.demoapp.ui.assistant.AssistantViewModel
import ru.sberdevices.services.pub.demoapp.R

class CvFragment : Fragment(R.layout.fragment_cv) {

    private val viewModel: AssistantViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        fun newInstance() = CvFragment()
    }
}