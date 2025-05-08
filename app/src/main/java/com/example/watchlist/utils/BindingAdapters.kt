package com.example.watchlist.utils

import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.watchlist.R
import com.example.watchlist.base.BaseDiffUtilCallback
import com.example.watchlist.base.BaseRecyclerViewAdapter
import com.example.watchlist.data.domain.Movie
import com.example.watchlist.data.repository.MovieRepository.Companion.TAG
import java.util.Locale

object BindingAdapters {


    /**
     * Use binding adapter to set the recycler view data using livedata object
     */
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveData")
    @JvmStatic
    fun <T> setRecyclerViewData(
        recyclerView: RecyclerView,
        items: LiveData<List<T>>?
    ) {
        val lifecycleOwner = recyclerView.context as? LifecycleOwner ?: return
        items?.observe(lifecycleOwner) { newItemList ->
            if (newItemList == null) {
                Log.d("BindingAdapter", "New item list is null")
                return@observe
            }
            val adapter = recyclerView.adapter as? BaseRecyclerViewAdapter<T>
            if (adapter != null) {
                val oldItemList = adapter.getItems()

                // Debug old and new lists
                Log.d("BindingAdapter", "Old list size: ${oldItemList.size}")
                oldItemList.forEach { Log.d("BindingAdapter", "Old item: $it") }
                Log.d("BindingAdapter", "New list size: ${newItemList.size}")
                newItemList.forEach { Log.d("BindingAdapter", "New item: $it") }

                // Setup DiffUtil
                val diffCallback = BaseDiffUtilCallback(
                    oldItemList,
                    newItemList,
                    itemComparison = { oldItem, newItem ->
                        Log.d("BindingAdapter", "Comparing old=$oldItem with new=$newItem")
                        (oldItem as? Movie)?.id == (newItem as? Movie)?.id
                    },
                    contentComparison = { oldItem, newItem ->
                        val oldMovie = oldItem as Movie
                        val newMovie = newItem as Movie
                        val contentSame = oldMovie.title == newMovie.title &&
                                oldMovie.date == newMovie.date &&
                                oldMovie.watchlist == newMovie.watchlist
                        if (!contentSame) {
                            Log.d("BindingAdapter", "Content mismatch for ID=${oldMovie.id}")
                        }
                        contentSame
                    }
                )

                // Apply DiffUtil results
                val diffResult = DiffUtil.calculateDiff(diffCallback)
                adapter.updateItems(newItemList)
                diffResult.dispatchUpdatesTo(adapter)
            } else {
                Log.e("BindingAdapter", "RecyclerView adapter is null or not a BaseRecyclerViewAdapter")
            }
        }
    }

    //show green tick when title watchlist property is true
    @JvmStatic
    @BindingAdapter("watchlistStatus")
    fun setWatchlistStatus(view: TextView, isInWatchlist: Int) {
        view.text = if (isInWatchlist == 1) "✅" else ""
    }

    //tidy type text in recycler view
    @JvmStatic
    @BindingAdapter("movieType")
    fun setMovieType(view: TextView, rawType: String?) {
        view.text = when (rawType) {
            "tv_series" -> "TV Series"
            "tv_miniseries" -> "TV Miniseries"
            "movie" -> "Movie"
            "short_film" -> "Short Film"
            "tv_movie" -> "TV Movie"
            "tv_special" -> "TV Special"
            else ->rawType ?: "Unknown"
        }
    }

    //ribbon recycler: tidy streaming service type
    @JvmStatic
    @BindingAdapter("streamingServiceType")
    fun setServiceType(view: TextView, rawServiceType: String?) {
        Log.d("Binding Adapter","streamingServiceType: $rawServiceType")
        view.text = when (rawServiceType) {
            "sub" -> "Subscription"
            "free" -> "Free"
            "tve" -> "TVE"
            "purchase" -> "Purchase"
            else ->rawServiceType ?: "Unknown"
        }
    }

    //format dates for recycler views
    @JvmStatic
    @BindingAdapter("formattedDate")
    fun setFormattedDate(textView: TextView, dateString: String?) {
        if (dateString.isNullOrEmpty()) {
            textView.text = "" // Set to empty if the date string is null or empty
            return
        }

        try {
            // Parse the original date string into a Date object
            val originalFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = originalFormat.parse(dateString)

            // Extract the day to add ordinal suffix (st, nd, rd, th)
            val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
            val day = dayFormat.format(date).toInt()

            val suffix = when {
                day in 11..13 -> "th" // Special case for 11th, 12th, 13th
                day % 10 == 1 -> "st"
                day % 10 == 2 -> "nd"
                day % 10 == 3 -> "rd"
                else -> "th"
            }

            // Format the Date object into the desired output: "21st Apr 2025"
            val monthAndYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val formattedDate = "$day$suffix ${monthAndYearFormat.format(date)}"

            // Set the formatted date to the TextView
            textView.text = formattedDate

        } catch (e: Exception) {
            // Handle invalid date formats gracefully
            textView.text = dateString // Fallback to the original string
        }
    }
    //format language title details fragment
    @JvmStatic
    @BindingAdapter("languageCode")
    fun bindLanguage(textView: TextView, languageCode: String?) {
        languageCode?.let {
            // Convert ISO code to full language name
            val locale = Locale(it) // Create a Locale instance
            val languageName = locale.displayLanguage // Get the full language name

            // Set the full language name in the TextView
            textView.text = languageName
        }
    }
    //format hyperlink title details fragment
    @JvmStatic
    @BindingAdapter("hyperlink")
    fun setHyperlink(textView: TextView, url: String?) {
        if (url != null) {
            val clickableText = "Link to Trailer" // Display text
            val spannableString = SpannableString(clickableText)

            // Set the link behavior
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        textView.context.startActivity(intent) // Open the link in a browser
                    }
                },
                0, clickableText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance() // Handle clickable links
            textView.visibility = View.VISIBLE
        } else {
            textView.visibility = View.GONE //when url is none hide the text view
        }
    }
}