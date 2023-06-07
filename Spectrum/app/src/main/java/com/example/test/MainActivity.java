package com.example.test;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // element refs
    ConstraintLayout parentLayout;
    View selectedColourView;
    LinearLayout displayLayout;
    LinearLayout linearLayout;
    ColourWheel colourWheel;


    // sensor management
    SensorManager sensorManager;
    float accel;
    float accelLast;
    float accelCurrent;
    boolean canShake = true;
    float shakeSensitivity = 3;

    // login management
    String userId;
    GoogleSignInAccount account;

    PaletteParcelable palette;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // component refs
        parentLayout = findViewById(R.id.parentLayout);
        selectedColourView = findViewById(R.id.selectedColourDisplay);
        displayLayout = findViewById(R.id.displayLayout);
        colourWheel = findViewById(R.id.colourWheel);
        ImageButton addColourButton = findViewById(R.id.addColourButton);
        ImageButton deleteColourButton = findViewById(R.id.deleteColourButton);
        ImageButton savePaletteButton = findViewById(R.id.savePaletteButton);
        ImageButton paletteListButton = findViewById(R.id.paletteListButton);

        // Login management
        account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account != null) {
            userId = account.getEmail();
        }

        //  creating a drawable field around the palette image view
        colourWheel.setDrawingCacheEnabled(true);
        colourWheel.buildDrawingCache(true);

        // giving the colour wheel a reference to the target view
        colourWheel.setSubject(selectedColourView);

        // if "palette" exists, use for palette, else create a new one
        if (getIntent().getParcelableExtra("palette") != null) {
            palette = getIntent().getParcelableExtra("palette");
            reformatPaletteDisplay();
        } else {
            palette = new PaletteParcelable("new Palette", new ArrayList<>(), userId);
        }

        // when createButton is pressed, call paletteCreator Activity and parse through the selected colour
        addColourButton.setOnClickListener(v -> {
            int selectedColour = colourWheel.getColour();

            AddColourToPalette(selectedColour);
        });

        // when delete button is pressed, find all colours that care selected and remove them
        deleteColourButton.setOnClickListener(v -> {
            for (int i = displayLayout.getChildCount() - 1; i >= 0; i--) {

                LinearLayout layout = (LinearLayout) displayLayout.getChildAt(i);

                for (int j = layout.getChildCount() - 1; j >= 0; j--) {

                    ColourView colourView = (ColourView) layout.getChildAt(j);

                    if (colourView.isSelected) {
                        palette.removeColour((i * 5) + j);
                    }
                }
            }

            reformatPaletteDisplay();
        });

        // when save button is pressed, save palette object to database and open edit name activity
        savePaletteButton.setOnClickListener(v -> {

            if (displayLayout.getChildCount() == 0) {
                Toast.makeText(this, "Add colour to palette", Toast.LENGTH_SHORT).show();
            } else {
                if (getIntent().getParcelableExtra("palette") != null) {
                    // Firebase
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference ref = database.getReference("palettes");

                    // updating palette record on database
                    ref.child(palette.getId()).setValue(palette);

                    // open paletteList Activity
                    Intent savePaletteIntent = new Intent(this, PaletteList.class);
                    savePaletteIntent.putExtra("palette", palette);
                    startActivity(savePaletteIntent);
                    finish();
                } else {
                    Intent editNameIntent = new Intent(this, EditName.class);
                    editNameIntent.putExtra("palette", palette);
                    startActivityForResult(editNameIntent, 1);
                }
            }
        });

        // when this button is pressed, navigate to paletteList Activity
        paletteListButton.setOnClickListener(v -> {
            Intent paletteListIntent = new Intent(this, PaletteList.class);
            startActivity(paletteListIntent);
            finish();
        });

        // sensor setup with onCreate function
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        accel = 0.00f;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;
    }

    // Re register sensor when app is resumed
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Unregister listener when app is not in use
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1) {
            palette = data.getParcelableExtra("palette");
        }
    }

    // Shake Functionality
    private final SensorEventListener sensorEventListener = new SensorEventListener() {

        //Called whenever any values within the registered sensor change
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // Gathering position of sensor in real world
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            // accel calculations
            accelLast = accelCurrent;
            accelCurrent = (float) Math.sqrt((double) (x*x + y*y + z*z));
            float delta = accelCurrent - accelLast;
            accel = accel * 0.9f + delta;

            // check if Shake function should be called
            if (accel > shakeSensitivity && canShake) {
                Shake();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };
    private void Shake() {

        // turn off colour wheel interactivity
        canShake = false;

        // spin colour wheel animation
        colourWheel.animate().rotationBy(360f * 3).setDuration(1000).withEndAction(new Runnable() {

            // function is not run until end of animation due to the .start() at the end
            @Override
            public void run() {

                // turn on colour wheel interactivity
                canShake = true;

                // Checking if any colours exist in palette
                if (linearLayout == null || linearLayout.getChildCount() == 0) {

                    // if no colours in palette
                    // Add a random colour to palette
                    AddColourToPalette(Color.rgb(RandomInteger(0, 255), RandomInteger(0, 255), RandomInteger(0, 255)));

                    // Add three more harmonising colours
                    ColourView colourView = (ColourView) linearLayout.getChildAt(0);
                    for (int i = 0; i < 3; i++) {
                        AddColourToPalette(getHarmonisingColour(colourView.getSelectedColour()));
                    }
                } else {
                    // adding harmonising colour based upon 1st colour in the palette
                    ColourView colourView = (ColourView) linearLayout.getChildAt(0);
                    AddColourToPalette(getHarmonisingColour(colourView.getSelectedColour()));
                }
            }
        }).start();
    }

    // Move all remaining colours to front of display
    private void reformatPaletteDisplay() {
        // restart displayLayout
        displayLayout.removeAllViews();

        if (palette.getColours().size() > 0) {
            // go through each colour stored in palette and add them to display
            for (int colour : palette.getColours()) {
                // add new layout if current one is full
                linearLayout = (LinearLayout) displayLayout.getChildAt(displayLayout.getChildCount() - 1);
                if (linearLayout == null || linearLayout.getChildCount() == 5) {
                    linearLayout = new LinearLayout(getApplicationContext());
                    linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            Utils.pxFromDp(getApplicationContext(), 350),
                            Utils.pxFromDp(getApplicationContext(), 70)
                    ));
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    displayLayout.addView(linearLayout);
                }

                // create a new view to store the current colour and add to current linearLayout
                ColourView display = new ColourView(getApplicationContext());
                display.setLayoutParams(new LinearLayout.LayoutParams(
                        Utils.pxFromDp(getApplicationContext(), 70),
                        Utils.pxFromDp(getApplicationContext(), 70)
                ));
                display.setSelectedColour(colour);
                display.isSelected = false;
                linearLayout.addView(display);
            }
        } else {
            // create new layout for addColourButton to work with
            linearLayout = new LinearLayout(getApplicationContext());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    Utils.pxFromDp(getApplicationContext(), 350),
                    Utils.pxFromDp(getApplicationContext(), 70)
            ));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            displayLayout.addView(linearLayout);
        }
    }

    // add colour to the palette
    private void AddColourToPalette(int colorToAdd) {

        // if there are 0 or a multiple five colours, create a new linearLayout row in the scrollview
        if (linearLayout == null || linearLayout.getChildCount() == 5) {
            linearLayout = new LinearLayout(getApplicationContext());
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    Utils.pxFromDp(getApplicationContext(), 350),
                    Utils.pxFromDp(getApplicationContext(), 70)
            ));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            displayLayout.addView(linearLayout);
        }

        // create a new view to store the new colour and add to current linearLayout
        ColourView display = new ColourView(getApplicationContext());
        display.setLayoutParams(new LinearLayout.LayoutParams(
                Utils.pxFromDp(getApplicationContext(), 70),
                Utils.pxFromDp(getApplicationContext(), 70)
        ));
        display.setSelectedColour(colorToAdd);
        linearLayout.addView(display);

        // add colour to palette Colours arrayList
        palette.addColour(colorToAdd);
    }

    // returns a random colour that harmonises with the provided colour
    public int getHarmonisingColour(int colorToHarmonise) {

        int red = Color.red(colorToHarmonise);
        int green = Color.green(colorToHarmonise);
        int blue = Color.blue(colorToHarmonise);

        // convert rbg to hsv
        float[] HSVColorToHarmonise = new float[3];
        Color.RGBToHSV(red, green, blue, HSVColorToHarmonise);

        // 180 for complimentary colours, 90 for quadratic colours
        float hue = HSVColorToHarmonise[0];
        HSVColorToHarmonise[0] = KeepHueWithin360(hue + 180);;
        int colorToAdd = Color.HSVToColor(HSVColorToHarmonise);

        // Handle duplicate colours
        if (IsColorInPalette(colorToAdd) == true) {
            HSVColorToHarmonise[0] = KeepHueWithin360(hue + 90);
            colorToAdd = Color.HSVToColor(HSVColorToHarmonise);
        }

        if (IsColorInPalette(colorToAdd) == true) {
            HSVColorToHarmonise[0] = KeepHueWithin360(hue - 90);
            colorToAdd = Color.HSVToColor(HSVColorToHarmonise);
        }

        if (IsColorInPalette(colorToAdd) == true) {
            if (RandomInteger(0, 1) == 0) { return Color.BLACK; }
            else { return Color.WHITE; }
        }

        return colorToAdd;
    }

    // returns a random integer
    private int RandomInteger(int min, int max) {
        Random r = new Random();
        return r.nextInt((max + 1) - min) + min;
    }

    // Checks hue in not above 360 or below 1
    private float KeepHueWithin360(float hue) {
        if (hue > 360) {
            return hue - 360;
        } else if (hue < 1) {
            return 360 - hue;
        } else {
            return hue;
        }
    }

    // Checks to see if a given colour is already in the palette
    private boolean IsColorInPalette(int colorToCheck) {
        // for each colour view in linear view (only checks one row in palette)
        for (int i = 0; i < linearLayout.getChildCount(); i++) {

            // if colour to add matches get selected colour
            ColourView color = (ColourView) linearLayout.getChildAt(i);
            if (color.getSelectedColour() == colorToCheck) {
                return true;
            }
        }

        return false;
    }
}