package com.mrikso.anitube.app.ui.player;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.ActivityPlayerBinding;
import com.mrikso.anitube.app.model.BaseAnimeModel;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.ui.dialogs.UnsupportedVideoSourceDialog;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.view.CustomAutoCompleteTextView;
import com.mrikso.anitube.app.viewmodel.ListRepository;
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

    private static final String EMPTY_STRING_VALUE = "~";
    private static final int EMPTY_EPISODE_NUMBER_VALUE = 0;

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
    private CardView cvPlay;
    private ImageView exoPlay;
    private ProgressBar exoProgressBar;
    private ImageView exoNextEp;

    // Bottom buttons
    private ImageView exoPip;
    private ImageView exoSkip;
    private ImageView exoScreen;

    private LinearLayout exoTopControllers;
    private LinearLayout exoMiddleControllers;
    private LinearLayout exoBottomControllers;

    private TrackSelectionParameters trackSelectionParameters;
    private ExoMediaSourceHelper mediaSourceHelper;
    public CustomPlayerView playerView;
    private YouTubeOverlay youTubeOverlay;
    private Object mPictureInPictureParamsBuilder;
    final Rational rationalLimitWide = new Rational(239, 100);
    final Rational rationalLimitTall = new Rational(100, 239);

    // player status
    private boolean isLock = false;
    private boolean isBuffering = false;
    private boolean isFullScreen = false;

    public static boolean controllerVisible;
    public static boolean controllerVisibleFully;
    private boolean restoreControllerTimeout = false;
    private boolean shortControllerTimeout = false;
    private long lastScrubbingPosition;
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

    private BaseAnimeModel animeModel;
    private String episodePath;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView();
        initViewModel();
        setupViews();
       // if (savedInstanceState == null) {
            hideNavBar();
            parseExtra();
            preparePlayer();
            setClickListeners();
            setupTextViewValues();
            setupQuality();
            setupMiddleControllers();
     //   }
    }

    private void setView() {
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        playerView = binding.videoView;
    }

    private void initViewModel() {
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
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
        cvPlay = findViewById(R.id.cv_exo_play_pause);
        exoPlay = findViewById(R.id.ib_exo_play_pause);
        exoProgressBar = findViewById(R.id.exo_progress_bar);
        exoNextEp = findViewById(R.id.exo_next_ep);

        // Bottom buttons
        exoPip = findViewById(R.id.exo_pip);
        //  exoSkip = findViewById(R.id.exo_skip);
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

        if (Utils.isPiPSupported(this)) {
            // TODO: Android 12 improvements:
            // https://developer.android.com/about/versions/12/features/pip-improvements
            mPictureInPictureParamsBuilder = new PictureInPictureParams.Builder();
            //  boolean success = updatePictureInPictureActions(R.drawable.ic_play_arrow_24dp,
            // R.string.exo_controls_play_description, CONTROL_TYPE_PLAY, REQUEST_PLAY);
        }
    }

    private void preparePlayer() {
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
        /// playerView.setControllerShowTimeoutMs(-1);

        ((DoubleTapPlayerView) playerView).setDoubleTapEnabled(true);

        playerView.setLocked(isLock);
        var renderersFactory = new NextRenderersFactory(this)
                .setEnableDecoderFallback(true)
                .setExtensionRendererMode(NextRenderersFactory.EXTENSION_RENDERER_MODE_PREFER);
        // new DefaultRenderersFactory(this).setEnableDecoderFallback(true);

        long INCREMENT_MILLIS = 5000L;
        exoPlayer = new ExoPlayer.Builder(this, renderersFactory)
                .setSeekBackIncrementMs(INCREMENT_MILLIS)
                .setSeekForwardIncrementMs(INCREMENT_MILLIS)
                .build();
        //  exoPlayer.setTrackSelectionParameters(trackSelectionParameters);
        exoPlayer.addListener(new PlayerEventListener());
        // exoPlayer.addAnalyticsListener(new EventLogger());
        exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true);
        // exoPlayer.setPlayWhenReady(true);
        exoPlayer.setHandleAudioBecomingNoisy(true);

        playerView.setControllerVisibilityListener((PlayerView.ControllerVisibilityListener) visibility -> {
            controllerVisible = visibility == View.VISIBLE;
            controllerVisibleFully = playerView.isControllerFullyVisible();
            if (restoreControllerTimeout) {
                restoreControllerTimeout = false;
            }

            if (visibility != View.VISIBLE) {
                exoQuality.dismissDropDown();
            } else {

                // controller is not visible
            }
        });
        youTubeOverlay.player(exoPlayer);
        playerView.setPlayer(exoPlayer);
        currentPosition = listRepo.getList().get(episodeNumber - 1).getTotalWatchTime();
        Log.i("player", "currentPosition: " + currentPosition);
        setMediaSourceByModel(episodeLinks);

        // exoPlayer.seekTo(playbackPosition);
        // exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.prepare();

        playVideo();
    }

    private void setupTextViewValues() {
        exoEpisodeNumber.setText(animeKind);
        exoAnimeTitle.setText(animeTitle);
    }

    private void setupMiddleControllers() {
        Log.i("TAG", "epnum:" + episodeNumber + "size:" + listRepo.getList().size());

        if (episodeNumber == 1) {
            exoPrevEp.setAlpha(0.7f);
            exoPrevEp.setEnabled(false);
            if (listRepo.getList().size() == 1) {
                exoNextEp.setAlpha(0.7f);
                exoNextEp.setEnabled(false);
            } else {
                exoNextEp.setAlpha(1f);
                exoNextEp.setEnabled(true);
            }
        } else if (episodeNumber == listRepo.getList().size()) {
            exoNextEp.setAlpha(0.7f);
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

        playerView.setLocked(isLock);
    }

    private void playVideo() {
        exoPlay.setImageResource(R.drawable.ic_pause);
        exoPlayer.play();
    }

    private void pauseVideo() {
        exoPlay.setImageResource(R.drawable.ic_play_arrow);
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
                currentPosition = exoPlayer.getCurrentPosition();
                /*
                        Log.i("tag", currentQuality + ": " + qualityItem);
                        episodeLinks.getLinksQuality().entrySet().stream()
                                .forEach(
                                        e ->
                                                Log.i(
                                                        "tag",
                                                        "currentQuality: "
                                                                + currentQuality
                                                                + " "
                                                                + e.getKey()
                                                                + " "
                                                                + e.getValue()));
                */
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
            currentPosition = exoPlayer.getCurrentPosition();
            exoPlayer.stop();
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    private void updateQualityArray() {
        List<String> qualityList =
                new ArrayList<>(episodeLinks.getLinksQuality().keySet());
        ArrayAdapter<String> adapterQuality = new ArrayAdapter<>(this, R.layout.item_quality_speed, qualityList);
        exoQuality.setText(currentQuality);
        Log.i("tag", "updateQualityArray");
        exoQuality.setAdapter(adapterQuality);
    }

    private void savePlayer() {
        if (exoPlayer != null) {
            currentPosition = exoPlayer.getCurrentPosition();
            sharedViewModel.addOrUpdateWatchedAnime(
                    animeModel, episodePath, episodeNumber, exoPlayer.getContentDuration(), currentPosition);
            sharedViewModel.addOrUpdateWatchedEpisode(
                    episodeNumber - 1, true, exoPlayer.getContentDuration(), currentPosition, animeModel);
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
        exoPrevEp.setOnClickListener(v -> {
            if (episodeNumber != 1) {
                episodeNumber -= 1;
                if (episodeNumber != 1) {
                    exoPrevEp.setAlpha(1f);
                } else {
                    exoPrevEp.setAlpha(0.7f);
                }
                exoNextEp.setAlpha(1f);
                exoNextEp.setEnabled(true);
                savePlayer();
                loadEpisode(listRepo.getList().get(episodeNumber - 1));
            } else {
                exoPrevEp.setAlpha(0.7f);
            }
        });

        exoNextEp.setOnClickListener(v -> {
            if (episodeNumber != listRepo.getList().size()) {
                episodeNumber += 1;
                if (episodeNumber != listRepo.getList().size()) {
                    exoNextEp.setAlpha(1f);
                } else {
                    exoNextEp.setAlpha(0.7f);
                }
                exoPrevEp.setAlpha(1f);
                exoPrevEp.setEnabled(true);
                savePlayer();
                loadEpisode(listRepo.getList().get(episodeNumber - 1));
            } else {
                exoNextEp.setAlpha(0.7f);
            }
        });

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
    }

    private void enterPiP() {
        final AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (AppOpsManager.MODE_ALLOWED
                != appOpsManager.checkOpNoThrow(
                        AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(), getPackageName())) {
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

        playerView.setControllerAutoShow(false);
        playerView.hideController();

        final Format format = exoPlayer.getVideoFormat();
        playerView.setUseController(false);
        if (format != null) {
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
                ((PictureInPictureParams.Builder) mPictureInPictureParamsBuilder).setExpandedAspectRatio(rational);
            }
            if (rational.floatValue() > rationalLimitWide.floatValue()) rational = rationalLimitWide;
            else if (rational.floatValue() < rationalLimitTall.floatValue()) rational = rationalLimitTall;

            ((PictureInPictureParams.Builder) mPictureInPictureParamsBuilder).setAspectRatio(rational);
        }
        enterPictureInPictureMode(((PictureInPictureParams.Builder) mPictureInPictureParamsBuilder).build());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private boolean isInPip() {
        if (!Utils.isPiPSupported(this)) return false;
        return isInPictureInPictureMode();
    }

    private void loadEpisode(final EpisodeModel episode) {
        currentPosition = episode.getTotalWatchTime();

        Disposable disposable = sharedViewModel
                .loadData(episode.getEpisodeUrl())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(v -> {
                    progressDialog = DialogUtils.getProDialog(this, R.string.loading_episode);
                })
                .subscribeWith(new DisposableSingleObserver<Pair<LoadState, VideoLinksModel>>() {
                    @Override
                    public void onSuccess(Pair<LoadState, VideoLinksModel> result) {
                        DialogUtils.cancelDialog(progressDialog);
                        if (result.first == LoadState.DONE) {
                            episodeLinks = result.second;
                            exoEpisodeNumber.setText(episode.getName());
                            setMediaSourceByModel(result.second);
                        } else {
                            UnsupportedVideoSourceDialog.show(PlayerActivity.this, result.second.getIfRameUrl());
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        DialogUtils.cancelDialog(progressDialog);
                        throwable.printStackTrace();
                    }
                });
        disposables.add(disposable);
    }


     private void setMediaSourceByModel(VideoLinksModel model) {
        if (model.isIgnoreSSL()) {
            Utils.bypassSSL();
        }

        MediaSource mediaSource = null;
        if (model.getLinksQuality() != null && !model.getLinksQuality().isEmpty()) {
            Map<String, String> qualitiesMap = model.getLinksQuality();

            currentQuality = model.getDefaultQuality();

            if (Strings.isNullOrEmpty(currentQuality)) {
                currentQuality = qualitiesMap.keySet().stream().findFirst().get();
            }

            qualitiesMap.forEach((key, value) -> Log.i("tag", "currentQuality: " + currentQuality + " " + key + " " + value));
            String playUrl = qualitiesMap.get(currentQuality);

            Log.i("PlayerActivity", "playUrl: " + playUrl);
            updateQualityArray();
            mediaSource = mediaSourceHelper.getMediaSource(playUrl, model.getHeaders(), true);
            exoPlayer.setMediaSource(mediaSource);
            playerView.setHaveMedia(true);
            exoPlayer.seekTo(currentPosition);
        } else if (!Strings.isNullOrEmpty(model.getSingleDirectUrl())) {
            mediaSource = mediaSourceHelper.getMediaSource(model.getSingleDirectUrl(), model.getHeaders(), true);
            exoPlayer.setMediaSource(mediaSource);
            playerView.setHaveMedia(true);
            exoPlayer.seekTo(currentPosition);
        } else {
            Toast.makeText(this, "Виникла помилка, посилання на файли пусті", Toast.LENGTH_LONG)
                    .show();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isLock) {
            return;
        } else {
            // super.onBackPressed();
            finish();
            // Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasePlayer();
        binding = null;
        disposables.dispose();
    }

    @Override
    public void onPause() {
        super.onPause();
        savePlayer();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideNavBar();
        if (binding != null && exoPlayer != null) {
            playerView.setUseController(true);
            exoPlayer.prepare();
        }
    }

    private class PlayerEventListener implements Player.Listener {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            playerView.setKeepScreenOn(isPlaying);

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
                    isBuffering = true;
                    exoProgressBar.setVisibility(View.VISIBLE);
                    cvPlay.setVisibility(View.GONE);
                    break;
                case Player.STATE_READY:
                    isBuffering = false;
                    exoProgressBar.setVisibility(View.GONE);
                    cvPlay.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_IDLE:
                    isBuffering = false;
                    exoProgressBar.setVisibility(View.VISIBLE);
                    cvPlay.setVisibility(View.GONE);
                    break;
                case Player.STATE_ENDED:
                    finish();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onIsLoadingChanged(boolean isLoading) {
            if (isLoading) {
                isBuffering = true;
                if (exoPlayer.isCurrentMediaItemSeekable()
                        && !exoPlayer.isPlaying()
                        && exoPlayer.getBufferedPosition() == 0L) {
                    exoProgressBar.setVisibility(View.VISIBLE);
                }
            } else {
                isBuffering = false;
                exoProgressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPlayerError(PlaybackException error) {
            if (error.errorCode == PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW) {
                exoPlayer.seekToDefaultPosition();
                exoPlayer.prepare();
            } else {
                Toast.makeText(PlayerActivity.this, "error: " + error.getMessage(), Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
    }
}
