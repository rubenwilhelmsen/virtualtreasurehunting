package com.github.rubenwilhelmsen.virtualtreasurehunting;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

public class NewGameSetupActivity extends AppCompatActivity {

    private TextView distanceTextView;
    private Spinner cacheNumbersSpinner, timelimitSpinner;
    private ToggleButton standardGamemodeButton;
    private ToggleButton timetrialGamemodeButton;

    private Integer[] cacheNumbers = {1, 2, 3, 4, 5, 6};
    private Integer[] timeLimits = {30, 60, 90, 120};
    private int distanceChoosen = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game_setup);

        cacheNumbersSpinner = findViewById(R.id.cache_spinner);
        timelimitSpinner = findViewById(R.id.timelimit_spinner);
        View actionbar = findViewById(R.id.backbutton_actionbar);
        ImageButton backButton = actionbar.findViewById(R.id.backbutton);
        backButton.setOnClickListener(new BackButtonListener());
        SeekBar distanceSeekbar = findViewById(R.id.distance_seekbar);
        distanceTextView = findViewById(R.id.distance_textview);
        distanceSeekbar.setOnSeekBarChangeListener(new DistanceListener());
        standardGamemodeButton = findViewById(R.id.standard_gamemode);
        timetrialGamemodeButton = findViewById(R.id.timetrial_gamemode);
        standardGamemodeButton.setOnClickListener(new GamemodeButtonListener());
        timetrialGamemodeButton.setOnClickListener(new GamemodeButtonListener());
        Button startGameButton = findViewById(R.id.start_game);
        startGameButton.setOnClickListener(new StartGameButtonListener());

        ArrayAdapter<Integer> cacheSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, cacheNumbers);
        cacheNumbersSpinner.setAdapter(cacheSpinnerAdapter);
        ArrayAdapter<Integer> timelimitSpinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, timeLimits);
        timelimitSpinner.setAdapter(timelimitSpinnerAdapter);
        timelimitSpinner.setEnabled(false);

        if (savedInstanceState != null) {
            distanceChoosen = savedInstanceState.getInt("DISTANCE_CHOOSEN_KEY");
            distanceTextView.setText(Integer.toString(distanceChoosen) + "m");
            if (savedInstanceState.getInt("TIME_LIMIT_SPINNER_KEY") == 0) {
                timelimitSpinner.setEnabled(true);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("DISTANCE_CHOOSEN_KEY", distanceChoosen);
        outState.putInt("TIME_LIMIT_SPINNER_KEY", timelimitSpinner.isEnabled() ? 0 : 1);
        super.onSaveInstanceState(outState);
    }

    class BackButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View w) {
            onBackPressed();
        }
    }

    /**
     * Listener for both the Standard & Time trial buttons. Acts like a combobutton.
     */
    class GamemodeButtonListener implements ToggleButton.OnClickListener {
        @Override
        public void onClick(View v) {
            ToggleButton tg = (ToggleButton) v;
            if (tg.isChecked()) {
                if (tg == standardGamemodeButton) {
                    timetrialGamemodeButton.setChecked(false);
                    timelimitSpinner.setEnabled(false);
                } else {
                    standardGamemodeButton.setChecked(false);
                    timelimitSpinner.setEnabled(true);
                }
            }
            tg.setChecked(true);
        }
    }

    /**
     * Listener for Start Game button. Saves the activity results in an intent.
     */
    class StartGameButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.putExtra("NUMBER_OF_TREASURES_KEY", (Integer) cacheNumbersSpinner.getSelectedItem());
            intent.putExtra("MAX_DISTANCE_KEY", distanceChoosen);
            if (standardGamemodeButton.isChecked()) {
                intent.putExtra("GAMEMODE_KEY", "standard");
            } else {
                intent.putExtra("GAMEMODE_KEY", "timetrial");
            }

            intent.putExtra("TIMELIMIT_KEY", (Integer) timelimitSpinner.getSelectedItem());
            setResult(1, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(0);
        super.onBackPressed();
    }

    /**
     * Listener for the Seek Bar. Updates the text field according to position.
     */
    class DistanceListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                distanceChoosen = progress * 25 + 500;
                distanceTextView.setText(Integer.toString(distanceChoosen) + "m");
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) { }
    }

}
