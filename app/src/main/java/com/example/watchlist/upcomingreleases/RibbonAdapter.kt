package com.example.watchlist.upcomingreleases

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.watchlist.R
import com.example.watchlist.data.domain.StreamingService
import com.example.watchlist.databinding.ItemRibbonBinding
import com.example.watchlist.upcomingreleases.RibbonAdapter.Companion.TAG
import retrofit2.http.Streaming

class RibbonAdapter(private var items: List<StreamingService>,
                    private val viewModel: UpcomingReleasesViewModel,
                    private val onItemClicked: (Int) -> Unit) :
    RecyclerView.Adapter<RibbonAdapter.RibbonViewHolder>() {

    companion object {
        val TAG = "WatchListApp"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RibbonViewHolder {
        val binding: ItemRibbonBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_ribbon,
            parent,
            false
        )
        return RibbonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RibbonViewHolder, position: Int) {
        val item = items[position]
        Log.d("DiffCallback", "Binding item ${item.id} = ${item.name}")
        holder.bind(item) {id, isChecked ->
            viewModel.updateCheckboxState(id, isChecked)
        }
    }
    //holder.bind(item, onItemClicked)
    override fun getItemCount() = items.size

    inner class RibbonViewHolder(val binding: ItemRibbonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StreamingService, onClick: (Int, Boolean) -> Unit) {
            binding.item = item // Bind the item to the layout

                // Prevent residual listeners, set current state, and update on change
                binding.ribbonCheckbox.setOnCheckedChangeListener(null)
                // restore the checkbox state from the ViewModel
                binding.ribbonCheckbox.isChecked = viewModel.getCheckboxState(item.id) // was it previously marked or not
                //binding.ribbonCheckbox.isChecked = item.isChecked //assign checkbox widget aligns with isChecked property on StreamingServiceItem
                binding.ribbonCheckbox.setOnCheckedChangeListener { _, isChecked ->
                    //item.isChecked = isChecked
                    viewModel.updateCheckboxState(item.id, isChecked)
                    Log.d(TAG, "${item.name} checkbox toggled: $isChecked")
                    onItemClicked(item.id)
                }
                binding.executePendingBindings()
            }
        }

    fun updateItems(newItems: List<StreamingService>) {
        Log.d("DiffCallback","DiffUtil started: Old size =${items.size}, New size =${newItems.size}")
        val diffCallback = StreamingServiceDiffCallback(items, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        Log.d("DiffCallback","DiffUtil calculated")
        items = newItems // Replace the old list with the new list
        diffResult.dispatchUpdatesTo(this) // Notify RecyclerView of precise changes
        Log.d("DiffCallback","RecyclerView Updated")
    }
}

class StreamingServiceDiffCallback(
    private val oldList: List<StreamingService>,
    private val newList: List<StreamingService>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compare unique identifiers (e.g., name or type)
        Log.d("DiffCallback","Comparing items ${oldList[oldItemPosition].name} == ${newList[newItemPosition].name}")
        return oldList[oldItemPosition].name == newList[newItemPosition].name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Compare full object content, including checkbox state
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]
        Log.d("DiffCallback","Comparing contents $oldItem == $newItem")
        return oldItem.name == newItem.name &&
                oldItem.type == newItem.type &&
                oldItem.isChecked == newItem.isChecked
    }
}