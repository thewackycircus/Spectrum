package com.example.test;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.content.Intent;
import android.icu.text.Edits;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.ui.login.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.Locale;

public class PaletteList extends AppCompatActivity {

    // Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("palettes");

    // components
    LinearLayout paletteListLayout;
    LayoutInflater inflater;
    CheckBox checkBox;
    EditText searchBar;

    // Google account
    GoogleSignInAccount account;

    String query = "";
    DataSnapshot palettes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette_list);

        // component refs
        paletteListLayout = findViewById(R.id.paletteListLayout);
        inflater = LayoutInflater.from(this);
        checkBox = findViewById(R.id.check_box);
        searchBar = findViewById(R.id.search_bar);

        // get google account
        account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        // when the database gets updated and also called once at beginning of onCreate
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                palettes = snapshot;
                ResetPaletteList(palettes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("READINGDATABASE", "Failed to real value.", error.toException());
            }
        });

        ImageButton newPaletteButton = findViewById(R.id.addPaletteButton);
        newPaletteButton.setOnClickListener(v -> {
            Intent newPaletteIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(newPaletteIntent);
            finish();
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ResetPaletteList(palettes);
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                query = charSequence.toString().toLowerCase(Locale.ROOT);
                ResetPaletteList(palettes);
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void onBackPressed() {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void ResetPaletteList(DataSnapshot palettes) {

        Iterator<DataSnapshot> paletteIterator = palettes.getChildren().iterator();

        // clear layout
        paletteListLayout.removeAllViews();

        while (paletteIterator.hasNext()) {

            // getting palette and setting it's id
            DataSnapshot next = paletteIterator.next();
            PaletteParcelable palette = next.getValue(PaletteParcelable.class);

            if (checkBox.isChecked()) {
                if (palette.getTitle().toLowerCase(Locale.ROOT).contains(query) || query.isEmpty()) {
                    if (palette.getUserId().equals(account.getEmail())) {
                        InflateLinearLayout(palette);
                    }
                }
            } else {
                if (palette.getTitle().toLowerCase(Locale.ROOT).contains(query)
                        || palette.getUserId().toLowerCase(Locale.ROOT).contains(query)
                        || query.isEmpty())
                { InflateLinearLayout(palette); }
            }
        }
    }

    private void InflateLinearLayout(PaletteParcelable palette) {
        // inflating linear layout to activity
        PaletteListItem paletteListItem = (PaletteListItem) inflater.inflate(R.layout.palette_list_item, paletteListLayout, false);
        paletteListItem.initDisplay(palette);

        paletteListLayout.addView(paletteListItem);
    }
}