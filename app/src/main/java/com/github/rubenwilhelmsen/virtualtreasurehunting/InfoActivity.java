package com.github.rubenwilhelmsen.virtualtreasurehunting;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class InfoActivity extends AppCompatActivity {

    private View actionbar;
    private ImageButton backButton;
    private Button feedbackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        actionbar = findViewById(R.id.backbutton_actionbar);
        backButton = actionbar.findViewById(R.id.backbutton);
        backButton.setOnClickListener(new BackButtonListener());
        feedbackButton = findViewById(R.id.feedback_button);
        feedbackButton.setOnClickListener(new FeedbackButtonListener());

    }

    class BackButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View w) {
            onBackPressed();
        }
    }

    class FeedbackButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"virtualtreasurehuntingapp@gmail.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(InfoActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
