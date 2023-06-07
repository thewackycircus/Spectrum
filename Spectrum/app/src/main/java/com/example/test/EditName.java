package com.example.test;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditName extends AppCompatActivity {

    // Creating a reference to the firebase database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("palettes");

    // element refs
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name);

        // getting palette object
        Intent intent = getIntent();
        PaletteParcelable palette = intent.getParcelableExtra("palette");

        // component refs
        editText = findViewById(R.id.editText);
        ImageButton backButton = findViewById(R.id.backButton);
        ImageButton nextButton = findViewById(R.id.nextButton);

        // Setting EditText
        if (!palette.getTitle().isEmpty()) {
            Log.i("Search", palette.getTitle());
            editText.setText(palette.getTitle());
        }

        // setting window size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        double width = dm.widthPixels * .8;
        double height = dm.heightPixels * .2;
        getWindow().setLayout((int)width, (int)height);

        // when back button is pressed, return to main activity with palette object
        backButton.setOnClickListener(v -> {
            Intent backIntent = new Intent();
            backIntent.putExtra("palette", palette);
            setResult(RESULT_OK, backIntent);
            finish();
        });

        // when next button is pressed, save title to palette, save palette to database, start palette list activity
        nextButton.setOnClickListener(v -> {

            palette.setTitle(editText.getText().toString());
            String key;

            // adding palette to database
            if(palette.getId() == null) {
                // if palette does not yet have an id, create a key and set palette to it.
                key = ref.push().getKey();
                palette.setId(key);
            } else {
                // if palette already has id, set the key to it
                key = palette.getId();
            }

            ref.child(key).setValue(palette);

            Intent paletteListIntent = new Intent(this, PaletteList.class);
            startActivity(paletteListIntent);
            finish();
        });
    }
}