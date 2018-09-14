package com.github.rubenwilhelmsen.virtualtreasurehunting;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import rubenwilhelmsen.github.com.virtualgeocaching.R;

public class MainMenuActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Button tracklocation, newGame;
    private TextView timerText = null;

    private Game currentGame;
    private int LOCATION_PERMISSION = 1;
    private boolean requestingFollow = false;
    private boolean finishedMinigame;
    private boolean userRemoveTreasure = false;
    private LatLng lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate", "called, MainMenuActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
        timerText = findViewById(R.id.timer);
        timerText.setVisibility(View.GONE);
        currentGame = loadGame();

        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(5);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location temp = locationResult.getLastLocation();
                if (requestingFollow) {
                    map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(temp.getLatitude(), temp.getLongitude())));
                }
                lastLocation = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
            }
        };
        startLocationUpdates();
        zoomToLastLocation(17);

        finishedMinigame = false;

        ImageButton assistButton = findViewById(R.id.assistbutton);
        assistButton.setOnClickListener(new AssistButtonListener());
        ImageButton infoButton = findViewById(R.id.infobutton);
        infoButton.setOnClickListener(new InfoButtonListener());

        tracklocation = findViewById(R.id.tracklocation);
        tracklocation.setOnClickListener(new FollowButtonListener());
        newGame = findViewById(R.id.newgame);
        newGame.setOnClickListener(new NewGameListener());


        if (ContextCompat.checkSelfPermission(MainMenuActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            tracklocation.setEnabled(true);
            newGame.setEnabled(true);
        } else {
            tracklocation.setEnabled(false);
            newGame.setEnabled(false);
        }

        if (savedInstanceState != null) {
            requestingFollow = savedInstanceState.getInt("LOCATION_UPDATES_KEY") != 0;
            lastLocation = savedInstanceState.getParcelable("LAST_LOCATION_KEY");
        }
        handleFollowButton();
        handleNewGameButton();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("LOCATION_UPDATES_KEY", requestingFollow ? 1 : 0);
        outState.putParcelable("LAST_LOCATION_KEY", lastLocation);
        super.onSaveInstanceState(outState);
    }

    /**
     * Handles results from NewGameSetupActivity (starts game based on {@code data}) and OpenTreasureMinigameActivity (registers treasure has been opened in currentGame).
     * @param requestCode which activity the results came from: 1 = NewGameSetupActivity & 2 = OpenTreasureMinigameActivity
     * @param resultCode 0 if user pressed back button, 1 if not.
     * @param data result from activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == 1) {
                int numberOfTreasures = data.getIntExtra("NUMBER_OF_TREASURES_KEY", -1);
                int maxDistance = data.getIntExtra("MAX_DISTANCE_KEY", -1);
                String gamemode = data.getStringExtra("GAMEMODE_KEY");
                GameSetup gs = new GameSetup(numberOfTreasures, maxDistance, lastLocation);
                if (gs.getTreasures()!= null) {
                    currentGame = new Game(gs.getTreasures(), maxDistance, gamemode);
                    if (gamemode.equals("timetrial")) {
                        startService(new Intent(this, CountdownService.class).putExtra("TIME_KEY", data.getIntExtra("TIMELIMIT_KEY", -1) * 60 * 1000));
                        timerText.setVisibility(View.VISIBLE);
                    } else {
                        timerText.setVisibility(View.GONE);
                    }
                    handleNewGameButton();
                } else {
                    Toast.makeText(this, "Game setup failed, position unavailable. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        }
        if (requestCode == 2) {
            if (resultCode == 1) {
                if (currentGame != null) {
                    finishedMinigame = true;
                    currentGame.openTreasure((LatLng) data.getParcelableExtra("MARKER_KEY_BACK"));
                    if (currentGame.gameFinished()) {
                        finishGame(false, false);
                    }
                }
            }
        }
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(MainMenuActivity.this);
    }

    /**
     * Creates the Broadcast Reciever and assigns how it should handle the recieved data (which is updating the timer for each call and finishing the game if it runs out).
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleTimer(intent);
            if (intent.getLongExtra("TIMELEFT_KEY", -1) < 1000) {
                finishGame(true,true);
            }
        }
    };

    /**
     * Saves the game and unregisters the Broadcast Reciever listener if it is running.
     */
    @Override
    public void onDestroy() {
        saveGame();
        if (serviceRunning(CountdownService.class)) {
            unregisterReceiver(broadcastReceiver);
        }
        super.onDestroy();
    }

    /**
     * Registers a listener to the Broadcast Reciever. Also finishes the game if the timer ran out when user was not using the app.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (currentGame!=null) {
            if (currentGame.getGamemode().equals("timetrial")) {
                if (serviceRunning(CountdownService.class)) {
                    registerReceiver(broadcastReceiver, new IntentFilter("COUNTDOWN_INTENT"));
                } else {
                    finishGame(true,true);
                }
            }
        }
    }

    /**
     * Checks if a certain service is running.
     * @param serviceClass the service class to check
     * @return true if the service provided is running
     */
    private boolean serviceRunning(Class<?> serviceClass) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : am.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets map type to satellite and adds listener to potential markers. Also places markers on map if there is a game in progress. Checks if a minigame has been completed in order to if so remove the marker.
     * @param googleMap map shown to user
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        map.setOnMarkerClickListener(new MarkerListener());

        if (finishedMinigame) {
            map.clear();
            finishedMinigame = false;
        }

        if (currentGame != null) {
            placeTreasures();
        }

        enableLocation();
        zoomToLastLocation(15);
    }

    /**
     * Places the {@code currentGame}'s {@code Treasure}'s on map.
     */
    private void placeTreasures() {
        for (int i = 0; i < currentGame.getTreasures().length; i++) {
            if (!currentGame.getTreasures()[i].getOpened()) {
                map.addMarker(new MarkerOptions().position(currentGame.getTreasures()[i].getPosition()).icon(BitmapDescriptorFactory.fromResource(R.drawable.treasure)));
            }
        }
        saveGame();
    }

    /**
     * Saves the game to the internal storage.
     */
    private void saveGame() {
        if (currentGame != null) {
            try {
                FileOutputStream stream = openFileOutput("current_game.txt", Context.MODE_PRIVATE);
                StringBuilder gamemode = new StringBuilder();
                gamemode.append(currentGame.getGamemode());
                gamemode.append(",");
                gamemode.append(currentGame.getMaxDistance());
                String finalString = gamemode.toString();
                stream.write(finalString.getBytes());
                stream.write("\n".getBytes());
                for (int i = 0; i < currentGame.getTreasures().length; i++) {
                    Treasure temp = currentGame.getTreasures()[i];
                    String item = Double.toString(temp.getPosition().latitude) + "," + Double.toString(temp.getPosition().longitude) + "," + Boolean.toString(temp.getOpened());
                    stream.write(item.getBytes());
                    stream.write("\n".getBytes());
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            deleteCurrentGameFile();
        }
    }

    /**
     * Loads the game from the internal storage.
     * @return the {@code Game} loaded; {@code null} if internal storage did not contain any saved game
     */
    private Game loadGame() {
        if (new File(this.getFilesDir(), "current_game.txt").exists()) {
            try {
                File file = new File(this.getFilesDir(), "current_game.txt");
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bf = new BufferedReader(isr);
                String line;
                String gamemodeLine;
                String gamemode;
                int maxDistance;
                try {
                    int numberOfTreasures = 0;
                    while ((line = bf.readLine()) != null) {
                        numberOfTreasures++;
                    }
                    numberOfTreasures--;
                    fis.getChannel().position(0);
                    isr = new InputStreamReader(fis);
                    bf = new BufferedReader(isr);

                    gamemodeLine = bf.readLine();
                    String[] s = gamemodeLine.split(",");
                    gamemode = s[0];
                    maxDistance = Integer.parseInt(s[1]);

                    Treasure[] temp = new Treasure[numberOfTreasures];
                    int i = 0;
                    while ((line = bf.readLine()) != null) {
                        String[] atr = line.split(",");
                        double lat = Double.parseDouble(atr[0]);
                        double lng = Double.parseDouble(atr[1]);
                        boolean opened = Boolean.parseBoolean(atr[2]);
                        temp[i] = new Treasure(new LatLng(lat, lng), opened);
                        i++;
                    }
                    if (gamemode.equals("timetrial")) {
                        timerText.setVisibility(View.VISIBLE);
                        //startTimer(timeLeft);
                    } else {
                        timerText.setVisibility(View.GONE);
                    }
                    return new Game(temp, maxDistance, gamemode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Deletes the saved game file from the internal storage if it exists.
     */
    private void deleteCurrentGameFile() {
        if (new File(getFilesDir(), "current_game.txt").exists()) {
            new File(getFilesDir(), "current_game.txt").delete();
        }
    }

    /**
     * Handles all the different ways a game can be finished.
     * @param userCancelled true if the game was cancelled by the user (the timer running out also counts as userCancelled)
     * @param timeRanOut true if timer ran out
     */
    private void finishGame(boolean userCancelled, boolean timeRanOut) {
        deleteCurrentGameFile();
        if (currentGame.getGamemode().equals("standard")) {
            if (userCancelled) {
                map.clear();
                Toast.makeText(this, "Game canceled.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Game completed!", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (userCancelled) {
                if (timeRanOut) {
                    if (map != null) {
                        map.clear();
                    }
                    Toast.makeText(this, "Time ran out!", Toast.LENGTH_SHORT).show();
                } else {
                    map.clear();
                    Toast.makeText(this, "Game canceled.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Game completed!", Toast.LENGTH_SHORT).show();
            }
            unregisterReceiver(broadcastReceiver);
            stopService(new Intent(this, CountdownService.class));
            timerText.setVisibility(View.GONE);
        }
        currentGame = null;
        handleNewGameButton();
    }

    /**
     * Listens to taps on google map markers. Minigame starts if user is close to marker, otherwise map just pans to the marker.
     * If user has requested it the marker tapped instead gets replaced.
     */
    class MarkerListener implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            LatLng markerPosition = marker.getPosition();
            if (!userRemoveTreasure) {
                if (SphericalUtil.computeDistanceBetween(lastLocation, markerPosition) < 25) {
                    Intent intent = new Intent(MainMenuActivity.this, OpenTreasureMinigameActivity.class);
                    intent.putExtra("MARKER_KEY", markerPosition);
                    startActivityForResult(intent, 2);
                    return true;
                }
            } else {
                Treasure temp = new GameSetup(currentGame.getMaxDistance(), lastLocation).getNewTreasure();
                currentGame.replaceTreasure(marker.getPosition(), temp);
                map.clear();
                placeTreasures();
                userRemoveTreasure = false;
                return true;
            }
            return false;
        }
    }

    /**
     * Listens to taps on the Follow button. Sets the boolean requestingFollow and call method to change appearance of button accordingly.
     */
    class FollowButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            requestingFollow = !requestingFollow;
            handleFollowButton();
        }

    }

    /**
     * Listens to taps on the New Game button. Starts NewGameSetupActivity if there is no current game. If there is a current game it gets canceled if user confirms a dialog.
     */
    class NewGameListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (currentGame == null) {
                Intent intent = new Intent(MainMenuActivity.this, NewGameSetupActivity.class);
                startActivityForResult(intent, 1);
            } else {
                new AlertDialog.Builder(MainMenuActivity.this)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure you want to cancel the current game?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishGame(true, false);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        }
    }

    /**
     * Listens to taps on the Flag button. Sets the boolean userRemoveTreasure in order to work with MarkerListener.
     */
    class AssistButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (currentGame != null) {
                new AlertDialog.Builder(MainMenuActivity.this)
                        .setTitle("Replace treasure")
                        .setMessage("If one of the randomly placed treasures is out of reach press the Ok button and tap on the treasure in order to receive a new one.")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                userRemoveTreasure = true;
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            } else {
                Toast.makeText(MainMenuActivity.this, "Action only available when in game.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Listens to taps on the Information button. Starts InfoActivity if tapped.
     */
    class InfoButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainMenuActivity.this, InfoActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Changes color and text of Follow button according to boolean requestingFollow.
     */
    private void handleFollowButton() {
        if (requestingFollow) {
            tracklocation.setTextColor(Color.parseColor("#C23530"));
            tracklocation.setText("Stop Following");
        } else {
            tracklocation.setTextColor(Color.parseColor("#FFFFFF"));
            tracklocation.setText("Follow");
        }
    }

    /**
     * Changes color and text of New Game button based on currentGame object.
     */
    private void handleNewGameButton() {
        if (currentGame != null) {
            newGame.setTextColor(Color.parseColor("#C23530"));
            newGame.setText("Cancel Game");
        } else {
            newGame.setTextColor(Color.parseColor("#FFFFFF"));
            newGame.setText("New Game");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tracklocation.setEnabled(true);
                newGame.setEnabled(true);
                enableLocation();
                zoomToLastLocation(15);
            }
        }
    }

    /**
     * Enables location services if allowed, otherwise asks for permission.
     */
    private void enableLocation() {
        if (ContextCompat.checkSelfPermission(MainMenuActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
        } else {
            requestLocationPermission();
        }
    }

    /**
     * Starts location services if permission is allowed.
     */
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(MainMenuActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    /**
     * Requests permission to access users location through a dialog.
     */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This application needs access to your device location in order to function. Please tap 'Ok' to continue.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainMenuActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
    }

    /**
     * Pans & zooms the google map to the users location.
     * @param zoom amount of zoom
     */
    private void zoomToLastLocation(final int zoom) {
        if (ContextCompat.checkSelfPermission(MainMenuActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                public void onSuccess(Location location) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
                }
            });
        }
    }

    /**
     * Handles the timer display of the timer during the Timetrial gamemode.
     * @param intent contains time left from CountdownService
     */
    private void handleTimer(Intent intent) {
        long timeLeft = intent.getLongExtra("TIMELEFT_KEY", -1);
        long minutes = timeLeft / 60000;
        long seconds = timeLeft % 60000 / 1000;
        if (minutes >= 10 && seconds >= 10) {
            timerText.setText((timeLeft / 60000)+":"+(timeLeft % 60000 / 1000));
        } else if (minutes < 10 && seconds >= 10) {
            timerText.setText("0"+(timeLeft / 60000)+":"+(timeLeft % 60000 / 1000));
        } else if (seconds < 10 && minutes >= 10){
            timerText.setText((timeLeft / 60000)+":"+"0"+(timeLeft % 60000 / 1000));
        } else {
            timerText.setText("0"+(timeLeft / 60000)+":"+"0"+(timeLeft % 60000 / 1000));
        }
    }
}
