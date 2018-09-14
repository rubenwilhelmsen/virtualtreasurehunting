package com.github.rubenwilhelmsen.virtualtreasurehunting;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import rubenwilhelmsen.github.com.virtualgeocaching.R;

public class OpenTreasureMinigameActivity extends AppCompatActivity implements SensorEventListener{

    private View tempView;
    private ImageView tapActionView, darkActionView, shakeActionView, jumpActionView;
    private ProgressBar darkProgress, shakeProgress, tapProgress, jumpProgress;
    private TextView timerText, actionsCompletedText;
    private CountDownTimer timer;

    private SensorManager sensorManager;
    private MinigameLogic minigameLogic;
    private int currentAction;
    private int taps = 0;
    private int animationDuration;
    private float lastAcceleration;
    private float currentAcceleration;
    private float acceleration;
    private boolean jumped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_treasure_minigame);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (sensor == null) {
            Toast.makeText(this,"No light sensor found", Toast.LENGTH_SHORT);
            finishMinigame();
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor == null) {
            Toast.makeText(this,"No accelerometer found", Toast.LENGTH_SHORT);
            finishMinigame();
        }

        FrameLayout frameLayout = findViewById(R.id.layout);
        frameLayout.setOnClickListener(new TapListener());

        tapActionView = findViewById(R.id.tap_action_view);
        shakeActionView = findViewById(R.id.shake_action_view);
        darkActionView = findViewById(R.id.dark_action_view);
        jumpActionView = findViewById(R.id.jump_action_view);

        darkProgress = findViewById(R.id.dark_progress);
        shakeProgress = findViewById(R.id.shake_progress);
        tapProgress = findViewById(R.id.tap_progress);
        jumpProgress = findViewById(R.id.jump_progress);

        minigameLogic = new MinigameLogic();

        timer = null;
        timerText = findViewById(R.id.countdown_timer);
        animationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

        actionsCompletedText = findViewById(R.id.actions_completed);

        acceleration = 0.00f;
        setupAction();
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onBackPressed() {
        setResult(0);
        super.onBackPressed();
    }

    /**
     * Handles the Tap card. Listens for taps and calls {@code completeAction()} if the user has tapped 10 times. Also updates the progress bar.
     */
    class TapListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (currentAction == 3) {
                taps++;
                tapProgress.setProgress(taps);
                if (taps == 10) {
                    completeAction();
                }
            }
        }
    }

    /**
     * Handles the Jump card. Listens for physical jumps. Also updates the progress bar.
     * @param event accelerometer event
     * @return true if a jump has been detected
     */
    private boolean detectJump(SensorEvent event) {
        float z = event.values[2];
        if (!jumped) {
            if (z > 30) {
                jumpProgress.setProgress(50);
                jumped = true;
            }
        } else {
            if (!detectShake(event, 1)) {
                if (z < 0.1 && z > -0.1) {
                    jumpProgress.setProgress(100);
                    jumped = false;
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Handles the Make it dark card. Also updates the progress bar.
     * @param event light sensor event
     * @return true if the light sensor values are below 3
     */
    private boolean detectDark(SensorEvent event) {
        float light = event.values[0];
        darkProgress.setProgress(Math.round(3000 / light));
        return light < 3;
    }

    /**
     * Handles the Shake card. Increments the progress bar based on amount of shake.
     * @param event accelerometer event
     * @param threshold how hard the user has to shake
     * @return true if the progress bar is completed
     */
    private boolean detectShake(SensorEvent event, int threshold) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        lastAcceleration = currentAcceleration;
        currentAcceleration = (float) Math.sqrt((double) (x*x + y*y + z*z));
        float delta = currentAcceleration - lastAcceleration;
        acceleration = acceleration * 0.9f + delta;

        if (acceleration > threshold) {
            shakeProgress.incrementProgressBy(Math.round(acceleration));
            return true;
        } else {
            return false;
        }
    }


    /**
     * Handles the crossfade animation between two cards.
     * @param viewToCrossfadeTo view that is being switched to
     */
    private void crossfade(final View viewToCrossfadeTo) {
        viewToCrossfadeTo.setAlpha(0f);
        viewToCrossfadeTo.setVisibility(View.VISIBLE);

        viewToCrossfadeTo.animate().alpha(1f).setDuration(animationDuration).setListener(null);

        tempView.animate().alpha(0f).setDuration(animationDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tempView.setVisibility(View.GONE);
                tempView = viewToCrossfadeTo;
            }
        });
    }

    /**
     * Sets {@code viewToShow} to visible if no other card is shown on screen, otherwise calls {@code crossfade(viewToShow)}.
     * @param viewToShow the card to show or crossfade to
     */
    private void actionViewSetup(View viewToShow) {
        if (tempView != null) {
            crossfade(viewToShow);
            tempView.setVisibility(View.VISIBLE);
        } else {
            tempView = viewToShow;
            viewToShow.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handles whether or not the minigame is completed or not. If it is not completed a new card is shown.
     */
    private void completeAction() {
        actionsCompletedText.setText(minigameLogic.actionCompleted()+"/3");
        if (minigameLogic.minigameCompleted()) {
            finishMinigame();
        } else {
            setupAction();
        }
    }

    /**
     * Set results to show that the minigame has been completed. Then unregisters sensor listeners and calls {@code finish()}.
     */
    private void finishMinigame() {
        Intent intent = new Intent();
        intent.putExtra("MARKER_KEY_BACK", getIntent().getParcelableExtra("MARKER_KEY"));
        setResult(1, intent);
        sensorManager.unregisterListener(this);
        finish();
    }

    /**
     * Handles completion of cards. If a card has been completed {@code completeAction()} is called.
     * @param event sensor event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(currentAction) {
            case 0:
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    if (detectShake(event, 10)) {
                        if (shakeProgress.getProgress() == 300) {
                            completeAction();
                        }
                    }
                }
                break;
            case 1:
                if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    if (detectDark(event)) {
                        completeAction();
                    }
                }
                break;
            case 2:
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    if (detectJump(event)) {
                        completeAction();
                    }
                    jumpProgress.setProgress(0);
                    shakeProgress.setProgress(0);
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    /**
     * Handles the cards, their timers and progress bars. Updates the view according to a random integer.
     */
    private void setupAction() {
        resetProgress();
        cancelTimer();
        shakeProgress.setVisibility(View.GONE);
        int action = minigameLogic.getRandomAction();
        switch(action) {
            case 0:
                actionViewSetup(shakeActionView);
                shakeProgress.setVisibility(View.VISIBLE);
                break;
            case 1:
                actionViewSetup(darkActionView);
                darkProgress.setVisibility(View.VISIBLE);
                break;
            case 2:
                actionViewSetup(jumpActionView);
                jumpProgress.setVisibility(View.VISIBLE);
                break;
            case 3:
                actionViewSetup(tapActionView);
                tapProgress.setVisibility(View.VISIBLE);
                break;
        }
        currentAction = action;
        startTimer();
    }

    /**
     * Resets all progress bars and hides them.
     */
    public void resetProgress() {
        shakeProgress.setVisibility(View.GONE);
        shakeProgress.setProgress(0);
        darkProgress.setVisibility(View.GONE);
        darkProgress.setProgress(0);
        jumpProgress.setVisibility(View.GONE);
        jumpProgress.setProgress(0);
        tapProgress.setVisibility(View.GONE);
        tapProgress.setProgress(0);
        taps = 0;
    }

    /**
     * Starts the timer for a card.
     */
    private void startTimer() {
        timer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText(""+millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                setupAction();
            }
        };
        timer.start();
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }


}
