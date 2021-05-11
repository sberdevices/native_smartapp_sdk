package ru.sberdevices.pub.demoapp.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ru.sberdevices.services.pub.demoapp.R

class MainAdapter(private val listener: (MainMenuItem) -> Unit) : RecyclerView.Adapter<ItemHolder>() {

    private val _items: MutableList<MainMenuItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder =
        ItemHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.view_list_item, parent, false), listener
        )

    override fun getItemCount(): Int = _items.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = _items[position]
        holder.bind(item)
    }

    fun setItems(items: List<MainMenuItem>) {
        _items.clear()
        _items.addAll(items)
        notifyDataSetChanged()
    }
}

class ItemHolder(view: View, private val listener: (MainMenuItem) -> Unit) : RecyclerView.ViewHolder(view) {
    private val textView = view.findViewById<TextView>(R.id.textView)
    private val cardView = view.findViewById<CardView>(R.id.card_view)

    fun bind(item: MainMenuItem) {
        textView.text = item.text
        cardView.setOnClickListener { listener(item) }
        cardView.animate().alpha(1.0f).setDuration(500).start()
    }
}
