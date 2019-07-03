package com.github.rubenwilhelmsen.virtualtreasurehunting;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class CountdownService extends Service {

    private CountDownTimer timer;
    private final String CHANNEL_ID = "TIMER_CHANNEL";
    private final int NOTIFICATION_ID = 3;
    private Intent serviceOutput = new Intent("COUNTDOWN_INTENT");
    NotificationManager notificationManager;
    NotificationCompat.Builder builder;


    @Override
    public void onCreate() {
        super.onCreate();

        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Timer")
                .setContentText("Time Left");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "VirtualTreasureHunting", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(notificationChannel);
        } else {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            builder.setPriority(NotificationCompat.PRIORITY_LOW).setVibrate(null);
        }

        Notification notification = builder.setOngoing(true).build();
        startForeground(NOTIFICATION_ID, notification);

    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    private void updateNotification(long timeLeft) {
        int minutesLeft = (int)timeLeft / 1000 / 60;
        builder.setContentText(minutesLeft + " minutes left.");
        notificationManager.notify(NOTIFICATION_ID, builder.setOngoing(true).build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (timer == null) {
            startTimer(intent.getIntExtra("TIME_KEY", -1));
        }
        return super.onStartCommand(intent,flags,startId);
    }

    /**
     * Starts timer. Saves time left and sends it to the Broadcast reciever. If timer runs out it stops the service and removes the notification.
     * @param timeleft
     */
    public void startTimer(final int timeleft) {
        timer = new CountDownTimer(timeleft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateNotification(millisUntilFinished);
                serviceOutput.putExtra("TIMELEFT_KEY", millisUntilFinished);
                sendBroadcast(serviceOutput);
            }

            @Override
            public void onFinish() {
                stopForeground(true);
                stopSelf();
            }
        };
        timer.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
