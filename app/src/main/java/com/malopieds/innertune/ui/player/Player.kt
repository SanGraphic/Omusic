package com.malopieds.innertune.ui.player

import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.ColorUtils
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.malopieds.innertune.LocalDatabase
import com.malopieds.innertune.LocalDownloadUtil
import com.malopieds.innertune.LocalPlayerConnection
import com.malopieds.innertune.R
import com.malopieds.innertune.constants.DarkModeKey
import com.malopieds.innertune.constants.ListThumbnailSize
import com.malopieds.innertune.constants.PlayerBackgroundStyle
import com.malopieds.innertune.constants.PlayerBackgroundStyleKey
import com.malopieds.innertune.constants.PlayerHorizontalPadding
import com.malopieds.innertune.constants.PlayerTextAlignmentKey
import com.malopieds.innertune.constants.PureBlackKey
import com.malopieds.innertune.constants.QueuePeekHeight
import com.malopieds.innertune.constants.ShowLyricsKey
import com.malopieds.innertune.constants.SliderStyle
import com.malopieds.innertune.constants.SliderStyleKey
import com.malopieds.innertune.constants.ThumbnailCornerRadius
import com.malopieds.innertune.db.entities.PlaylistSongMap
import com.malopieds.innertune.extensions.togglePlayPause
import com.malopieds.innertune.models.MediaMetadata
import com.malopieds.innertune.playback.ExoDownloadService
import com.malopieds.innertune.ui.component.BottomSheet
import com.malopieds.innertune.ui.component.BottomSheetState
import com.malopieds.innertune.ui.component.ListDialog
import com.malopieds.innertune.ui.component.ListItem
import com.malopieds.innertune.ui.component.LocalMenuState
import com.malopieds.innertune.ui.component.ResizableIconButton
import com.malopieds.innertune.ui.component.rememberBottomSheetState
import com.malopieds.innertune.ui.menu.AddToPlaylistDialog
import com.malopieds.innertune.ui.menu.PlayerMenu
import com.malopieds.innertune.ui.screens.settings.DarkMode
import com.malopieds.innertune.ui.screens.settings.PlayerTextAlignment
import com.malopieds.innertune.ui.theme.extractGradientColors
import com.malopieds.innertune.utils.joinByBullet
import com.malopieds.innertune.utils.makeTimeString
import com.malopieds.innertune.utils.rememberEnumPreference
import com.malopieds.innertune.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import me.saket.squiggles.SquigglySlider
import java.time.LocalDateTime
import kotlin.math.roundToInt
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.widthIn
import com.malopieds.innertune.ui.component.Lyrics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.fillMaxHeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current

    val clipboardManager = LocalClipboardManager.current

    val playerConnection = LocalPlayerConnection.current ?: return

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val pureBlack by rememberPreference(PureBlackKey, defaultValue = false)
    val playerBackground by rememberEnumPreference(PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.DEFAULT)
    val useBlackBackground =
        remember(isSystemInDarkTheme, darkTheme, pureBlack, playerBackground) {
            val useDarkTheme = if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
            val usePureBlackPlayerBackground = (playerBackground == PlayerBackgroundStyle.DEFAULT)
            useDarkTheme && pureBlack && usePureBlackPlayerBackground
        }

    val playerTextAlignment by rememberEnumPreference(PlayerTextAlignmentKey, PlayerTextAlignment.SIDED)

    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val automix by playerConnection.service.automixItems.collectAsState()

    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    var showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)

    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.DEFAULT)

    var position by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.currentPosition)
    }
    var duration by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.duration)
    }
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }

    var gradientColors by remember {
        mutableStateOf<List<Color>>(emptyList())
    }

    var changeColor by remember {
        mutableStateOf(false)
    }

    if (!canSkipNext && automix.isNotEmpty()) {
        playerConnection.service.addToQueueAutomix(automix[0], 0)
    }

    LaunchedEffect(mediaMetadata, playerBackground, darkTheme, useBlackBackground) {
        if (useBlackBackground) {
            gradientColors = listOf(Color.Black, Color.Black)
        } else if (playerBackground == PlayerBackgroundStyle.GRADIENT) {
            withContext(Dispatchers.IO) {
                val result =
                    (
                        ImageLoader(context)
                            .execute(
                                ImageRequest
                                    .Builder(context)
                                    .data(mediaMetadata?.thumbnailUrl)
                                    .allowHardware(false)
                                    .build(),
                            ).drawable as? BitmapDrawable
                    )?.bitmap?.extractGradientColors(
                        darkTheme =
                            darkTheme == DarkMode.ON || (darkTheme == DarkMode.AUTO && isSystemInDarkTheme),
                    )

                result?.let {
                    gradientColors = it
                }
            }
        } else {
            gradientColors = emptyList()
        }
    }

    val changeBound = state.expandedBound / 3

    val hasGradientColours : Boolean = gradientColors.size >= 2
    val whiteContrastThreshold = 2.0
    val blackContrastThreshold = 2.0

    val whiteGradientContrast : Double = when {
        hasGradientColours -> {
            ColorUtils.calculateContrast(
                gradientColors.first().toArgb(),
                MaterialTheme.colorScheme.onBackground.toArgb(),
            )
        }
        else -> whiteContrastThreshold
    }

    val blackGradientContrast : Double = when {
        hasGradientColours -> {
            ColorUtils.calculateContrast(
                gradientColors.last().toArgb(),
                MaterialTheme.colorScheme.onBackground.toArgb(),
            )
        }
        else -> blackContrastThreshold
    }

    val onBackgroundColor =
        when (playerBackground) {
            PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.onBackground
            else -> {
                when {
                    hasGradientColours &&
                            whiteGradientContrast < whiteContrastThreshold &&
                            blackGradientContrast > blackContrastThreshold -> {
                        changeColor = true
                        MaterialTheme.colorScheme.onBackground
                    }
                    hasGradientColours &&
                            whiteGradientContrast > whiteContrastThreshold &&
                            blackGradientContrast < blackContrastThreshold -> {
                        changeColor = true
                        MaterialTheme.colorScheme.onBackground
                    }
                    else -> {
                        changeColor = false
                        MaterialTheme.colorScheme.onSurface
                    }
                }
            }
        }

    val download by LocalDownloadUtil.current.getDownload(mediaMetadata?.id ?: "").collectAsState(initial = null)

    val sleepTimerEnabled =
        remember(playerConnection.service.sleepTimer.triggerTime, playerConnection.service.sleepTimer.pauseWhenSongEnd) {
            playerConnection.service.sleepTimer.isActive
        }

    var sleepTimerTimeLeft by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft =
                    if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                        playerConnection.player.duration - playerConnection.player.currentPosition
                    } else {
                        playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                    }
                delay(1000L)
            }
        }
    }

    var showSleepTimerDialog by remember {
        mutableStateOf(false)
    }

    var sleepTimerValue by remember {
        mutableFloatStateOf(30f)
    }
    if (showSleepTimerDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showSleepTimerDialog = false },
            icon = { Icon(painter = painterResource(R.drawable.bedtime), contentDescription = null) },
            title = { Text(stringResource(R.string.sleep_timer)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSleepTimerDialog = false
                        playerConnection.service.sleepTimer.start(sleepTimerValue.roundToInt())
                    },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSleepTimerDialog = false },
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pluralStringResource(R.plurals.minute, sleepTimerValue.roundToInt(), sleepTimerValue.roundToInt()),
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Slider(
                        value = sleepTimerValue,
                        onValueChange = { sleepTimerValue = it },
                        valueRange = 5f..120f,
                        steps = (120 - 5) / 5 - 1,
                    )

                    OutlinedButton(
                        onClick = {
                            showSleepTimerDialog = false
                            playerConnection.service.sleepTimer.start(-1)
                        },
                    ) {
                        Text(stringResource(R.string.end_of_song))
                    }
                }
            },
        )
    }

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showErrorPlaylistAddDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToPlaylistDialog(
        isVisible = showChoosePlaylistDialog,
        onAdd = { playlist ->
            database.transaction {
                mediaMetadata?.let {
                    insert(it)
                    if (checkInPlaylist(playlist.id, it.id) == 0) {
                        insert(
                            PlaylistSongMap(
                                songId = it.id,
                                playlistId = playlist.id,
                                position = playlist.songCount,
                            ),
                        )
                        update(playlist.playlist.copy(lastUpdateTime = LocalDateTime.now()))
                    } else {
                        showErrorPlaylistAddDialog = true
                    }
                }
            }
        },
        onDismiss = {
            showChoosePlaylistDialog = false
        },
    )

    if (showErrorPlaylistAddDialog && mediaMetadata != null) {
        ListDialog(
            onDismiss = {
                showErrorPlaylistAddDialog = false
            },
        ) {
            item {
                ListItem(
                    title = stringResource(R.string.already_in_playlist),
                    thumbnailContent = {
                        Image(
                            painter = painterResource(R.drawable.close),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.size(ListThumbnailSize),
                        )
                    },
                    modifier =
                        Modifier
                            .clickable { showErrorPlaylistAddDialog = false },
                )
            }

            items(listOf(mediaMetadata)) { song ->
                ListItem(
                    title = song!!.title,
                    thumbnailContent = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(ListThumbnailSize),
                        ) {
                            AsyncImage(
                                model = song.thumbnailUrl,
                                contentDescription = null,
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                            )
                        }
                    },
                    subtitle =
                        joinByBullet(
                            song.artists.joinToString { it.name },
                            makeTimeString(song.duration * 1000L),
                        ),
                )
            }
        }
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(100)
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
            }
        }
    }

    val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)

    var showDetailsDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showDetailsDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showDetailsDialog = false },
            containerColor = if (useBlackBackground) Color.Black else AlertDialogDefaults.containerColor,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.info),
                    contentDescription = null,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showDetailsDialog = false },
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            text = {
                Column(
                    modifier =
                        Modifier
                            .sizeIn(minWidth = 280.dp, maxWidth = 560.dp)
                            .verticalScroll(rememberScrollState()),
                ) {
                    listOf(
                        stringResource(R.string.song_title) to mediaMetadata?.title,
                        stringResource(R.string.song_artists) to mediaMetadata?.artists?.joinToString { it.name },
                        stringResource(R.string.media_id) to mediaMetadata?.id,
                        "Itag" to currentFormat?.itag?.toString(),
                        stringResource(R.string.mime_type) to currentFormat?.mimeType,
                        stringResource(R.string.codecs) to currentFormat?.codecs,
                        stringResource(R.string.bitrate) to currentFormat?.bitrate?.let { "${it / 1000} Kbps" },
                        stringResource(R.string.sample_rate) to currentFormat?.sampleRate?.let { "$it Hz" },
                        stringResource(R.string.loudness) to currentFormat?.loudnessDb?.let { "$it dB" },
                        stringResource(R.string.volume) to "${(playerConnection.player.volume * 100).toInt()}%",
                        stringResource(R.string.file_size) to
                            currentFormat?.contentLength?.let { Formatter.formatShortFileSize(context, it) },
                    ).forEach { (label, text) ->
                        val displayText = text ?: stringResource(R.string.unknown)
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.titleMedium,
                            modifier =
                                Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(displayText))
                                        Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                                    },
                                ),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            },
        )
    }

    val queueSheetState =
        rememberBottomSheetState(
            dismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
            expandedBound = state.expandedBound,
        )

    BottomSheet(
        state = state,
        modifier = modifier,
        brushBackgroundColor =
            if (hasGradientColours &&
                state.value > changeBound
            ) {
                Brush.verticalGradient(gradientColors)
            } else {
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.surfaceContainer,
                    ),
                )
            },
        onDismiss = {
            playerConnection.service.clearAutomix()
            playerConnection.player.stop()
            playerConnection.player.clearMediaItems()
        },
        collapsedContent = {
            MiniPlayer(
                position = position,
                duration = duration,
            )
        },
    ) {
        val controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit = { mediaMetadata ->
            val playPauseRoundness by animateDpAsState(
                targetValue = if (isPlaying) 24.dp else 36.dp,
                animationSpec = tween(durationMillis = 90, easing = LinearEasing),
                label = "playPauseRoundness",
            )

            Row(
                horizontalArrangement =
                    when (playerTextAlignment) {
                        PlayerTextAlignment.SIDED -> Arrangement.Start
                        PlayerTextAlignment.CENTER -> Arrangement.Center
                    },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PlayerHorizontalPadding),
            ) {
                AnimatedContent(
                    targetState = mediaMetadata.title,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "",
                ) { title ->
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        fontSize = 22.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = onBackgroundColor,
                        modifier =
                            Modifier
                                .basicMarquee()
                                .clickable(enabled = mediaMetadata.album != null) {
                                    navController.navigate("album/${mediaMetadata.album!!.id}")
                                    state.collapseSoft()
                                },
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Row(
                horizontalArrangement =
                    when (playerTextAlignment) {
                        PlayerTextAlignment.SIDED -> Arrangement.Start
                        PlayerTextAlignment.CENTER -> Arrangement.Center
                    },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PlayerHorizontalPadding),
            ) {
                mediaMetadata.artists.fastForEachIndexed { index, artist ->
                    AnimatedContent(
                        targetState = artist.name,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "",
                    ) { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            fontSize = 14.sp,
                            color = onBackgroundColor,
                            maxLines = 1,
                            modifier =
                                Modifier.clickable(enabled = artist.id != null) {
                                    artist.id?.let { id ->
                                        navController.navigate("artist/$id")
                                        state.collapseSoft()
                                    }
                                },
                        )
                    }

                    if (index != mediaMetadata.artists.lastIndex) {
                        AnimatedContent(
                            targetState = ", ",
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "",
                        ) { comma ->
                            Text(
                                text = comma,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                fontSize = 14.sp,
                                color = onBackgroundColor,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PlayerHorizontalPadding),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                                playerConnection.service.startRadioSeamlessly()
                            },
                ) {
                    Image(
                        painter = painterResource(R.drawable.radio),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Box(
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                                if (download?.state == Download.STATE_COMPLETED) {
                                    DownloadService.sendRemoveDownload(
                                        context,
                                        ExoDownloadService::class.java,
                                        mediaMetadata.id,
                                        false,
                                    )
                                } else {
                                    database.transaction {
                                        insert(mediaMetadata)
                                    }
                                    val downloadRequest =
                                        DownloadRequest
                                            .Builder(mediaMetadata.id, mediaMetadata.id.toUri())
                                            .setCustomCacheKey(mediaMetadata.id)
                                            .setData(mediaMetadata.title.toByteArray())
                                            .build()
                                    DownloadService.sendAddDownload(
                                        context,
                                        ExoDownloadService::class.java,
                                        downloadRequest,
                                        false,
                                    )
                                }
                            },
                ) {
                    Image(
                        painter =
                            painterResource(
                                if (download?.state ==
                                    Download.STATE_COMPLETED
                                ) {
                                    R.drawable.offline
                                } else {
                                    R.drawable.download
                                },
                            ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Box(
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                                showChoosePlaylistDialog = true
                            },
                ) {
                    Image(
                        painter = painterResource(R.drawable.playlist_add),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    AnimatedContent(
                        label = "sleepTimer",
                        targetState = sleepTimerEnabled,
                    ) { sleepTimerEnabled ->
                        if (sleepTimerEnabled) {
                            Text(
                                text = makeTimeString(sleepTimerTimeLeft),
                                style = MaterialTheme.typography.labelLarge,
                                color = onBackgroundColor,
                                maxLines = 1,
                                modifier =
                                    Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable(onClick = playerConnection.service.sleepTimer::clear)
                                        .basicMarquee(),
                            )
                        } else {
                            Box(
                                modifier =
                                    Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer)
                                        .clickable {
                                            showSleepTimerDialog = true
                                        },
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.bedtime),
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                                    contentDescription = null,
                                    modifier =
                                        Modifier
                                            .align(Alignment.Center)
                                            .size(24.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable {
                                menuState.show {
                                    PlayerMenu(
                                        mediaMetadata = mediaMetadata,
                                        navController = navController,
                                        playerBottomSheetState = state,
                                        onShowDetailsDialog = { showDetailsDialog = true },
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                ) {
                    Image(
                        painter = painterResource(R.drawable.more_horiz),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            when (sliderStyle) {
                SliderStyle.SQUIGGLY -> {
                    SquigglySlider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            sliderPosition = it.toLong()
                        },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                playerConnection.player.seekTo(it)
                                position = it
                            }
                            sliderPosition = null
                        },
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                        squigglesSpec =
                            SquigglySlider.SquigglesSpec(
                                amplitude = if (isPlaying) (2.dp).coerceAtLeast(2.dp) else 0.dp,
                                strokeWidth = 3.dp,
                            ),
                    )
                }

                SliderStyle.DEFAULT -> {
                    Slider(
                        value = (sliderPosition ?: position).toFloat(),
                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                        onValueChange = {
                            sliderPosition = it.toLong()
                        },
                        onValueChangeFinished = {
                            sliderPosition?.let {
                                playerConnection.player.seekTo(it)
                                position = it
                            }
                            sliderPosition = null
                        },
                        modifier = Modifier.padding(horizontal = PlayerHorizontalPadding),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PlayerHorizontalPadding + 4.dp),
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: position),
                    style = MaterialTheme.typography.labelMedium,
                    color = onBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = onBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .size(width = 72.dp, height = 56.dp)
                        .clickable { playerConnection.toggleLike() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            if (currentSong?.song?.liked == true) R.drawable.favorite else R.drawable.favorite_border
                        ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(if (currentSong?.song?.liked == true) MaterialTheme.colorScheme.error else Color.White),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .size(width = 72.dp, height = 56.dp)
                        .clickable { showLyrics = !showLyrics },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.lyrics),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(if (showLyrics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .size(width = 72.dp, height = 56.dp)
                        .clickable {
                            if (download?.state == Download.STATE_COMPLETED) {
                                DownloadService.sendRemoveDownload(
                                    context,
                                    ExoDownloadService::class.java,
                                    mediaMetadata!!.id,
                                    false,
                                )
                            } else {
                                database.transaction {
                                    insert(mediaMetadata!!)
                                }
                                val downloadRequest =
                                    DownloadRequest
                                        .Builder(mediaMetadata!!.id, mediaMetadata!!.id.toUri())
                                        .setCustomCacheKey(mediaMetadata!!.id)
                                        .setData(mediaMetadata!!.title.toByteArray())
                                        .build()
                                DownloadService.sendAddDownload(
                                    context,
                                    ExoDownloadService::class.java,
                                    downloadRequest,
                                    false,
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            if (download?.state == Download.STATE_COMPLETED) R.drawable.offline else R.drawable.download
                        ),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .size(width = 72.dp, height = 56.dp)
                        .clickable { showChoosePlaylistDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.playlist_add),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Row(
                    modifier =
                        Modifier
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                            .padding(bottom = queueSheetState.collapsedBound),
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f),
                    ) {
                        Thumbnail(
                            sliderPositionProvider = { sliderPosition },
                            modifier = Modifier.nestedScroll(state.preUpPostDownNestedScrollConnection),
                            changeColor = changeColor,
                            color = onBackgroundColor,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier =
                            Modifier
                                .weight(1f)
                                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
                    ) {
                        Spacer(Modifier.weight(1f))

                        mediaMetadata?.let {
                            controlsContent(it)
                        }

                        Spacer(Modifier.weight(1f))
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                        .padding(bottom = queueSheetState.collapsedBound)
                ) {
                    mediaMetadata?.thumbnailUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize().fillMaxSize().alpha(0.7f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.80f),
                                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )

                    if (!showLyrics) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(top = 50.dp, start = 24.dp, end = 24.dp, bottom = 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            mediaMetadata?.thumbnailUrl?.let { url ->
                                Box(
                                    modifier = Modifier
                                        .size(210.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                            mediaMetadata?.let {
                                Text(
                                    text = it.title,
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 18.sp,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    it.artists.forEachIndexed { index, artist ->
                                        Text(
                                            text = artist.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .clickable(enabled = artist.id != null) {
                                                    artist.id?.let { id ->
                                                        navController.navigate("artist/$id")
                                                        state.collapseSoft()
                                                    }
                                                }
                                        )
                                        if (index != it.artists.lastIndex) {
                                            Text(
                                                text = ", ",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                                fontSize = 12.sp,
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                            val playPauseRoundness by animateDpAsState(
                                targetValue = if (isPlaying) 16.dp else 52.dp,
                                animationSpec = tween(durationMillis = 90, easing = LinearEasing),
                                label = "playPauseRoundnessPortrait",
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSystemInDarkTheme) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.onBackground
                                        )
                                        .clickable(enabled = canSkipPrevious) { playerConnection.seekToPrevious() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.skip_previous),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.White),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(20.dp))
                                Box(
                                    modifier = Modifier
                                        .size(78.dp)
                                        .clip(RoundedCornerShape(playPauseRoundness))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            if (playbackState == STATE_ENDED) {
                                                playerConnection.player.seekTo(0, 0)
                                                playerConnection.player.playWhenReady = true
                                            } else {
                                                playerConnection.player.togglePlayPause()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(
                                            if (playbackState == STATE_ENDED) R.drawable.replay
                                            else if (isPlaying) R.drawable.pause
                                            else R.drawable.play
                                        ),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.White),
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                                Spacer(Modifier.width(20.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSystemInDarkTheme) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.onBackground
                                        )
                                        .clickable(enabled = canSkipNext) { playerConnection.seekToNext() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.skip_next),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.White),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                            when (sliderStyle) {
                                SliderStyle.SQUIGGLY -> {
                                    SquigglySlider(
                                        value = (sliderPosition ?: position).toFloat(),
                                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                        onValueChange = {
                                            sliderPosition = it.toLong()
                                        },
                                        onValueChangeFinished = {
                                            sliderPosition?.let {
                                                playerConnection.player.seekTo(it)
                                                position = it
                                            }
                                            sliderPosition = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        squigglesSpec =
                                            SquigglySlider.SquigglesSpec(
                                                amplitude = if (isPlaying) (2.dp).coerceAtLeast(2.dp) else 0.dp,
                                                strokeWidth = 3.dp,
                                            ),
                                    )
                                }
                                SliderStyle.DEFAULT -> {
                                    Slider(
                                        value = (sliderPosition ?: position).toFloat(),
                                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                        onValueChange = {
                                            sliderPosition = it.toLong()
                                        },
                                        onValueChangeFinished = {
                                            sliderPosition?.let {
                                                playerConnection.player.seekTo(it)
                                                position = it
                                            }
                                            sliderPosition = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = PlayerHorizontalPadding + 4.dp),
                            ) {
                                Text(
                                    text = makeTimeString(sliderPosition ?: position),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Text(
                                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .fillMaxWidth()
                                .padding(top = 50.dp, start = 24.dp, end = 24.dp, bottom = 0.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                mediaMetadata?.thumbnailUrl?.let { url ->
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    mediaMetadata?.let {
                                        Text(
                                            text = it.title,
                                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onBackground,
                                            fontSize = 18.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            it.artists.forEachIndexed { index, artist ->
                                                Text(
                                                    text = artist.name,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier
                                                        .clickable(enabled = artist.id != null) {
                                                            artist.id?.let { id ->
                                                                navController.navigate("artist/$id")
                                                                state.collapseSoft()
                                                            }
                                                        }
                                                )
                                                if (index != it.artists.lastIndex) {
                                                    Text(
                                                        text = ", ",
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                                        fontSize = 12.sp,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(0.515f)
                                    .padding(vertical = 8.dp)
                            ) {
                                Lyrics(
                                    sliderPositionProvider = { sliderPosition },
                                    changeColor = false,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 16.sp,
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            val playPauseRoundness by animateDpAsState(
                                targetValue = if (isPlaying) 16.dp else 52.dp,
                                animationSpec = tween(durationMillis = 90, easing = LinearEasing),
                                label = "playPauseRoundnessPortraitLyrics",
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSystemInDarkTheme) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.onBackground
                                        )
                                        .clickable(enabled = canSkipPrevious) { playerConnection.seekToPrevious() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.skip_previous),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.White),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(Modifier.width(20.dp))
                                Box(
                                    modifier = Modifier
                                        .size(78.dp)
                                        .clip(RoundedCornerShape(playPauseRoundness))
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            if (playbackState == STATE_ENDED) {
                                                playerConnection.player.seekTo(0, 0)
                                                playerConnection.player.playWhenReady = true
                                            } else {
                                                playerConnection.player.togglePlayPause()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(
                                            if (playbackState == STATE_ENDED) R.drawable.replay
                                            else if (isPlaying) R.drawable.pause
                                            else R.drawable.play
                                        ),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.White),
                                        modifier = Modifier.size(56.dp)
                                    )
                                }
                                Spacer(Modifier.width(20.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSystemInDarkTheme) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.onBackground
                                        )
                                        .clickable(enabled = canSkipNext) { playerConnection.seekToNext() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.skip_next),
                                        contentDescription = null,
                                        colorFilter = ColorFilter.tint(Color.White),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                            when (sliderStyle) {
                                SliderStyle.SQUIGGLY -> {
                                    SquigglySlider(
                                        value = (sliderPosition ?: position).toFloat(),
                                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                        onValueChange = {
                                            sliderPosition = it.toLong()
                                        },
                                        onValueChangeFinished = {
                                            sliderPosition?.let {
                                                playerConnection.player.seekTo(it)
                                                position = it
                                            }
                                            sliderPosition = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        squigglesSpec =
                                            SquigglySlider.SquigglesSpec(
                                                amplitude = if (isPlaying) (2.dp).coerceAtLeast(2.dp) else 0.dp,
                                                strokeWidth = 3.dp,
                                            ),
                                    )
                                }
                                SliderStyle.DEFAULT -> {
                                    Slider(
                                        value = (sliderPosition ?: position).toFloat(),
                                        valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                                        onValueChange = {
                                            sliderPosition = it.toLong()
                                        },
                                        onValueChangeFinished = {
                                            sliderPosition?.let {
                                                playerConnection.player.seekTo(it)
                                                position = it
                                            }
                                            sliderPosition = null
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = PlayerHorizontalPadding + 4.dp),
                            ) {
                                Text(
                                    text = makeTimeString(sliderPosition ?: position),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Text(
                                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.surfaceContainer
                                    )
                                )
                            )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .size(width = 72.dp, height = 56.dp)
                                .clickable { playerConnection.toggleLike() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    if (currentSong?.song?.liked == true) R.drawable.favorite else R.drawable.favorite_border
                                ),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(if (currentSong?.song?.liked == true) MaterialTheme.colorScheme.error else Color.White),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .size(width = 72.dp, height = 56.dp)
                                .clickable { showLyrics = !showLyrics },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.lyrics),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(if (showLyrics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .size(width = 72.dp, height = 56.dp)
                                .clickable {
                                    if (download?.state == Download.STATE_COMPLETED) {
                                        DownloadService.sendRemoveDownload(
                                            context,
                                            ExoDownloadService::class.java,
                                            mediaMetadata!!.id,
                                            false,
                                        )
                                    } else {
                                        database.transaction {
                                            insert(mediaMetadata!!)
                                        }
                                        val downloadRequest =
                                            DownloadRequest
                                                .Builder(mediaMetadata!!.id, mediaMetadata!!.id.toUri())
                                                .setCustomCacheKey(mediaMetadata!!.id)
                                                .setData(mediaMetadata!!.title.toByteArray())
                                                .build()
                                        DownloadService.sendAddDownload(
                                            context,
                                            ExoDownloadService::class.java,
                                            downloadRequest,
                                            false,
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    if (download?.state == Download.STATE_COMPLETED) R.drawable.offline else R.drawable.download
                                ),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .size(width = 72.dp, height = 56.dp)
                                .clickable { showChoosePlaylistDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.playlist_add),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 50.dp, end = 24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(50))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable {
                                    menuState.show {
                                        PlayerMenu(
                                            mediaMetadata = mediaMetadata,
                                            navController = navController,
                                            playerBottomSheetState = state,
                                            onShowDetailsDialog = {},
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_horiz),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        Queue(
            state = queueSheetState,
            playerBottomSheetState = state,
            navController = navController,
            backgroundColor =
                if (useBlackBackground) {
                    Color.Black
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            onBackgroundColor = onBackgroundColor,
        )
    }
}
