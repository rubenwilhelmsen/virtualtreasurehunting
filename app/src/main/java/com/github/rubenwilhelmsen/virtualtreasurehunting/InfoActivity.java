package com.github.rubenwilhelmsen.virtualtreasurehunting;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import rubenwilhelmsen.github.com.virtualgeocaching.R;

public class InfoActivity extends AppCompatActivity {

    private View actionbar;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        actionbar = findViewById(R.id.backbutton_actionbar);
        backButton = actionbar.findViewById(R.id.backbutton);
        backButton.setOnClickListener(new BackButtonListener());
    }

    class BackButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View w) {
            onBackPressed();
        }
    }
}
