package com.example.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PaletteListItem extends LinearLayout {

    // Firebase
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("palettes");

    // components
    LinearLayout paletteColoursDisplay;
    ImageButton deletePaletteButton;
    ImageButton editPaletteButton;
    ImageButton editPaletteNameButton;

    // CONSTRUCTORS

    public PaletteListItem(Context context) {
        super(context);
    }

    public PaletteListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaletteListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // PUBLIC FUNCTIONS
    public void initDisplay(PaletteParcelable palette){

        // add title to display
        TextView title = this.findViewById(R.id.paletteTitle);
        title.setText(palette.getTitle());

        // format userID
        int index = palette.getUserId().indexOf("@");
        String userID_S = palette.getUserId().substring(0, index);

        // add user ID to display
        TextView userID = this.findViewById(R.id.user_id);
        userID.setText("By User: " + userID_S);

        for (int i = 0; i < 5; i++) {
            if (palette.getColours().size() > i) {

                paletteColoursDisplay = findViewById(R.id.paletteColoursDisplay);

                // make colour view display and add it to linear view
                View colourView  = new View(getContext());
                colourView.setBackgroundColor(palette.getColours().get(i));
                colourView.setLayoutParams(new LinearLayout.LayoutParams(
                        Utils.pxFromDp(getContext(), 50),
                        Utils.pxFromDp(getContext(), 50)
                ));
                paletteColoursDisplay.addView(colourView);
            }
        }

        // BUTTONS
        deletePaletteButton = this.findViewById(R.id.deletePaletteButton);
        deletePaletteButton.setOnClickListener(v -> {
            ref.child(palette.getId()).removeValue();
        });

        editPaletteButton = this.findViewById(R.id.editPaletteButton);
        editPaletteButton.setOnClickListener(v -> {
            Intent editPaletteIntent = new Intent(getContext(), MainActivity.class);
            editPaletteIntent.putExtra("palette", palette);

            Activity host = (Activity)getContext();
            host.startActivity(editPaletteIntent);
            host.finish();
        });

        editPaletteNameButton = this.findViewById(R.id.editNameButton);
        editPaletteNameButton.setOnClickListener(v -> {
            Intent editPaletteNameIntent = new Intent(getContext(), EditName.class);
            editPaletteNameIntent.putExtra("palette", palette);
            Activity host = (Activity)getContext();
            host.startActivityForResult(editPaletteNameIntent, 1);
        });
    }
}
