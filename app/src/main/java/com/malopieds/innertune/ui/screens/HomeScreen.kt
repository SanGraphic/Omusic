package com.malopieds.innertune.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.malopieds.innertube.models.AlbumItem
import com.malopieds.innertube.models.Artist
import com.malopieds.innertube.models.ArtistItem
import com.malopieds.innertube.models.PlaylistItem
import com.malopieds.innertube.models.SongItem
import com.malopieds.innertube.models.WatchEndpoint
import com.malopieds.innertube.utils.parseCookieString
import com.malopieds.innertune.LocalDatabase
import com.malopieds.innertune.LocalPlayerAwareWindowInsets
import com.malopieds.innertune.LocalPlayerConnection
import com.malopieds.innertune.R
import com.malopieds.innertune.constants.GridThumbnailHeight
import com.malopieds.innertune.constants.InnerTubeCookieKey
import com.malopieds.innertune.constants.ListItemHeight
import com.malopieds.innertune.extensions.togglePlayPause
import com.malopieds.innertune.models.toMediaMetadata
import com.malopieds.innertune.playback.queues.YouTubeAlbumRadio
import com.malopieds.innertune.playback.queues.YouTubeQueue
import com.malopieds.innertune.ui.component.AlbumSmallGridItem
import com.malopieds.innertune.ui.component.ArtistSmallGridItem
import com.malopieds.innertune.ui.component.HideOnScrollFAB
import com.malopieds.innertune.ui.component.LocalMenuState
import com.malopieds.innertune.ui.component.NavigationTile
import com.malopieds.innertune.ui.component.NavigationTitle
import com.malopieds.innertune.ui.component.SongListItem
import com.malopieds.innertune.ui.component.SongSmallGridItem
import com.malopieds.innertune.ui.component.YouTubeGridItem
import com.malopieds.innertune.ui.component.YouTubeSmallGridItem
import com.malopieds.innertune.ui.menu.ArtistMenu
import com.malopieds.innertune.ui.menu.SongMenu
import com.malopieds.innertune.ui.menu.YouTubeAlbumMenu
import com.malopieds.innertune.ui.menu.YouTubeArtistMenu
import com.malopieds.innertune.ui.menu.YouTubePlaylistMenu
import com.malopieds.innertune.ui.menu.YouTubeSongMenu
import com.malopieds.innertune.ui.utils.SnapLayoutInfoProvider
import com.malopieds.innertune.utils.rememberPreference
import com.malopieds.innertune.viewmodels.HomeViewModel
import kotlin.random.Random
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.foundation.clickable
import com.malopieds.innertune.ui.component.SearchBar
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

@SuppressLint("UnrememberedMutableState")
@Suppress("DEPRECATION")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    searchBarVisible: Boolean = false,
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()

    val forgottenFavorite by viewModel.forgottenFavorite.collectAsState()
    val homeFirstAlbumRecommendation by viewModel.homeFirstAlbumRecommendation.collectAsState()
    val homeSecondAlbumRecommendation by viewModel.homeSecondAlbumRecommendation.collectAsState()

    val homeFirstArtistRecommendation by viewModel.homeFirstArtistRecommendation.collectAsState()
    val homeSecondArtistRecommendation by viewModel.homeSecondArtistRecommendation.collectAsState()
    val homeThirdArtistRecommendation by viewModel.homeThirdArtistRecommendation.collectAsState()
    val home by viewModel.home.collectAsState()

    val keepListeningSongs by viewModel.keepListeningSongs.collectAsState()
    val keepListeningAlbums by viewModel.keepListeningAlbums.collectAsState()
    val keepListeningArtists by viewModel.keepListeningArtists.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()

    val homeFirstContinuation by viewModel.homeFirstContinuation.collectAsState()
    val homeSecondContinuation by viewModel.homeSecondContinuation.collectAsState()
    val homeThirdContinuation by viewModel.homeThirdContinuation.collectAsState()

    val youtubePlaylists by viewModel.youtubePlaylists.collectAsState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val mostPlayedLazyGridState = rememberLazyGridState()
    val forgottenFavoritesLazyGridState = rememberLazyGridState()
    val listenAgainLazyGridState = rememberLazyGridState()

    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn =
        remember(innerTubeCookie) {
            "SAPISID" in parseCookieString(innerTubeCookie)
        }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop = backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            scrollState.animateScrollTo(value = 0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", true)
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = viewModel::refresh,
        indicatorPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(),
        ) {
            val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
            val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
            val snapLayoutInfoProviderQuickPicks =
                remember(mostPlayedLazyGridState) {
                    SnapLayoutInfoProvider(
                        lazyGridState = mostPlayedLazyGridState,
                    )
                }
            val snapLayoutInfoProviderForgottenFavorite =
                remember(forgottenFavoritesLazyGridState) {
                    SnapLayoutInfoProvider(
                        lazyGridState = forgottenFavoritesLazyGridState,
                    )
                }

            Column(
                modifier = Modifier.verticalScroll(scrollState),
            ) {
                if (searchBarVisible) {
                    Spacer(
                        Modifier.height(
                            LocalPlayerAwareWindowInsets.current
                                .asPaddingValues()
                                .calculateTopPadding(),
                        ),
                    )
                }

                quickPicks?.let { quickPicks ->
                    if (quickPicks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.height(15.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        style = SpanStyle(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary,
                                                    MaterialTheme.colorScheme.tertiary
                                                ),
                                                tileMode = TileMode.Clamp
                                            )
                                        )
                                    ) {
                                        append("Your Mix")
                                    }
                                },
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontSize = MaterialTheme.typography.displaySmall.fontSize * 0.75f
                                ),
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { navController.navigate("history") }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.history),
                                    contentDescription = stringResource(R.string.history)
                                )
                            }
                            IconButton(
                                onClick = { navController.navigate("stats") }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.trending_up),
                                    contentDescription = stringResource(R.string.stats)
                                )
                            }
                            if (isLoggedIn) {
                                IconButton(
                                    onClick = { navController.navigate("account") }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.person),
                                        contentDescription = stringResource(R.string.account)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { navController.navigate("settings") }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.settings),
                                    contentDescription = stringResource(R.string.settings)
                                )
                            }
                            IconButton(
                                onClick = {
                                    navController.currentBackStackEntry?.savedStateHandle?.set("active", true)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.search),
                                    contentDescription = stringResource(R.string.search)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        androidx.compose.foundation.lazy.grid.LazyHorizontalGrid(
                            rows = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 16.dp),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                        ) {
                            items(quickPicks.size) { idx ->
                                val song = database.song(quickPicks[idx].id).collectAsState(initial = quickPicks[idx]).value!!
                                Column(
                                    modifier = Modifier
                                        .width(110.dp)
                                        .clickable {
                                            if (song.id == mediaMetadata?.id) {
                                                playerConnection.player.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    YouTubeQueue(
                                                        WatchEndpoint(videoId = song.id),
                                                        song.toMediaMetadata(),
                                                    ),
                                                )
                                            }
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    androidx.compose.foundation.Image(
                                        painter = coil.compose.rememberAsyncImagePainter(song.song.thumbnailUrl),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .width(110.dp)
                                            .height(110.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = song.song.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.align(Alignment.Start),
                                    )
                                    Text(
                                        text = song.artists.joinToString { it.name },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.align(Alignment.Start),
                                    )
                                }
                            }
                        }
                    }
                }

                if (youtubePlaylists?.isNotEmpty() == true) {
                    NavigationTitle(
                        title = stringResource(R.string.your_ytb_playlists),
                        onClick = {
                            navController.navigate("account")
                        },
                        modifier = Modifier.padding(start = 24.dp),
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    androidx.compose.foundation.lazy.grid.LazyHorizontalGrid(
                        rows = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 16.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                    ) {
                        items(youtubePlaylists!!.size) { idx ->
                            val item = youtubePlaylists!![idx]
                            Column(
                                modifier = Modifier
                                    .width(110.dp)
                                    .clickable {
                                        navController.navigate("online_playlist/${item.id}")
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = coil.compose.rememberAsyncImagePainter(item.thumbnail),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(110.dp)
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.align(Alignment.Start),
                                )
                                Text(
                                    text = item.author?.name ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    modifier = Modifier.align(Alignment.Start),
                                )
                            }
                        }
                    }
                }

                if (keepListening?.isNotEmpty() == true) {
                    keepListening?.let {
                        Spacer(modifier = Modifier.height(48.dp))
                        NavigationTitle(
                            title = stringResource(R.string.keep_listening),
                            modifier = Modifier.padding(start = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        LazyHorizontalGrid(
                            state = listenAgainLazyGridState,
                            rows = GridCells.Fixed(if (keepListening!!.size > 6) 2 else 1),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(GridThumbnailHeight * if (keepListening!!.size > 6) 2.4f else 1.2f),
                        ) {
                            keepListening?.forEach {
                                when (it) {
                                    in 0..4 ->
                                        item {
                                            ArtistSmallGridItem(
                                                artist = keepListeningArtists!![it],
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .combinedClickable(
                                                            onClick = {
                                                                navController.navigate("artist/${keepListeningArtists!![it].id}")
                                                            },
                                                            onLongClick = {
                                                                haptic.performHapticFeedback(
                                                                    HapticFeedbackType.LongPress,
                                                                )
                                                                menuState.show {
                                                                    ArtistMenu(
                                                                        originalArtist = keepListeningArtists!![it],
                                                                        coroutineScope = coroutineScope,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }
                                                            },
                                                        ),
                                            )
                                        }

                                    in 5..9 ->
                                        item {
                                            AlbumSmallGridItem(
                                                song = keepListeningAlbums!![it - 5],
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .combinedClickable(
                                                            onClick = {
                                                                navController.navigate(
                                                                    "album/${keepListeningAlbums!![it - 5].song.albumId}",
                                                                )
                                                            },
                                                        ),
                                            )
                                        }

                                    in 10..19 ->
                                        item {
                                            SongSmallGridItem(
                                                song = keepListeningSongs!![it - 10],
                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .combinedClickable(
                                                            onClick = {
                                                                if (keepListeningSongs!![it - 10].id == mediaMetadata?.id) {
                                                                    playerConnection.player.togglePlayPause()
                                                                } else {
                                                                    playerConnection.playQueue(
                                                                        YouTubeQueue(
                                                                            WatchEndpoint(videoId = keepListeningSongs!![it - 10].id),
                                                                            keepListeningSongs!![it - 10].toMediaMetadata(),
                                                                        ),
                                                                    )
                                                                }
                                                            },
                                                            onLongClick = {
                                                                haptic.performHapticFeedback(
                                                                    HapticFeedbackType.LongPress,
                                                                )
                                                                menuState.show {
                                                                    SongMenu(
                                                                        originalSong = keepListeningSongs!![it - 10],
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }
                                                            },
                                                        ),
                                                isActive = keepListeningSongs!![it - 10].song.id == mediaMetadata?.id,
                                                isPlaying = isPlaying,
                                            )
                                        }
                                }
                            }
                        }
                    }
                }

                homeFirstArtistRecommendation?.let { albums ->
                    if (albums.listItem.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.artistName,
                            modifier = Modifier.padding(start = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        LazyRow(
                            modifier = Modifier.padding(start = 24.dp),
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.listItem,
                                key = { it.id },
                            ) { item ->
                                if (!item.title.contains("Presenting")) {
                                    YouTubeSmallGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        when (item) {
                                                            is PlaylistItem ->
                                                                navController.navigate(
                                                                    "online_playlist/${item.id}",
                                                                )

                                                            is SongItem -> {
                                                                if (item.id == mediaMetadata?.id) {
                                                                    playerConnection.player.togglePlayPause()
                                                                } else {
                                                                    playerConnection.playQueue(
                                                                        YouTubeQueue(
                                                                            WatchEndpoint(videoId = item.id),
                                                                            item.toMediaMetadata(),
                                                                        ),
                                                                    )
                                                                }
                                                            }

                                                            is AlbumItem -> navController.navigate("album/${item.id}")

                                                            else -> navController.navigate("artist/${item.id}")
                                                        }
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            when (item) {
                                                                is PlaylistItem ->
                                                                    YouTubePlaylistMenu(
                                                                        playlist = item,
                                                                        coroutineScope = coroutineScope,
                                                                        onDismiss = menuState::dismiss,
                                                                    )

                                                                is ArtistItem -> {
                                                                    YouTubeArtistMenu(
                                                                        artist = item,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is SongItem -> {
                                                                    YouTubeSongMenu(
                                                                        song = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is AlbumItem -> {
                                                                    YouTubeAlbumMenu(
                                                                        albumItem = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                else -> {
                                                                }
                                                            }
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                forgottenFavorite?.let { forgottenFavorite ->
                    if (forgottenFavorite.isNotEmpty() && forgottenFavorite.size > 5) {
                        NavigationTitle(
                            title = stringResource(R.string.forgotten_favorites),
                            modifier = Modifier.padding(start = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        LazyHorizontalGrid(
                            state = forgottenFavoritesLazyGridState,
                            rows = GridCells.Fixed(4),
                            flingBehavior =
                                rememberSnapFlingBehavior(
                                    snapLayoutInfoProviderForgottenFavorite,
                                ),
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(ListItemHeight * 4),
                        ) {
                            items(
                                items = forgottenFavorite,
                                key = { it.id },
                            ) { originalSong ->
                                val song by database
                                    .song(originalSong.id)
                                    .collectAsState(initial = originalSong)
                                SongListItem(
                                    song = song!!,
                                    showInLibraryIcon = true,
                                    isActive = song!!.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    trailingContent = {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                            )
                                        }
                                    },
                                    modifier =
                                        Modifier
                                            .width(horizontalLazyGridItemWidth)
                                            .combinedClickable(
                                                onClick = {
                                                    if (song!!.id == mediaMetadata?.id) {
                                                        playerConnection.player.togglePlayPause()
                                                    } else {
                                                        playerConnection.playQueue(
                                                            YouTubeQueue(
                                                                WatchEndpoint(videoId = song!!.id),
                                                                song!!.toMediaMetadata(),
                                                            ),
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        SongMenu(
                                                            originalSong = song!!,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            ),
                                )
                            }
                        }
                    }
                }

                home?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()) {
                        homePlaylists.let { playlists ->
                            NavigationTitle(
                                title = playlists.playlistName,
                                modifier = Modifier.padding(start = 24.dp),
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            androidx.compose.foundation.lazy.grid.LazyHorizontalGrid(
                                rows = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 16.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                            ) {
                                items(playlists.playlists.size) { idx ->
                                    val playlist = playlists.playlists[idx]
                                    playlist.author ?: run {
                                        playlist.author = Artist(name = "YouTube Music", id = null)
                                    }
                                    Column(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .clickable {
                                                navController.navigate("online_playlist/${playlist.id}")
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = coil.compose.rememberAsyncImagePainter(playlist.thumbnail),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = playlist.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.Start),
                                        )
                                        Text(
                                            text = playlist.author?.name ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.Start),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                homeFirstAlbumRecommendation?.albums?.let { albums ->
                    if (albums.recommendationAlbum.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.recommendedAlbum.name,
                            modifier = Modifier.padding(start = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        LazyRow(
                            modifier = Modifier.padding(start = 24.dp),
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.recommendationAlbum,
                                key = { it.id },
                            ) { album ->
                                if (!album.title.contains("Presenting")) {
                                    YouTubeGridItem(
                                        item = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("online_playlist/${album.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            YouTubePlaylistMenu(
                                                                playlist = album,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeFirstContinuation?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()) {
                        homePlaylists.let { playlists ->
                            NavigationTitle(
                                title = playlists.playlistName,
                                modifier = Modifier.padding(start = 24.dp),
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            androidx.compose.foundation.lazy.grid.LazyHorizontalGrid(
                                rows = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 16.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                            ) {
                                items(playlists.playlists.size) { idx ->
                                    val playlist = playlists.playlists[idx]
                                    playlist.author ?: run {
                                        playlist.author = Artist(name = "YouTube Music", id = null)
                                    }
                                    Column(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .clickable {
                                                navController.navigate("online_playlist/${playlist.id}")
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = coil.compose.rememberAsyncImagePainter(playlist.thumbnail),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = playlist.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.Start),
                                        )
                                        Text(
                                            text = playlist.author?.name ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.Start),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                homeSecondArtistRecommendation?.let { albums ->
                    if (albums.listItem.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.artistName,
                            modifier = Modifier.padding(start = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        LazyRow(
                            modifier = Modifier.padding(start = 24.dp),
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.listItem,
                                key = { it.id },
                            ) { item ->
                                if (!item.title.contains("Presenting")) {
                                    YouTubeSmallGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        when (item) {
                                                            is PlaylistItem ->
                                                                navController.navigate(
                                                                    "online_playlist/${item.id}",
                                                                )

                                                            is SongItem -> {
                                                                if (item.id == mediaMetadata?.id) {
                                                                    playerConnection.player.togglePlayPause()
                                                                } else {
                                                                    playerConnection.playQueue(
                                                                        YouTubeQueue(
                                                                            WatchEndpoint(videoId = item.id),
                                                                            item.toMediaMetadata(),
                                                                        ),
                                                                    )
                                                                }
                                                            }

                                                            is AlbumItem -> navController.navigate("album/${item.id}")

                                                            else -> navController.navigate("artist/${item.id}")
                                                        }
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            when (item) {
                                                                is PlaylistItem ->
                                                                    YouTubePlaylistMenu(
                                                                        playlist = item,
                                                                        coroutineScope = coroutineScope,
                                                                        onDismiss = menuState::dismiss,
                                                                    )

                                                                is ArtistItem -> {
                                                                    YouTubeArtistMenu(
                                                                        artist = item,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is SongItem -> {
                                                                    YouTubeSongMenu(
                                                                        song = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is AlbumItem -> {
                                                                    YouTubeAlbumMenu(
                                                                        albumItem = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                else -> {
                                                                }
                                                            }
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeSecondContinuation?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()) {
                        homePlaylists.let { playlists ->
                            NavigationTitle(
                                title = playlists.playlistName,
                                modifier = Modifier.padding(start = 24.dp),
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            androidx.compose.foundation.lazy.grid.LazyHorizontalGrid(
                                rows = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 16.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                            ) {
                                items(playlists.playlists.size) { idx ->
                                    val playlist = playlists.playlists[idx]
                                    playlist.author ?: run {
                                        playlist.author = Artist(name = "YouTube Music", id = null)
                                    }
                                    Column(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .clickable {
                                                navController.navigate("online_playlist/${playlist.id}")
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = coil.compose.rememberAsyncImagePainter(playlist.thumbnail),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = playlist.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.Start),
                                        )
                                        Text(
                                            text = playlist.author?.name ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.Start),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                homeSecondAlbumRecommendation?.albums?.let { albums ->
                    if (albums.recommendationAlbum.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.recommendedAlbum.name,
                            modifier = Modifier.padding(start = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        LazyRow(
                            modifier = Modifier.padding(start = 24.dp),
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.recommendationAlbum,
                                key = { it.id },
                            ) { album ->
                                if (!album.title.contains("Presenting")) {
                                    YouTubeGridItem(
                                        item = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("online_playlist/${album.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            YouTubePlaylistMenu(
                                                                playlist = album,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                homeThirdContinuation?.forEach { homePlaylists ->
                    if (homePlaylists.playlists.isNotEmpty()) {
                        homePlaylists.let { playlists ->
                            NavigationTitle(
                                title = playlists.playlistName,
                                modifier = Modifier.padding(start = 24.dp),
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            androidx.compose.foundation.lazy.grid.LazyHorizontalGrid(
                                rows = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 24.dp, end = 16.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                            ) {
                                items(playlists.playlists.size) { idx ->
                                    val playlist = playlists.playlists[idx]
                                    playlist.author ?: run {
                                        playlist.author = Artist(name = "YouTube Music", id = null)
                                    }
                                    Column(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .clickable {
                                                navController.navigate("online_playlist/${playlist.id}")
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        androidx.compose.foundation.Image(
                                            painter = coil.compose.rememberAsyncImagePainter(playlist.thumbnail),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(110.dp)
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = playlist.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.Start),
                                        )
                                        Text(
                                            text = playlist.author?.name ?: "",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            modifier = Modifier.align(Alignment.Start),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                homeThirdArtistRecommendation?.let { albums ->
                    if (albums.listItem.isNotEmpty()) {
                        NavigationTitle(
                            title = stringResource(R.string.similar_to) + " " + albums.artistName,
                            modifier = Modifier.padding(start = 24.dp),
                        )
                        Spacer(modifier = Modifier.height(32.dp))

                        LazyRow(
                            modifier = Modifier.padding(start = 24.dp),
                            contentPadding =
                                WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                        ) {
                            items(
                                items = albums.listItem,
                                key = { it.id },
                            ) { item ->
                                if (!item.title.contains("Presenting")) {
                                    YouTubeSmallGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        when (item) {
                                                            is PlaylistItem ->
                                                                navController.navigate(
                                                                    "online_playlist/${item.id}",
                                                                )

                                                            is SongItem -> {
                                                                if (item.id == mediaMetadata?.id) {
                                                                    playerConnection.player.togglePlayPause()
                                                                } else {
                                                                    playerConnection.playQueue(
                                                                        YouTubeQueue(
                                                                            WatchEndpoint(videoId = item.id),
                                                                            item.toMediaMetadata(),
                                                                        ),
                                                                    )
                                                                }
                                                            }

                                                            is AlbumItem -> navController.navigate("album/${item.id}")

                                                            else -> navController.navigate("artist/${item.id}")
                                                        }
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress,
                                                        )
                                                        menuState.show {
                                                            when (item) {
                                                                is PlaylistItem ->
                                                                    YouTubePlaylistMenu(
                                                                        playlist = item,
                                                                        coroutineScope = coroutineScope,
                                                                        onDismiss = menuState::dismiss,
                                                                    )

                                                                is ArtistItem -> {
                                                                    YouTubeArtistMenu(
                                                                        artist = item,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is SongItem -> {
                                                                    YouTubeSongMenu(
                                                                        song = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                is AlbumItem -> {
                                                                    YouTubeAlbumMenu(
                                                                        albumItem = item,
                                                                        navController = navController,
                                                                        onDismiss = menuState::dismiss,
                                                                    )
                                                                }

                                                                else -> {
                                                                }
                                                            }
                                                        }
                                                    },
                                                ).animateItemPlacement(),
                                    )
                                }
                            }
                        }
                    }
                }

                explorePage?.newReleaseAlbums?.let { newReleaseAlbums ->
                    NavigationTitle(
                        title = stringResource(R.string.new_release_albums),
                        onClick = {
                            navController.navigate("new_release")
                        },
                        modifier = Modifier.padding(start = 24.dp),
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    LazyRow(
                        modifier = Modifier.padding(start = 24.dp),
                        contentPadding =
                            WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                    ) {
                        items(
                            items = newReleaseAlbums,
                            key = { it.id },
                        ) { album ->
                            YouTubeGridItem(
                                item = album,
                                isActive = mediaMetadata?.album?.id == album.id,
                                isPlaying = isPlaying,
                                coroutineScope = coroutineScope,
                                modifier =
                                    Modifier
                                        .combinedClickable(
                                            onClick = {
                                                navController.navigate("album/${album.id}")
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    YouTubeAlbumMenu(
                                                        albumItem = album,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        ).animateItemPlacement(),
                            )
                        }
                    }
                }
                Spacer(
                    Modifier.height(
                        LocalPlayerAwareWindowInsets.current
                            .asPaddingValues()
                            .calculateBottomPadding(),
                    ),
                )
            }

            HideOnScrollFAB(
                visible =
                    !quickPicks.isNullOrEmpty() || !forgottenFavorite.isNullOrEmpty() || explorePage?.newReleaseAlbums?.isNotEmpty() == true,
                scrollState = scrollState,
                icon = R.drawable.casino,
                onClick = {
                    if (Random.nextBoolean() && !quickPicks.isNullOrEmpty()) {
                        val song = quickPicks!!.random()
                        playerConnection.playQueue(YouTubeQueue(WatchEndpoint(videoId = song.id), song.toMediaMetadata()))
                    } else if (explorePage?.newReleaseAlbums?.isNotEmpty() == true) {
                        val album = explorePage?.newReleaseAlbums!!.random()
                        playerConnection.playQueue(YouTubeAlbumRadio(album.playlistId))
                    }
                },
            )
        }
    }
}
