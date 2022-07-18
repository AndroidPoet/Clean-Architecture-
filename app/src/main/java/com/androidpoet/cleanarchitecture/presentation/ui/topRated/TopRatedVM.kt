package com.androidpoet.cleanarchitecture.presentation.ui.topRated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidpoet.cleanarchitecture.domain.SafeResult
import com.androidpoet.cleanarchitecture.domain.model.DomainLayerMovies
import com.androidpoet.cleanarchitecture.domain.usecase.GetTopRatedUseCase
import com.androidpoet.cleanarchitecture.presentation.mapper.UiNowPlayingMapper
import com.androidpoet.cleanarchitecture.presentation.model.UiLayerNowPlaying
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class TopRatedVM @Inject constructor(
    private val getTopRatedUseCase: GetTopRatedUseCase,
    private val uiNowPlayingMapper: UiNowPlayingMapper
) : ViewModel() {

    var viewState = MutableStateFlow<TopRatedViewState>(TopRatedViewState.Loading)
        private set


    init {
        getCoins()
    }


    private fun getCoins() {
        viewState.value = TopRatedViewState.Loading
        viewModelScope.launch {
            val result = getTopRatedUseCase.perform()
//            Log.d("result", result.toString())
            when (result) {


                is SafeResult.Success -> {
                    val jokes = uiNowPlaying(result)
                    viewState.value = TopRatedViewState.ShowTopRated(jokes)
                }
                is SafeResult.Failure -> {
                    viewState.value = TopRatedViewState.Error(result.message)
                }
                SafeResult.NetworkError -> {
                    viewState.value = TopRatedViewState.Error("Network Error")
                }
            }
        }
    }

    fun navigateToDetail(uiCoin: UiLayerNowPlaying.NowPlaying) {
        viewModelScope.launch {
            //navigationManager.navigateTo(CoinDetailRoutes.root(uiCoin.id))
        }
    }

    private fun uiNowPlaying(result: SafeResult.Success<List<DomainLayerMovies.Movie>>) =
        result.data.map {

            Timber.d(it.title)

            uiNowPlayingMapper.mapToPresentation(it)
        }


}

sealed class TopRatedViewState {
    object Loading : TopRatedViewState()
    class ShowTopRated(var nowPlayings: List<UiLayerNowPlaying.NowPlaying>) : TopRatedViewState()
    class Error(var message: String) : TopRatedViewState()
}

