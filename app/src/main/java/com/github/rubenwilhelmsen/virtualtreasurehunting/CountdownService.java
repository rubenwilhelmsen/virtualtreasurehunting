package com.github.rubenwilhelmsen.virtualtreasurehunting;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class CountdownService extends Service {

    private CountDownTimer timer;
    private String CHANNEL_ID = "TIMER_CHANNEL";
    private Intent serviceOutput = new Intent("COUNTDOWN_INTENT");

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "name", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("description");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }
        Notification notification = builder.setOngoing(true).build();
        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
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
    public void startTimer(int timeleft) {
        timer = new CountDownTimer(timeleft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
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
