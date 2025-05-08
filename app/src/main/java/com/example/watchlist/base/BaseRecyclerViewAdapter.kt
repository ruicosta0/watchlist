package com.example.watchlist.base

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.watchlist.data.domain.Movie
import com.example.watchlist.watchlist.WatchlistFragment
import com.example.watchlist.watchlist.WatchlistFragment.Companion

abstract class BaseRecyclerViewAdapter<T>(private val callback: ((item: T) -> Unit)? = null,
    private val clickListener: ((item: T) -> Unit)? =null) :
    RecyclerView.Adapter<DataBindingViewHolder<T>>() {


    companion object {
        val TAG = "WatchListApp"
    }

    private var _items: MutableList<T> = mutableListOf()

    override fun getItemCount() = _items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder<T> {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil
            .inflate<ViewDataBinding>(layoutInflater, getLayoutRes(viewType), parent, false)
        binding.lifecycleOwner = getLifecycleOwner()
        return DataBindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DataBindingViewHolder<T>, position: Int) {
        Log.d("onBindViewHolder", "Binding view at position $position, ITEM COUNT ${itemCount}")
        if (position < 0 || position >= itemCount) {
            Log.d("onBindViewHolder", "Invalid position requested: $position")
            return
        }
        val item = getItem(position)
        holder.bind(item) //see DataBindingViewHolder.kt which binds item to UI
        holder.itemView.setOnClickListener {
            callback?.invoke(item) //see RemindersListAdapter(?)
        }
    }

    fun getItems(): List<T> = _items //Expose the current dataset

    fun updateItems(newItems: List<T>) {
        Log.d("DiffUtil", "Updating items. Old size=${_items.size}, New size=${newItems.size}")
        newItems.forEach { item ->
            Log.d("DiffUtil", "Movie ID=${(item as? Movie)?.id}; Title=${(item as? Movie)?.title}; Release Date=${(item as? Movie)?.date}; service=${(item as? Movie)?.service};source=${(item as? Movie)?.sourceId}; type=${(item as? Movie)?.type};watclist=${(item as? Movie)?.watchlist}")
            if ((item as? Movie)?.id == null || (item as? Movie)?.title == null || (item as? Movie)?.date == null || (item as? Movie)?.service == null || (item as? Movie)?.sourceId == null || (item as? Movie)?.type == null || (item as? Movie)?.watchlist == null)
            {
                Log.e("Debug", "Invalid movie data: $item")
            }
        }
        val diffCallback = BaseDiffUtilCallback( //swapped out in favour of notifyDataSetChanged
            oldList = _items.toList(),
            newList = newItems,
            itemComparison = { old, new ->
                //Log.d( "DiffUtil","comparing old ID=${(old as Movie).id} with new id=${(new as Movie).id}")
                Log.d(
                    "DiffUtil",
                    "Comparing old ID=${(old as Movie).id}, Title=${(old as Movie).title}.title} with new ID=${(new as Movie).id}, Title=${(new as Movie).title}"
                )
                (old as? Movie)?.id == (new as? Movie)?.id
            },
            contentComparison = { old, new ->
                val oldMovie = old as Movie
                val newMovie = new as Movie
                val contentSame = oldMovie.title == newMovie.title &&
                        oldMovie.date == newMovie.date &&
                        oldMovie.watchlist == newMovie.watchlist
                if (!contentSame) {
                    Log.d(
                        "DiffUtil",
                        "Content difference detected for ID=${oldMovie.id}: old=$oldMovie, new=$newMovie"
                    )
                }
                Log.d(
                    "DiffUtil",
                    "Adapter dataset updated. Adapter size=${_items.size}, Expected size=${newItems.size}"
                )
                _items.forEach { Log.d("DiffUtil", "Adapter item: ${(it as Movie).id}") }
                contentSame
            }
        )
        Log.d(WatchlistFragment.TAG, "Updating adapter with new list ${newItems.size} items")
        _items.clear()
        _items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): T {
        Log.d("getItem", "getItem CALLED WITH POSITION = $position, _items.size ${_items.size}")
        if (position < 0 || position >= _items.size) {
            Log.d("getItem", "Invalid position requested: $position. Size: ${_items}")
            throw IndexOutOfBoundsException("Invalid Position: $position. Size: ${_items.size}")
        }
        Log.d("getItem", "getItem ${_items[position]}")
        return _items[position]
    }

    @LayoutRes
    abstract fun getLayoutRes(viewType: Int): Int

    open fun getLifecycleOwner(): LifecycleOwner? {
        return null
    }
}

class BaseDiffUtilCallback<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    private val itemComparison: (T, T) -> Boolean,
    private val contentComparison: (T, T) -> Boolean
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return itemComparison(oldList[oldItemPosition], newList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return contentComparison(oldList[oldItemPosition], newList[newItemPosition])
    }
}

//not in adapter class as adapter should not hold any functionality, this function is passed as a
//constructor to the adapter class
//listens to clicks, receives a Movie object and passes its codename field to the Main Frag
class MovieListener(val clickListener: (movie: Movie) -> Unit) {
    fun onClick(movie: Movie) = clickListener(movie)
}