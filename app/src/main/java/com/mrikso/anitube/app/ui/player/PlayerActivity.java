package com.mrikso.anitube.app.ui.player;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.util.Pair;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionParameters;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.Strings;
import com.mrikso.anitube.app.R;
import com.mrikso.anitube.app.databinding.ActivityPlayerBinding;
import com.mrikso.anitube.app.model.LoadState;
import com.mrikso.anitube.app.model.VideoLinksModel;
import com.mrikso.anitube.app.parser.video.model.EpisodeModel;
import com.mrikso.anitube.app.ui.dialogs.UnsupportedVideoSourceDialog;
import com.mrikso.anitube.app.utils.DialogUtils;
import com.mrikso.anitube.app.view.CustomAutoCompleteTextView;
import com.mrikso.anitube.app.viewmodel.SharedViewModel;

import dagger.hilt.android.AndroidEntryPoint;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

@AndroidEntryPoint
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

    private final long INCREMENT_MILLIS = 5000L;
    private TrackSelectionParameters trackSelectionParameters;
    private ExoMediaSourceHelper mediaSourceHelper;

    // player status
    private boolean isLock = false;
    private boolean isBuffering = false;
    private boolean isFullScreen = false;
    private boolean pipStatus = false;
    private int currentSpeedIndex = 1;
    // episodes variables
    // private String link = "";
    //  private int baseEpisodeNumber = 1;
    private String animeTitle = EMPTY_STRING_VALUE;
    private String animeKind = EMPTY_STRING_VALUE;
    private VideoLinksModel episodeLinks;
    private Map<Integer, EpisodeModel> episodes =
            new HashMap<>(); // episode number - epide model link
    private int episodeNumber = EMPTY_EPISODE_NUMBER_VALUE;
    private String currentQuality;
    private long currentPosition;
    private AlertDialog progressDialog;
    private SharedViewModel sharedViewModel;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setView();
        initViewModel();
        setupViews();
        if (savedInstanceState == null) {
            hideNavBar();
            parseExtra();
            preparePlayer();
            setClickListeners();
            setupTextViewValues();
            setupQuality();
            setupMiddleControllers();
        }
    }

    private void setView() {
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
    }

    private void initViewModel() {
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);
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
    }

    private void preparePlayer() {
        mediaSourceHelper = ExoMediaSourceHelper.getInstance(this);
        exoPlayer =
                new ExoPlayer.Builder(this)
                        .setSeekBackIncrementMs(INCREMENT_MILLIS)
                        .setSeekForwardIncrementMs(INCREMENT_MILLIS)
                        .build();
        //  exoPlayer.setTrackSelectionParameters(trackSelectionParameters);
        exoPlayer.addListener(new PlayerEventListener());
        // exoPlayer.addAnalyticsListener(new EventLogger());
        exoPlayer.setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus= */ true);
        // exoPlayer.setPlayWhenReady(true);
        binding.player.setControllerVisibilityListener(
                visibility -> {
                    if (visibility != View.VISIBLE) {
                        exoQuality.dismissDropDown();
                    } else {

                        // controller is not visible
                    }
                });
        binding.player.setPlayer(exoPlayer);

        setMediaSourceByModel(episodeLinks);

        // exoPlayer.seekTo(playbackPosition);
        // exoPlayer.setPlayWhenReady(playWhenReady);
        exoPlayer.prepare();

        playVideo();
    }

    private void setupTextViewValues() {
        //  if& (animeKind.equals("Сериал")) {
        exoEpisodeNumber.setText(animeKind);
        // getString(R.string.episode_number, episodeNumber));
        exoAnimeTitle.setText(animeTitle);
        //  } else {
        //  exoEpisodeNumber.setText(animeTitle);
        //  exoAnimeTitle.setVisibility(View.GONE);
        // }
    }

    private void setupMiddleControllers() {
        Log.i("TAG", "epnum:" + episodeNumber + "size:" + episodes.size());

        if (episodeNumber == 1) {
            exoPrevEp.setAlpha(0.7f);
            exoPrevEp.setEnabled(false);
            if (episodes.size() == 1) {
                exoNextEp.setAlpha(0.7f);
                exoNextEp.setEnabled(false);
            } else {
                exoNextEp.setAlpha(1f);
                exoNextEp.setEnabled(true);
            }
        } else if (episodeNumber == episodes.size()) {
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
            binding.player.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            exoScreen.setImageResource(R.drawable.ic_close_fullscreen);
        } else {
            binding.player.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT);
            exoScreen.setImageResource(R.drawable.ic_open_in_fullscreen);
        }
    }

    private void showSpeedDialog() {
        String[] speeds = {"0.5x", "Звичайна", "1.25x", "1.5x", "1.75x", "2.0x", "2.5x", "3.0x"};
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(R.string.set_video_speed);
        builder.setSingleChoiceItems(
                speeds,
                currentSpeedIndex,
                (dialog, which) -> {
                    currentSpeedIndex = which;
                    PlaybackParameters param =
                            new PlaybackParameters(getSpeedFromIndex(currentSpeedIndex));
                    exoPlayer.setPlaybackParameters(param);
                    dialog.dismiss();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setupQuality() {
        if (episodeLinks.getLinksQuality() != null && !episodeLinks.getLinksQuality().isEmpty()) {
            exoQuality.setOnClickListener(
                    v -> {
                        exoQuality.showDropDown();
                    });

            updateQualityArray();
            exoQuality.setOnItemClickListener(
                    (parent, view, position, id) -> {
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
                        MediaSource mediaSource =
                                mediaSourceHelper.getMediaSource(qualityItem, true);
                        exoPlayer.setMediaSource(mediaSource);

                        TrackSelectionParameters currentParameters =
                                exoPlayer.getTrackSelectionParameters();
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
        List<String> qualityList = new ArrayList<>(episodeLinks.getLinksQuality().keySet());
        ArrayAdapter<String> adapterQuality =
                new ArrayAdapter<>(this, R.layout.item_quality_speed, qualityList);
        exoQuality.setText(currentQuality);
        Log.i("tag", "updateQualityArray");
        exoQuality.setAdapter(adapterQuality);
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
        backButton.setOnClickListener(
                v -> {
                    exoPlayer.release();
                    finish();
                });

        exoLock.setOnClickListener(
                v -> {
                    isLock = !isLock;
                    lockScreen(isLock);
                });
        exoPrevEp.setOnClickListener(
                v -> {
                    if (episodeNumber != 1) {
                        episodeNumber -= 1;
                        if (episodeNumber != 1) {
                            exoPrevEp.setAlpha(1f);
                        } else {
                            exoPrevEp.setAlpha(0.7f);
                        }
                        exoNextEp.setAlpha(1f);
                        exoNextEp.setEnabled(true);
                        loadEpisode(episodes.get(episodeNumber - 1));
                    } else {
                        exoPrevEp.setAlpha(0.7f);
                    }
                });

        exoNextEp.setOnClickListener(
                v -> {
                    if (episodeNumber != episodes.size()) {
                        episodeNumber += 1;
                        if (episodeNumber != episodes.size()) {
                            exoNextEp.setAlpha(1f);
                        } else {
                            exoNextEp.setAlpha(0.7f);
                        }
                        exoPrevEp.setAlpha(1f);
                        exoPrevEp.setEnabled(true);
                        loadEpisode(episodes.get(episodeNumber - 1));
                    } else {
                        exoNextEp.setAlpha(0.7f);
                    }
                });

        exoPlay.setOnClickListener(
                v -> {
                    if (exoPlayer.isPlaying()) {
                        pauseVideo();
                    } else {
                        playVideo();
                    }
                });

        exoScreen.setOnClickListener(
                v -> {
                    isFullScreen = !isFullScreen;
                    playInFullscreen(isFullScreen);
                });
        exoSpeed.setOnClickListener(
                v -> {
                    showSpeedDialog();
                });
        exoPip.setOnClickListener(
                v -> {
                    // permission check
                    AppOpsManager appOps =
                            (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                    boolean status = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        status =
                                appOps.checkOpNoThrow(
                                                AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                                                android.os.Process.myUid(),
                                                getPackageName())
                                        == AppOpsManager.MODE_ALLOWED;
                    }
                    // API >= 26 check
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (status) {
                            enterPictureInPictureMode(new PictureInPictureParams.Builder().build());
                            binding.player.setUseController(false);
                            pipStatus = false;
                        } else {
                            Intent intent =
                                    new Intent(
                                            "android.settings.PICTURE_IN_PICTURE_SETTINGS",
                                            Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(
                                        this,
                                        "Функция не поддерживается на этом устройстве",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void loadEpisode(final EpisodeModel episode) {

        Disposable disposable =
                sharedViewModel
                        .loadData(episode.getEpisodeUrl())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(
                                v -> {
                                    progressDialog =
                                            DialogUtils.getProDialog(
                                                    this, R.string.loading_episode);
                                })
                        .subscribeWith(
                                new DisposableSingleObserver<Pair<LoadState, VideoLinksModel>>() {
                                    @Override
                                    public void onSuccess(Pair<LoadState, VideoLinksModel> result) {
                                        DialogUtils.cancelDialog(progressDialog);
                                        if (result.first == LoadState.DONE) {
                                            episodeLinks = result.second;
                                            exoEpisodeNumber.setText(episode.getName());
                                            setMediaSourceByModel(result.second);
                                        } else {
                                            UnsupportedVideoSourceDialog.show(
                                                    PlayerActivity.this,
                                                    result.second.getIfRameUrl());
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

    void bypassSSL() {
        try {
            // Disables ssl check
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                    null,
                    new SSLTrustManager[] {new SSLTrustManager()},
                    new java.security.SecureRandom());
            sslContext.createSSLEngine();
            HttpsURLConnection.setDefaultHostnameVerifier(
                    new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        } catch (Exception ex) {

        }
    }

    private void setMediaSourceByModel(VideoLinksModel model) {
        if (model.isIgnoreSSL()) {
            bypassSSL();
        }

        MediaSource mediaSource = null;
        if (model.getLinksQuality() != null && !model.getLinksQuality().isEmpty()) {
            Map<String, String> qualitiesMap = model.getLinksQuality();
            updateQualityArray();
            currentQuality = model.getDefaultQuality();

            if (Strings.isNullOrEmpty(currentQuality)) {
                currentQuality = qualitiesMap.keySet().stream().findFirst().get();
            }

            qualitiesMap.entrySet().stream()
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
            String playUrl = qualitiesMap.get(currentQuality);

            Log.i("PlayerActivity", "playUrl: " + playUrl);

            mediaSource = mediaSourceHelper.getMediaSource(playUrl, model.getHeaders(), true);
            exoPlayer.setMediaSource(mediaSource);

        } else if (!Strings.isNullOrEmpty(model.getSingleDirectUrl())) {
            mediaSource =
                    mediaSourceHelper.getMediaSource(
                            model.getSingleDirectUrl(), model.getHeaders(), true);
            exoPlayer.setMediaSource(mediaSource);

        } else {
            Toast.makeText(this, "Виникла помилка, посилання на файли пусті", Toast.LENGTH_LONG)
                    .show();
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideNavBar();
        if (binding != null && exoPlayer != null) {
            binding.player.setUseController(true);
            exoPlayer.prepare();
        }
    }

    private void hideNavBar() {
        if (binding != null) {
            WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
            WindowInsetsControllerCompat controller =
                    new WindowInsetsControllerCompat(getWindow(), binding.getRoot());
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
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
            episodes = args.getEpisodesTotal();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
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
        releasePlayer();
    }

    private class PlayerEventListener implements Player.Listener {

        @Override
        public void onPlaybackStateChanged(int playbackState) {
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
                Toast.makeText(
                                PlayerActivity.this,
                                "error: " + error.getMessage(),
                                Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        }
    }
}
