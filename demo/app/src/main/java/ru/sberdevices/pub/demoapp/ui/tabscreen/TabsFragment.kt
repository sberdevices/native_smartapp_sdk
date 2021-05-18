package ru.sberdevices.pub.demoapp.ui.tabscreen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.sberdevices.common.logger.Logger
import ru.sberdevices.pub.demoapp.ui.cv.ComputerVisionFragment
import ru.sberdevices.pub.demoapp.ui.smartapp.SmartAppFragment
import ru.sberdevices.services.pub.demoapp.R
import ru.sberdevices.services.pub.demoapp.databinding.FragmentTabsBinding

class TabsFragment : Fragment() {

    private val logger = Logger.get("TabsFragment")

    private val viewModel: TabsViewModel by viewModel()
    private lateinit var binding: FragmentTabsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setClickListeners()

        renderTabSelection(TabUi.SERVICES)

        lifecycleScope.launchWhenCreated {
            viewModel.isCameraAvailable.collect { hasCamera ->
                binding.cvTabButton.isEnabled = hasCamera
            }
        }
    }

    private fun setClickListeners() {
        binding.servicesTabButton.setOnClickListener { renderTabSelection(TabUi.SERVICES) }
        binding.cvTabButton.setOnClickListener { renderTabSelection(TabUi.CV) }
    }

    private fun renderTabSelection(selectedTab: TabUi) {
        logger.debug { "Render tab selection $selectedTab" }

        binding.servicesTabButton.isSelected = selectedTab == TabUi.SERVICES
        binding.cvTabButton.isSelected = selectedTab == TabUi.CV

        renderTabFragment(selectedTab)
    }

    private fun showChildFragment(fragment: Fragment, @IdRes fragmentContainerViewId: Int) {
        childFragmentManager.beginTransaction().apply {
            replace(fragmentContainerViewId, fragment)
            commit()
        }
    }

    private fun renderTabFragment(tab: TabUi) {
        val fragment = when (tab) {
            TabUi.SERVICES -> SmartAppFragment.newInstance()
            TabUi.CV -> ComputerVisionFragment.newInstance()
        }
        showChildFragment(fragment, R.id.fragmentContainerView)
    }

    // TODO можно использовать когда открывается фрагмент CV, чтобы пользователя не отвлекал лавашар
    // private fun enterImmersiveMode() {
    //     requireActivity().window.enterImmersiveMode()
    // }
    //
    // private fun exitImmersiveMode() {
    //     requireActivity().window.exitImmersiveMode()
    // }

    companion object {
        fun newInstance() = TabsFragment()

        enum class TabUi {
            /**
             * Tab for services demo
             */
            SERVICES,

            /**
             * Tab for Computer Vision demo
             */
            CV
        }
    }
}
