package com.mrikso.anitube.app.ui.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.util.Rational;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.Format;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.common.TrackSelectionParameters;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.ActivityPlayerBinding;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.repository.ListRepository;
import com.mrikso.anitube.app.ui.dialogs.UnsupportedVideoSourceDialog;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.utils.PreferencesHelper;
import com.mrikso.anitube.app.utils.ReadableTime;
import com.mrikso.anitube.app.utils.ViewUtils;
import com.mrikso.anitube.app.view.CustomAutoCompleteTextView;
import com.mrikso.anitube.app.viewmodel.SharedViewModel;
import com.mrikso.player.CustomPlayerView;
import com.mrikso.player.ExoMediaSourceHelper;
import com.mrikso.player.dtpv.DoubleTapPlayerView;
import com.mrikso.player.dtpv.youtube.YouTubeOverlay;
import com.mrikso.player.utils.BrightnessControl;
import com.mrikso.player.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;
import io.github.anilbeesetti.nextlib.media3ext.ffdecoder.NextRenderersFactory;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

@AndroidEntryPoint
@OptIn(markerClass = UnstableApi.class)
public class PlayerActivity extends AppCompatActivity {
    private final CompositeDisposable disposables = new CompositeDisposable();

    // constants for broadcast receiver
    public static final String ACTION_BROADCAST_CONTROL = "ACTION_BROADCAST_CONTROL";

    // Intent extra for broadcast controls from Picture-in-Picture mode.
    public static final String PLAYBACK_ACTION = "PLAYER_PLAYBACK";
    public static final int PLAYBACK_ACTION_RESUME = 1;
    public static final int PLAYBACK_ACTION_PAUSE = 2;
    public static final int PLAYBACK_ACTION_NEXT = 3;
    public static final int PLAYBACK_ACTION_PREV = 4;

    private static final String EMPTY_STRING_VALUE = "~";
    private static final int EMPTY_EPISODE_NUMBER_VALUE = 0;

    private static final String KEY_ITEM_INDEX = "item_index";
    private static final String KEY_POSITION = "position";
    private static final String KEY_AUTO_PLAY = "auto_play";
    private ActivityPlayerBinding binding;
    private ExoPlayer exoPlayer = null;
    // Top buttons
    private TextView exoEpisodeNumber;
    private TextView exoAnimeTitle;
    private ImageView backButton;
    private TextInputLayout exoQualityTil;
    private CustomAutoCompleteTextView exoQuality;
    private ImageView exoSpeed;
    private ImageView exoLock;

    // Middle buttons
    private ImageView exoPrevEp;
    //  private CardView cvPlay;
    private ImageView exoPlay;
    private ProgressBar exoProgressBar;
    private ImageView exoNextEp;

    // Bottom buttons
    private ImageButton exoPip;
    private ImageButton exoSkip;
    private ImageButton exoScreen;

    private LinearLayout exoTopControllers;
    private LinearLayout exoMiddleControllers;
    private LinearLayout exoBottomControllers;

    private TrackSelectionParameters trackSelectionParameters;
    private ExoMediaSourceHelper mediaSourceHelper;
    public CustomPlayerView playerView;
    private YouTubeOverlay youTubeOverlay;
    final Rational rationalLimitWide = new Rational(239, 100);
    final Rational rationalLimitTall = new Rational(100, 239);

    // player status
    private boolean isLock = false;
    private boolean isFullScreen = false;

    private boolean restoreControllerTimeout = false;
    public static long[] chapterStarts;

    private int currentSpeedIndex = 1;
    // episodes variables
    private String animeTitle = EMPTY_STRING_VALUE;
    private String animeKind = EMPTY_STRING_VALUE;
    private VideoLinksModel episodeLinks;
    private ListRepository listRepo;
    private int episodeNumber = EMPTY_EPISODE_NUMBER_VALUE;
    private String currentQuality;
    private long currentPosition;
    private AlertDialog progressDialog;
    private SharedViewModel sharedViewModel;
    private PlayerViewModel playerViewModel;
    private BaseAnimeModel animeModel;
    private String episodePath;
    private int doubleTapSeek;
    private int fastSeek;
    private boolean autoPlayNextEpisode;
    private boolean gesturesEnabled;
    private boolean autoContinue;
    private boolean isContinueDialogShowing = false;
    private BroadcastReceiver broadcastReceiver;

    private long lastSavedPosition = -1;
    private int lastSavedEpisode = -1;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSettings();
        setView();
        initViewModel();

        setupViews();

        // if (savedInstanceState == null) {
        hideNavBar();
        parseExtra();
        preparePlayer(false);
        setClickListeners();
        setupTextViewValues();
        setupQuality();
        setupMiddleControllers();
        //  }
        if (savedInstanceState != null) {
            currentPosition = savedInstanceState.getLong(KEY_POSITION, 0);
        }
    }

    private void setView() {
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        playerView = binding.videoView;
    }

    private void initViewModel() {
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        playerViewModel = new ViewModelProvider(this).get(PlayerViewModel.class);
        listRepo = ListRepository.getInstance();
    }

    private void setupViews() {
        exoTopControllers = findViewById(R.id.exo_top_controllers);
        exoMiddleControllers = findViewById(R.id.exo_middle_controllers);
        exoBottomControllers = findViewById(R.id.exo_bottom_controllers);

        // Top buttons
        backButton = findViewById(R.id.exo_back);
        exoEpisodeNumber = findViewById(R.id.exo_episode_number);
        exoAnimeTitle = findViewById(R.id.exo_anime_title);
        exoQualityTil = findViewById(R.id.exo_quality_til);
        exoQuality = findViewById(R.id.exo_quality);
        exoSpeed = findViewById(R.id.exo_speed);
        exoLock = findViewById(R.id.exo_lock);

        // Middle buttons
        exoPrevEp = findViewById(R.id.exo_prev_ep);
        //cvPlay = findViewById(R.id.cv_exo_play_pause);
        exoPlay = findViewById(R.id.ib_exo_play_pause);
        exoProgressBar = findViewById(R.id.exo_progress_bar);
        exoNextEp = findViewById(R.id.exo_next_ep);

        // Bottom buttons
        exoPip = findViewById(R.id.exo_pip);
        exoSkip = findViewById(R.id.exo_quick_skip);
        if (!(fastSeek > 0)) {
            exoSkip.setVisibility(View.GONE);
        }
        exoScreen = findViewById(R.id.exo_screen);

        youTubeOverlay = findViewById(R.id.youtube_overlay);

        youTubeOverlay.performListener(new YouTubeOverlay.PerformListener() {
            @Override
            public void onAnimationStart() {
                youTubeOverlay.setAlpha(1.0f);
                youTubeOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd() {
                youTubeOverlay.animate().alpha(0.0f).setDuration(300).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        youTubeOverlay.setVisibility(View.GONE);
                        youTubeOverlay.setAlpha(1.0f);
                    }
                });
            }
        });

        if (!Utils.isPiPSupported(this)) {
            exoPip.setVisibility(View.GONE);
            // TODO: Android 12 improvements:
            // https://developer.android.com/about/versions/12/features/pip-improvements
            //  boolean success = updatePictureInPictureActions(R.drawable.ic_play_arrow_24dp,
            // R.string.exo_controls_play_description, CONTROL_TYPE_PLAY, REQUEST_PLAY);
        }
    }

    private void preparePlayer(boolean restorePlayer) {
        chapterStarts = new long[0];
        mediaSourceHelper = ExoMediaSourceHelper.getInstance(this);
        BrightnessControl mBrightnessControl = new BrightnessControl(this);
        // if (mPrefs.brightness >= 0) {
        // mBrightnessControl.currentBrightnessLevel = mPrefs.brightness;
        //
        // mBrightnessControl.setScreenBrightness(mBrightnessControl.levelToBrightness(mBrightnessControl.currentBrightnessLevel));
        // }
        playerView.setBrightnessControl(mBrightnessControl);
        playerView.setRepeatToggleModes(Player.REPEAT_MODE_ONE);

        playerView.setControllerHideOnTouch(true);
        playerView.setControllerAutoShow(true);
        playerView.setUseController(true);
        // playerView.setUseArtwork(true);
        //playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER);
        /// playerView.setControllerShowTimeoutMs(-1);

        var doubleTapView = ((DoubleTapPlayerView) playerView);
        if (doubleTapSeek == -1) {
            doubleTapView.setDoubleTapEnabled(false);
        } else {
            doubleTapView.setDoubleTapEnabled(true);
            youTubeOverlay.seekSeconds(doubleTapSeek);
        }

        playerView.setGesturesEnabled(gesturesEnabled);

        playerView.setLocked(isLock);
        var renderersFactory = new NextRenderersFactory(this)
                .setEnableDecoderFallback(true)
                .setExtensionRendererMode(NextRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
        // new DefaultRenderersFactory(this).setEnableDecoderFallback(true);

        //long INCREMENT_MILLIS = doubleTapSeek * 1000L + 1;

        exoPlayer = new ExoPlayer.Builder(this, renderersFactory)
                //.setSeekBackIncrementMs(INCREMENT_MILLIS)
                // .setSeekForwardIncrementMs(INCREMENT_MILLIS)
                .build();
        exoPlayer.setPlayWhenReady(false);
        //  exoPlayer.setTrackSelectionParameters(trackSelectionParameters);
        exoPlayer.addListener(new PlayerEventListener());
        // exoPlayer.addAnalyticsListener(new EventLogger());
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                .build();
        exoPlayer.setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true);
        // exoPlayer.setPlayWhenReady(true);
        exoPlayer.setHandleAudioBecomingNoisy(true);

        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            //controllerVisible = visibility == View.VISIBLE;
            // controllerVisibleFully = playerView.isControllerFullyVisible();
            if (restoreControllerTimeout) {
                restoreControllerTimeout = false;
            }

            if (visibility != View.VISIBLE) {
                exoQuality.dismissDropDown();
            } else {
                if (!isLock) {
                    exoPlay.requestFocus();
                }
            }
        });
        youTubeOverlay.player(exoPlayer);
        playerView.setPlayer(exoPlayer);
        if (!restorePlayer && autoContinue && listRepo.getList().get(episodeNumber - 1).getTotalWatchTime() > 0) {
            isContinueDialogShowing = true;
            currentPosition = 0;
            setMediaSourceByModel(episodeLinks);
            exoPlayer.prepare();
            showContinuePlayDialog(listRepo.getList().get(episodeNumber - 1));
        } else {
            initPlayback(episodeLinks, currentPosition);
        }

        if (Utils.isPiPSupported(this)) {
            setPictureInPictureParams(getPipParams(exoPlayer.isPlaying()));
        }
    }

    private void initPlayback(VideoLinksModel model, long position) {
        isContinueDialogShowing = false;
        currentPosition = position;
        setMediaSourceByModel(model);
        exoPlayer.prepare();
        savePlayer(); // Save to history immediately on start
        playVideo();
    }

    private void setupTextViewValues() {
        exoEpisodeNumber.setText(animeKind);
        exoAnimeTitle.setText(animeTitle);
    }

    private void setupMiddleControllers() {
        // Log.i("TAG", "epnum:" + episodeNumber + "size:" + listRepo.getList().size());

        if (episodeNumber == 1) {
            exoPrevEp.setAlpha(0.4f);
            exoPrevEp.setEnabled(false);
            if (listRepo.getList().size() == 1) {
                exoNextEp.setAlpha(0.4f);
                exoNextEp.setEnabled(false);
            } else {
                exoNextEp.setAlpha(1f);
                exoNextEp.setEnabled(true);
            }
        } else if (episodeNumber == listRepo.getList().size()) {
            exoNextEp.setAlpha(0.4f);
            exoPrevEp.setEnabled(true);
            exoNextEp.setEnabled(false);
        } else {
            exoPrevEp.setEnabled(true);
            exoNextEp.setEnabled(true);
        }
    }

    private void lockScreen(boolean lock) {
        if (lock) {
            exoLock.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock));
            exoTopControllers.setVisibility(View.INVISIBLE);
            exoMiddleControllers.setVisibility(View.INVISIBLE);
            exoBottomControllers.setVisibility(View.INVISIBLE);

        } else {
            exoLock.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_open));
            exoTopControllers.setVisibility(View.VISIBLE);
            exoMiddleControllers.setVisibility(View.VISIBLE);
            exoBottomControllers.setVisibility(View.VISIBLE);
        }

        playerView.setLocked(lock);
        ((DoubleTapPlayerView) playerView).setDoubleTapEnabled(!lock && doubleTapSeek != -1);
    }

    private void playVideo() {
        Glide.with(this).load(R.drawable.anim_play_to_pause).into(exoPlay);
        exoPlayer.play();
    }

    private void pauseVideo() {
        Glide.with(this).load(R.drawable.anim_pause_to_play).into(exoPlay);
        exoPlayer.pause();
    }

    private void playInFullscreen(boolean enable) {
        if (enable) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            exoScreen.setImageResource(R.drawable.ic_close_fullscreen);
        } else {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            exoScreen.setImageResource(R.drawable.ic_open_in_fullscreen);
        }
    }

    private void showSpeedDialog() {
        String[] speeds = {"0.5x", "Звичайна", "1.25x", "1.5x", "1.75x", "2.0x", "2.5x", "3.0x"};
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.set_video_speed);
        builder.setSingleChoiceItems(speeds, currentSpeedIndex, (dialog, which) -> {
            currentSpeedIndex = which;
            PlaybackParameters param = new PlaybackParameters(getSpeedFromIndex(currentSpeedIndex));
            exoPlayer.setPlaybackParameters(param);
            dialog.dismiss();
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setupQuality() {
        if (episodeLinks.getLinksQuality() != null
                && !episodeLinks.getLinksQuality().isEmpty()) {
            exoQuality.setOnClickListener(v -> {
                exoQuality.showDropDown();
            });

            updateQualityArray();
            exoQuality.setOnItemClickListener((parent, view, position, id) -> {
                currentQuality = (String) parent.getAdapter().getItem(position);
                String qualityItem = episodeLinks.getLinksQuality().get(currentQuality);
                if (Strings.isNullOrEmpty(qualityItem)) return;
                currentPosition = Math.max(0, exoPlayer.getCurrentPosition());

                MediaSource mediaSource = mediaSourceHelper.getMediaSource(qualityItem, true);
                exoPlayer.setMediaSource(mediaSource);

                TrackSelectionParameters currentParameters = exoPlayer.getTrackSelectionParameters();
                // Build the resulting parameters.
                TrackSelectionParameters newParameters =
                        currentParameters.buildUpon().setMaxVideoBitrate(10000).build();
                // Set the new parameters.
                exoPlayer.setTrackSelectionParameters(newParameters);

                exoPlayer.seekTo(currentPosition);
            });
        } else {
            exoQualityTil.setVisibility(View.GONE);
        }
    }

    private void releasePlayer() {
        if (exoPlayer != null) {
            savePlayer();
//            exoPlayer.stop();
            exoPlayer.release();
            playerView.setPlayer(/* player= */ null);
            exoPlayer = null;
        }
    }

    private void updateQualityArray() {
        List<String> qualityList =
                new ArrayList<>(episodeLinks.getLinksQuality().keySet());
        ArrayAdapter<String> adapterQuality = new ArrayAdapter<>(this, R.layout.item_quality_speed, qualityList);
        exoQuality.setText(currentQuality);
        // Log.i("tag", "updateQualityArray");
        exoQuality.setAdapter(adapterQuality);
    }

    private void savePlayer() {
        if (exoPlayer != null) {
            long currentPos = Math.max(0, exoPlayer.getCurrentPosition());
            long duration = exoPlayer.getContentDuration();

            // Avoid redundant saves if position hasn't changed significantly (e.g. within 1 second)
            // and it's the same episode.
            if (lastSavedEpisode == episodeNumber && Math.abs(currentPos - lastSavedPosition) < 1000) {
                return;
            }

            lastSavedPosition = currentPos;
            lastSavedEpisode = episodeNumber;

            // Update DB
            sharedViewModel.addOrUpdateWatchedAnime(
                    animeModel, episodePath, episodeNumber, duration, currentPos);
            sharedViewModel.addOrUpdateWatchedEpisode(
                    episodeNumber - 1, true, duration, currentPos, animeModel);

            // Update local repository to keep data consistent when switching episodes
            List<EpisodeModel> episodes = listRepo.getList();
            if (episodes != null && episodeNumber > 0 && episodeNumber <= episodes.size()) {
                EpisodeModel currentEp = episodes.get(episodeNumber - 1);
                currentEp.setTotalWatchTime(currentPos);
                currentEp.setTotalEpisodeTime(duration);
                currentEp.setIsWatched(currentPos >= duration * 0.9); // Mark as watched if 90% done
            }
        }
    }

    private float getSpeedFromIndex(int index) {
        float speedRet = 1.0f;
        switch (index) {
            case 0:
                speedRet = 0.5f;
                break;
            case 1:
                speedRet = 1.0f;
                break;
            case 2:
                speedRet = 1.25f;
                break;
            case 3:
                speedRet = 1.5f;
                break;
            case 4:
                speedRet = 1.75f;
                break;
            case 5:
                speedRet = 2.0f;
                break;
            case 6:
                speedRet = 2.5f;
                break;
            case 7:
                speedRet = 3.0f;
                break;
        }
        return speedRet;
    }

    private void setClickListeners() {
        backButton.setOnClickListener(v -> {
            if (exoPlayer != null) {
                exoPlayer.release();
            }
            finish();
        });

        exoLock.setOnClickListener(v -> {
            isLock = !isLock;
            lockScreen(isLock);
        });
        exoPrevEp.setOnClickListener(v -> playPrevEpisode());

        exoNextEp.setOnClickListener(v -> playNextEpisode());

        exoPlay.setOnClickListener(v -> {
            if (exoPlayer.isPlaying()) {
                pauseVideo();
            } else {
                playVideo();
            }
        });

        exoScreen.setOnClickListener(v -> {
            isFullScreen = !isFullScreen;
            playInFullscreen(isFullScreen);
        });
        exoSpeed.setOnClickListener(v -> {
            showSpeedDialog();
        });
        exoPip.setOnClickListener(v -> {
            enterPiP();
        });
        exoSkip.setOnClickListener(v -> {
            exoPlayer.seekTo(exoPlayer.getCurrentPosition() + fastSeek * 1000L);
        });
    }

    private void playPrevEpisode() {
        if (episodeNumber != 1) {
            savePlayer();
            episodeNumber -= 1;
            if (episodeNumber != 1) {
                exoPrevEp.setAlpha(1f);
            } else {
                exoPrevEp.setAlpha(0.4f);
            }
            exoNextEp.setAlpha(1f);
            exoNextEp.setEnabled(true);
            loadEpisode(listRepo.getList().get(episodeNumber - 1));
        } else {
            exoPrevEp.setAlpha(0.4f);
        }
    }

    private void playNextEpisode() {
        if (episodeNumber == listRepo.getList().size()) {
            return;
        }
        if (episodeNumber < listRepo.getList().size()) {
            savePlayer();
            episodeNumber += 1;
            if (episodeNumber != listRepo.getList().size()) {
                exoNextEp.setAlpha(1f);
            } else {
                exoNextEp.setAlpha(0.4f);
            }
            exoPrevEp.setAlpha(1f);
            exoPrevEp.setEnabled(true);

            loadEpisode(listRepo.getList().get(episodeNumber - 1));
        } else {
            exoNextEp.setAlpha(0.4f);
        }
    }

    private void enterPiP() {
        final AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (AppOpsManager.MODE_ALLOWED
                != appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_PICTURE_IN_PICTURE, Process.myUid(), getPackageName())) {
            final Intent intent = new Intent(
                    "android.settings.PICTURE_IN_PICTURE_SETTINGS", Uri.fromParts("package", getPackageName(), null));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
            return;
        }

        if (exoPlayer == null) {
            return;
        }
        playerView.setUseController(false);
        playerView.setControllerAutoShow(false);
        playerView.hideController();

        // Ensure all UI overlays are hidden for PiP
        exoProgressBar.setVisibility(View.GONE);
        exoTopControllers.setVisibility(View.GONE);
        exoBottomControllers.setVisibility(View.GONE);
        exoMiddleControllers.setVisibility(View.GONE);
        exoLock.setVisibility(View.GONE);

        enterPictureInPictureMode(getPipParams(exoPlayer.isPlaying()));
    }

    protected PictureInPictureParams getPipParams(boolean isPlaying) {
        var pipParams = new PictureInPictureParams.Builder();
        final Format format = exoPlayer.getVideoFormat();

        if (format != null && format.width > 0 && format.height > 0) {
            // https://github.com/google/ExoPlayer/issues/8611
            // TODO: Test/disable on Android 11+
            final View videoSurfaceView = playerView.getVideoSurfaceView();
            if (videoSurfaceView instanceof SurfaceView) {
                ((SurfaceView) videoSurfaceView).getHolder().setFixedSize(format.width, format.height);
            }

            Rational rational = Utils.getRational(format);
            if (Build.VERSION.SDK_INT >= 33
                    && getPackageManager().hasSystemFeature(PackageManager.FEATURE_EXPANDED_PICTURE_IN_PICTURE)
                    && (rational.floatValue() > rationalLimitWide.floatValue()
                    || rational.floatValue() < rationalLimitTall.floatValue())) {
                pipParams.setExpandedAspectRatio(rational);
            }
            if (rational.floatValue() > rationalLimitWide.floatValue())
                rational = rationalLimitWide;
            else if (rational.floatValue() < rationalLimitTall.floatValue())
                rational = rationalLimitTall;

            pipParams.setAspectRatio(rational);
        } else {
            // Fallback to 16:9 if format not yet available
            pipParams.setAspectRatio(new Rational(16, 9));
        }

        List<RemoteAction> actions = new ArrayList<>();

        // Previous Episode Action
        if (episodeNumber > 1) {
            actions.add(new RemoteAction(
                    Icon.createWithResource(this, R.drawable.ic_skip_previous),
                    getString(R.string.previous_episode),
                    getString(R.string.previous_episode),
                    PendingIntent.getBroadcast(
                            this,
                            PLAYBACK_ACTION_PREV,
                            new Intent(ACTION_BROADCAST_CONTROL)
                                    .setPackage(this.getPackageName())
                                    .putExtra(PLAYBACK_ACTION, PLAYBACK_ACTION_PREV),
                            PendingIntent.FLAG_IMMUTABLE)));
        }

        // Play/Pause Action
        var pauseAction = isPlaying ? PLAYBACK_ACTION_PAUSE : PLAYBACK_ACTION_RESUME;
        var pauseIcon = isPlaying ? R.drawable.ic_pause : R.drawable.ic_play_arrow;
        var pauseTitle = isPlaying ? getString(R.string.pause) : getString(R.string.resume);

        actions.add(new RemoteAction(
                Icon.createWithResource(this, pauseIcon),
                pauseTitle, pauseTitle,
                PendingIntent.getBroadcast(
                        this,
                        isPlaying ? 2 : 1,
                        new Intent(ACTION_BROADCAST_CONTROL)
                                .setPackage(this.getPackageName())
                                .putExtra(PLAYBACK_ACTION, pauseAction),
                        PendingIntent.FLAG_IMMUTABLE)));

        // Next Episode Action
        if (listRepo.getList() != null && episodeNumber < listRepo.getList().size()) {
            actions.add(new RemoteAction(
                    Icon.createWithResource(this, R.drawable.ic_skip_next),
                    getString(R.string.next_episode),
                    getString(R.string.next_episode),
                    PendingIntent.getBroadcast(
                            this,
                            PLAYBACK_ACTION_NEXT,
                            new Intent(ACTION_BROADCAST_CONTROL)
                                    .setPackage(this.getPackageName())
                                    .putExtra(PLAYBACK_ACTION, PLAYBACK_ACTION_NEXT),
                            PendingIntent.FLAG_IMMUTABLE)));
        }

        pipParams.setActions(actions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pipParams.setTitle(animeTitle);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pipParams.setAutoEnterEnabled(isPlaying);
        }

        return pipParams.build();
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null || (!Objects.equals(intent.getAction(), ACTION_BROADCAST_CONTROL))) {
                    return;
                }

                if (exoPlayer == null)
                    return;

                switch (intent.getIntExtra(PLAYBACK_ACTION, 0)) {
                    case PLAYBACK_ACTION_RESUME:
                        exoPlayer.play();
                        break;
                    case PLAYBACK_ACTION_PAUSE:
                        exoPlayer.pause();
                        break;
                    case PLAYBACK_ACTION_NEXT:
                        playNextEpisode();
                        break;
                    case PLAYBACK_ACTION_PREV:
                        playPrevEpisode();
                        break;
                }
                // Update PiP params to reflect new state (e.g. play/pause icon or next/prev availability)
                if (isInPip()) {
                    setPictureInPictureParams(getPipParams(exoPlayer.isPlaying()));
                }
            }
        };
    }

    private void registerPipReceiver() {
        if (broadcastReceiver == null) {
            initBroadcastReceiver();
        }
        ContextCompat.registerReceiver(
                this,
                broadcastReceiver,
                new IntentFilter(ACTION_BROADCAST_CONTROL),
                ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterPipReceiver() {
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                // Ignore if not registered
            }
        }
    }

    private boolean isInPip() {
        if (!Utils.isPiPSupported(this))
            return false;
        return isInPictureInPictureMode();
    }

    private void loadEpisode(final EpisodeModel episode) {
        Disposable disposable = sharedViewModel
                .loadData(episode.getEpisodeUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(v -> {
                    if (exoPlayer != null) {
                        exoPlayer.stop();
                        exoPlayer.clearMediaItems();
                    }
                    if (!isInPip()) {
                        progressDialog = DialogUtils.getProDialog(this, R.string.loading_episode);
                    }
                })
                .subscribeWith(new DisposableSingleObserver<Pair<LoadState, VideoLinksModel>>() {
                    @Override
                    public void onSuccess(Pair<LoadState, VideoLinksModel> result) {
                        DialogUtils.cancelDialog(progressDialog);
                        if (result.first == LoadState.DONE) {
                            episodeLinks = result.second;
                            exoEpisodeNumber.setText(episode.getName());
                            if (autoContinue && episode.getTotalWatchTime() > 0) {
                                isContinueDialogShowing = true;
                                currentPosition = 0;
                                setMediaSourceByModel(episodeLinks);
                                exoPlayer.prepare();
                                if (isInPip()) {
                                    isContinueDialogShowing = false;
                                    currentPosition = episode.getTotalWatchTime();
                                    exoPlayer.seekTo(currentPosition);
                                    playVideo();
                                } else {
                                    showContinuePlayDialog(episode);
                                }
                            } else {
                                initPlayback(episodeLinks, 0);
                            }
                        } else {
                            UnsupportedVideoSourceDialog.show(PlayerActivity.this, result.second.getIfRameUrl());
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        DialogUtils.cancelDialog(progressDialog);
                        UnsupportedVideoSourceDialog.show(PlayerActivity.this, episode.getEpisodeUrl());
                        throwable.printStackTrace();
                    }
                });
        disposables.add(disposable);
    }


    private void setMediaSourceByModel(VideoLinksModel model) {
        if (model == null) {
            finish();
            return;
        }
        if (model.isIgnoreSSL()) {
            Utils.bypassSSL();
        }

        MediaSource mediaSource = null;
        if (model.getLinksQuality() != null && !model.getLinksQuality().isEmpty()) {
            Map<String, String> qualitiesMap = model.getLinksQuality();

            if (Strings.isNullOrEmpty(currentQuality) || !qualitiesMap.containsKey(currentQuality)) {
                currentQuality = qualitiesMap.keySet().stream().findFirst().get();
            }

            String playUrl = qualitiesMap.get(currentQuality);
            if (playUrl == null && !qualitiesMap.isEmpty()) {
                currentQuality = qualitiesMap.keySet().iterator().next();
                playUrl = qualitiesMap.get(currentQuality);
            }

            updateQualityArray();
            if (!Strings.isNullOrEmpty(playUrl)) {
                mediaSource = mediaSourceHelper.getMediaSource(playUrl, model.getHeaders(), true);
            }

        } else if (!Strings.isNullOrEmpty(model.getSingleDirectUrl())) {
            mediaSource = mediaSourceHelper.getMediaSource(model.getSingleDirectUrl(), model.getHeaders(), true);
        }
        if (mediaSource != null) {
            exoPlayer.setMediaSource(mediaSource);
            playerView.setHaveMedia(true);
            exoPlayer.seekTo(currentPosition);
        } else {
            ViewUtils.showSnackbar(this, R.string.message_error_video_has_empty_stream_lisnks);
            finish();
        }
    }

    private void hideNavBar() {
        if (binding != null) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(getWindow(), binding.getRoot());
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    private void showContinuePlayDialog(EpisodeModel episode) {
        var pos = episode.getTotalWatchTime();
        DialogUtils.showConfirmation(this, getString(R.string.dialog_continue_title),
                getString(R.string.dialog_continue_playning_summary, ReadableTime.generateTime(pos)),
                () -> {
                    isContinueDialogShowing = false;
                    currentPosition = pos;
                    exoPlayer.seekTo(pos);
                    playVideo();
                },
                () -> {
                    isContinueDialogShowing = false;
                    currentPosition = 0;
                    exoPlayer.seekTo(0);
                    playVideo();
                });
    }

    private void showProgressBarAndControlToggle(boolean isShow) {
        if (isShow) {
            playerView.showController();
            exoProgressBar.setVisibility(View.VISIBLE);
            exoPlay.setVisibility(View.GONE);
            exoNextEp.setVisibility(View.INVISIBLE);
            exoPrevEp.setVisibility(View.INVISIBLE);
            exoTopControllers.setVisibility(View.INVISIBLE);
            exoBottomControllers.setVisibility(View.INVISIBLE);
            exoLock.setVisibility(View.INVISIBLE);
        } else {
            playerView.hideController();
            exoProgressBar.setVisibility(View.GONE);
            exoPlay.setVisibility(View.VISIBLE);
            exoNextEp.setVisibility(View.VISIBLE);
            exoPrevEp.setVisibility(View.VISIBLE);
            exoTopControllers.setVisibility(View.VISIBLE);
            exoBottomControllers.setVisibility(View.VISIBLE);
            exoLock.setVisibility(View.VISIBLE);
        }
    }

    private void parseExtra() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            PlayerActivityArgs args = PlayerActivityArgs.fromBundle(bundle);
            animeTitle = args.getTitle();
            animeKind = args.getSubTitle();
            episodeLinks = args.getEpisodeUrl();
            episodeNumber = args.getEpisodeNumber();
            episodePath = args.getEpisodeSource();
            animeModel = args.getAnimeModel();
        } else {
            finish();
        }
    }

    private void loadSettings() {
        var prefs = PreferencesHelper.getInstance();

        doubleTapSeek = prefs.getPlayerDoubleTapSeek();
        fastSeek = prefs.getPlayerFastSeek();
        autoPlayNextEpisode = prefs.isPlayerEnabledPlayNextEpisode();
        gesturesEnabled = prefs.isPlayerEnabledSwipeControls();
        autoContinue = prefs.isPlayerAutoContinuePlay();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isLock) {
        } else {
            // super.onBackPressed();
            finish();
            // Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        savePlayer();
        outState.putLong(KEY_POSITION, currentPosition);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentPosition = savedInstanceState.getLong(KEY_POSITION);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        savePlayer();
        playerView.setUseController(!isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            registerPipReceiver();
        } else {
            unregisterPipReceiver();
            if (binding != null) {
                hideNavBar();
                // Restore UI controls visibility
                exoTopControllers.setVisibility(View.VISIBLE);
                exoBottomControllers.setVisibility(View.VISIBLE);
                exoMiddleControllers.setVisibility(View.VISIBLE);
                exoLock.setVisibility(View.VISIBLE);
                playerView.setControllerAutoShow(true);
                showProgressBarAndControlToggle(false);
            }
        }
    }

    // Activity input

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (isLock) return true;

        int keyCode = event.getKeyCode();
        int action = event.getAction();

        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (!playerView.isControllerFullyVisible()) {
                    playerView.showController();
                } else {
                    if (exoPlayer.isPlaying()) {
                        pauseVideo();
                    } else {
                        playVideo();
                    }
                }
                return true;
            }

            if (!playerView.isControllerFullyVisible()) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    exoPlayer.seekTo(Math.max(0, exoPlayer.getCurrentPosition() - 10000));
                    playerView.showController();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    exoPlayer.seekTo(Math.min(exoPlayer.getDuration(), exoPlayer.getCurrentPosition() + 10000));
                    playerView.showController();
                    return true;
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    playerView.showController();
                    return true;
                }
            }
        }

        // See whether the player view wants to handle media or DPAD keys events.
        return playerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (playerView != null) {
            playerView.onPause();
        }
        releasePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterPipReceiver();
        releasePlayer();
        binding = null;
        disposables.dispose();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (playerView != null) {
            playerView.onPause();
        }
        //releasePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideNavBar();
        if (exoPlayer == null) {
            preparePlayer(true);
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !isInPictureInPictureMode() && exoPlayer.isPlaying()) {
            enterPiP();
        }
    }

    private class PlayerEventListener implements Player.Listener {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            playerView.setKeepScreenOn(isPlaying);

            setPictureInPictureParams(getPipParams(isPlaying));
            if (!isPlaying) {
                isLock = false;
            }
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            boolean isNearEnd = false;
            final long duration = exoPlayer.getDuration();
            if (duration != C.TIME_UNSET) {
                final long position = exoPlayer.getCurrentPosition();
                if (position + 4000 >= duration) {
                    isNearEnd = true;
                } else {
                    // Last chapter is probably "Credits" chapter
                    final int chapters = chapterStarts.length;
                    if (chapters > 1) {
                        final long lastChapter = chapterStarts[chapters - 1];
                        if (duration - lastChapter < duration / 10 && position > lastChapter) {
                            isNearEnd = true;
                        }
                    }
                }
                playerView.setChapterStarts(chapterStarts);
            }

            switch (playbackState) {
                case Player.STATE_BUFFERING:
                case Player.STATE_IDLE:
                    showProgressBarAndControlToggle(true);
                    break;
                case Player.STATE_READY:
                    if (!isContinueDialogShowing) {
                        showProgressBarAndControlToggle(false);
                    }
                    break;
                case Player.STATE_ENDED:
                    if (autoPlayNextEpisode) {
                        playNextEpisode();
                    } else {
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onIsLoadingChanged(boolean isLoading) {
            if (isLoading) {
                if (exoPlayer.isCurrentMediaItemSeekable()
                        && !exoPlayer.isPlaying()
                        && exoPlayer.getBufferedPosition() == 0L) {
                    exoProgressBar.setVisibility(View.VISIBLE);
                }
            } else {
                exoProgressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                exoPlayer.seekToDefaultPosition();
                exoPlayer.prepare();
            } else {
                new MaterialAlertDialogBuilder(PlayerActivity.this)
                        .setTitle(R.string.error_dialog_title)
                        .setMessage(getString(R.string.oh_something_went_wrong) + ": " + error.getMessage())
                        .setPositiveButton(R.string.repeat, (dialog, which) -> {
                            exoPlayer.prepare();
                            exoPlayer.play();
                        })
                        .setNegativeButton(R.string.close, (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
            }
        }
    }
}
