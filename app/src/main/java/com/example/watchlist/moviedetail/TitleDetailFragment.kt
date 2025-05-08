package com.example.watchlist.moviedetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.compose.compiler.plugins.kotlin.inference.Binding
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.Target
import com.example.watchlist.R
import com.example.watchlist.base.BaseFragment
import com.example.watchlist.data.domain.TitleDetailDomain
import com.example.watchlist.data.repository.MovieRepository
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.example.watchlist.databinding.FragmentMovieDetailBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import okhttp3.internal.format
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class TitleDetailFragment() : BaseFragment() {


    private lateinit var binding: FragmentMovieDetailBinding
    //override val _viewModel: TitleDetailViewModel by viewModel()

    override val _viewModel: TitleDetailViewModel by activityViewModel() //scope viewmodel to activity otherwise will not persist post navigation

    companion object {
        val TAG = "MovieDetailDebug"
    }

    private lateinit var watchlistButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, "onCreateView")
        // super.onCreateView(inflater, container, savedInstanceState)
        val layoutId = R.layout.fragment_movie_detail
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

       watchlistButton = binding.buttonWatchlist

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated")

        val titleDetail = arguments?.getParcelable<TitleDetailDomain>("titleDetail")
        val source = arguments?.getInt("source")
        val savedToWatchlist = WatchListState(arguments?.getInt("savedToWatchlist") ?: 0)
        Log.d(TAG, "Arguments in TitleDetailFragment : $arguments and $source and saved $savedToWatchlist")

        formatSaveRemoveButton(savedToWatchlist)

        if (titleDetail == null) {
            Log.e(TAG, "TitleDetailDomain is null")
            return
        }
        binding.titleDetail = titleDetail

        // genres set as chips
        val chipGroup = binding.genreChipGroup
        val genres = titleDetail.genres
        populateGenreChips(chipGroup, genres)

        // title poster
        val titlePoster = binding.titlePoster
        val posterUrl = titleDetail.poster
        loadImage(titlePoster, posterUrl)


        if (source == null) {
            binding.networkLogo.apply {
                loadImage(this, null)
            }
        } else {
            Log.d(TAG, "else triggered $source")
            _viewModel.fetchStreamingServiceLogo(source)
        }


        _viewModel.logoUrl.observe(viewLifecycleOwner) { logoUrl ->
            logoUrl?.let {
                Log.d(TAG, "Logo URL observed $logoUrl")
                val streamingServiceImageView = binding.networkLogo
                binding.networkLogo.post { loadImage(streamingServiceImageView, it ?: "") }
            }
        }

        watchlistButton.setOnClickListener { click ->
            saveOrRemoveWatchlist(savedToWatchlist, titleDetail)
        }

    }

    fun saveOrRemoveWatchlist(savedToWatchlist: WatchListState, titleDetail: TitleDetailDomain) {
        when (savedToWatchlist.isSaved) {
            0 -> {
                _viewModel.setWatchListTrue(titleDetail.id)
                Log.d(TAG, "BEFORE watchListButtonToggle SAVE ${savedToWatchlist.isSaved}")
                savedToWatchlist.isSaved = 1
                Log.d(TAG, "AFTER watchListButtonToggle SAVE ${savedToWatchlist.isSaved}")
                formatSaveRemoveButton(savedToWatchlist)
            }
            1 -> {
                _viewModel.setWatchListFalse(titleDetail.id)
                Log.d(TAG, "BEFORE watchListButtonToggle SAVE ${savedToWatchlist.isSaved}")
                savedToWatchlist.isSaved = 0
                Log.d(TAG, "AFTER watchListButtonToggle SAVE ${savedToWatchlist.isSaved}")
                formatSaveRemoveButton(savedToWatchlist)
            }
        }
    }

    fun formatSaveRemoveButton(savedToWatchlist: WatchListState) {
        when (savedToWatchlist.isSaved) {
            1 -> {
                watchlistButton.setText("Remove from WatchList")
                watchlistButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.amber))
            }
            0 -> {
                watchlistButton.setText("Add to WatchList")
                watchlistButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            }
        }
    }



    fun loadImage(imageView: ImageView, imageUrl: String?) {
        Log.d(TAG,"LOADIMAGE $imageUrl")
        clearImageView(imageView)
        Glide.with(imageView)
            .load(imageUrl?: "")
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .override(300,300)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(imageView)
    }

    fun populateGenreChips(chipGroup: ChipGroup, genres: List<String?>) {
        genres.forEach { genre ->
            if(!genre.isNullOrEmpty()) { //ensure is not null or empty
                val chip = Chip(chipGroup.context).apply {
                    text = genre //set chip text to the genre
                    isCheckable = false
                    isClickable = false
                    setTextAppearance(R.style.TextAppearance_MaterialComponents_Chips)
                }
                chipGroup.addView(chip)
                }
        }
    }

    fun clearImageView(imageView: ImageView) {
        imageView.setImageDrawable(null) // Removes any existing image or drawable
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        binding = null
//    }
}

data class WatchListState(var isSaved: Int)