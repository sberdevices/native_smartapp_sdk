package ru.sberdevices.pub.demoapp.ui.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_main.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.sberdevices.services.sdk.demoapp.R

class MainFragment : Fragment(R.layout.fragment_main) {

    private val viewModel: MainViewModel by viewModel()
    private val adapter = MainAdapter { item ->
        viewModel.onItemClick(item)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.recyclerView.adapter = adapter
        view.recyclerView.layoutManager = GridLayoutManager(context, 3)

        viewModel.menuItem.observe(viewLifecycleOwner) { items ->
            adapter.setItems(items)
        }

        viewModel.navigation.observe(viewLifecycleOwner) { action ->
            action?.let { findNavController().navigate(it) }
        }
    }
}
