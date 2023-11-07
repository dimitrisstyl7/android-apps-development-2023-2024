package com.example.androidapp;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.sql.Timestamp;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private View mainActivity;
    private TextView speed_textView;
    private LocationManager locationManager;
    private SharedPreferences preferences;
    private float speedLimit;
    private EditText speedLimitEditText;
    private SQLiteDatabase database;
    private RadioButton weekViolationsRadioButton, allViolationsRadioButton;
    private TextView warningTextView;
    private MyTextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textToSpeech = new MyTextToSpeech(this);
        databaseConnection();
        setContentView(R.layout.activity_main);
        mainActivity = findViewById(R.id.main_activity);
        speed_textView = findViewById(R.id.speedTextView);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        preferences = getPreferences(MODE_PRIVATE);
        readSpeedLimit();
        speedLimitEditText = findViewById(R.id.speedLimitEditText);
        speedLimitEditText.setText(String.valueOf(speedLimit));
        weekViolationsRadioButton = findViewById(R.id.weekViolationsRadioButton);
        allViolationsRadioButton = findViewById(R.id.allViolationsRadioButton);
        warningTextView = findViewById(R.id.warningTextView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }

    private void databaseConnection() {
        database = openOrCreateDatabase("speedLimitViolation.db", MODE_PRIVATE, null);
        database.execSQL("Create table if not exists SpeedLimitViolation(id integer primary key autoincrement, longitude text, latitude text, speed text, timestamp text);");
    }

    public void getSpeed(View view) {
        // Check if the permission has already been granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // If granted, register for location updates.
            long minTime = 3000; // Minimum time interval between location updates, in milliseconds.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, 0, this);
        } else {
            // If not granted, request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        float speed = location.getSpeed();
        speed_textView.setText(String.format("%s m/s", speed));
        checkSpeedLimitViolation(speed, location);
    }

    public void stopSpeedMeasurement(View view) {
        // Stop location updates.
        locationManager.removeUpdates(this);
        mainActivity.setBackgroundColor(ContextCompat.getColor(this, R.color.main_activity_bg_color));
        warningTextView.setVisibility(View.INVISIBLE);
        speed_textView.setText("-");
    }

    private void readSpeedLimit() {
        speedLimit = preferences.getFloat("speedLimit", -1);
        if (speedLimit == -1) {
            speedLimit = 40;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat("speedLimit", speedLimit);
            editor.apply();
        }
    }

    public void saveSpeedLimit(View view) {
        // Check if user has entered a speed limit.
        if (speedLimitEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter a speed limit", Toast.LENGTH_LONG).show();
            return;
        }
        // Save the new speed limit.
        speedLimit = Float.parseFloat(speedLimitEditText.getText().toString());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("speedLimit", speedLimit);
        editor.apply();

        // Inform the user that the new speed limit has been saved.
        Toast.makeText(this, "New speed limit saved", Toast.LENGTH_LONG).show();
    }

    private void checkSpeedLimitViolation(float speed, Location location) {
        // Check if the speed is greater than the speed limit.
        if (speed > speedLimit) {
            // Change activity color to red.
            mainActivity.setBackgroundColor(ContextCompat.getColor(this, R.color.red));

            // Change the visibility of the warning text view to visible.
            warningTextView.setVisibility(View.VISIBLE);

            // Speak the warning.
            textToSpeech.speak(warningTextView.getText().toString());

            // Insert speed, longitude, latitude and timestamp into the database.
            insertDataIntoDatabase(speed, location);
        } else {
            // Change activity color to main_activity_bg_color.
            mainActivity.setBackgroundColor(ContextCompat.getColor(this, R.color.main_activity_bg_color));

            // Change the visibility of the warning text view to invisible.
            warningTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void insertDataIntoDatabase(float speed, Location location) {
        String speedString = String.valueOf(speed);
        String longitudeString = String.valueOf(location.getLongitude());
        String latitudeString = String.valueOf(location.getLatitude());
        String timestampString = String.valueOf(new Timestamp(System.currentTimeMillis()));
        System.out.printf("Speed: %s, Longitude: %s, Latitude: %s, Timestamp: %s%n", speedString, longitudeString, latitudeString, timestampString);
        String parameterizedQuery = "Insert into SpeedLimitViolation(longitude, latitude, speed, timestamp) values(?, ?, ?, ?);";
        database.execSQL(parameterizedQuery, new String[]{longitudeString, latitudeString, speedString, timestampString});
    }

    public void showViolations(View view) {
        if (weekViolationsRadioButton.isChecked()) {
            showWeekViolations();
        } else if (allViolationsRadioButton.isChecked()) {
            showAllViolations();
        } else {
            Toast.makeText(this, "Please select an option", Toast.LENGTH_LONG).show();
        }
    }

    private void showWeekViolations() {
        try (Cursor cursor = database.rawQuery("Select * from SpeedLimitViolation;", null)) {
            StringBuilder data = new StringBuilder();
            while (cursor.moveToNext()) {
                // Get the year, month and day from the timestamp.
                String timestamp = cursor.getString(4);
                String year = timestamp.substring(0, 4);
                String month = timestamp.substring(5, 7);
                String day = timestamp.substring(8, 10);

                // Get the current year, month and day.
                String currYear = String.valueOf(new Timestamp(System.currentTimeMillis())).substring(0, 4);
                String currMonth = String.valueOf(new Timestamp(System.currentTimeMillis())).substring(5, 7);
                String currDay = String.valueOf(new Timestamp(System.currentTimeMillis())).substring(8, 10);

                // Check if the violation occurred in the current week.
                if (year.equals(currYear) && month.equals(currMonth) && Integer.parseInt(currDay) - Integer.parseInt(day) <= 7) {
                    data.append("Longitude: ").append(cursor.getString(1)).append("\n");
                    data.append("Latitude: ").append(cursor.getString(2)).append("\n");
                    data.append("Speed: ").append(cursor.getString(3)).append("\n");
                    data.append("Timestamp: ").append(timestamp).append("\n");
                    data.append("-----------------\n");
                }
            }
            showMessage("Last 7 days violations", data.toString());
        }
    }

    private void showAllViolations() {
        try (Cursor cursor = database.rawQuery("Select * from SpeedLimitViolation;", null)) {
            StringBuilder data = new StringBuilder();
            while (cursor.moveToNext()) {
                data.append("Longitude: ").append(cursor.getString(1)).append("\n");
                data.append("Latitude: ").append(cursor.getString(2)).append("\n");
                data.append("Speed: ").append(cursor.getString(3)).append("\n");
                data.append("Timestamp: ").append(cursor.getString(4)).append("\n");
                data.append("-----------------\n");
            }
            showMessage("All violations", data.toString());
        }
    }

    private void showMessage(String title, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .show();
    }

    public void clearDatabase(View view) {
        database.execSQL("Delete from SpeedLimitViolation;");
        Toast.makeText(this, "Database cleared", Toast.LENGTH_LONG).show();
    }
}