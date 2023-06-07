package com.example.test.ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.MainActivity;
import com.example.test.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    // Google sign in management
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    GoogleSignInAccount account;
    SignInButton signInButton;
    ImageButton startButton;
    Button signOutButton;
    TextView loginInfo;
    LinearLayout loginLayout;
    LinearLayout playLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //signOutButton setup
        signOutButton = findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(v -> SignOut());

        // signInButton setup
        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> SignIn());

        // startButton setup
        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> Start());

        // google sign in setup
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this, gso);

        // text view setup
        loginInfo = findViewById(R.id.login_info);
        account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        // linear layout setups
        playLayout = findViewById(R.id.play_layout);
        loginLayout = findViewById(R.id.login_layout);

        CheckIfAnyoneIsLoggedIn();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                task.getResult(ApiException.class);
                Log.i("Login", "Login Success");

            } catch (ApiException e) {
                Log.i("Login", "Something went wrong with the login: " + e);
                Toast.makeText(getApplicationContext(), "Something went wrong with the login: ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        CheckIfAnyoneIsLoggedIn();
    }

    // SignIn functionality
    private void SignIn() {
        Intent signInIntent = gsc.getSignInIntent();
        startActivityForResult(signInIntent, 1000);
    }

    // SignOut functionality
    private void SignOut() {
        gsc.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                playLayout.setVisibility(View.GONE);
                loginLayout.setVisibility((View.VISIBLE));
                Toast.makeText(LoginActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // start button
    private void Start() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    // check if anyone is logged in
    private void CheckIfAnyoneIsLoggedIn() {
        account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        if (account == null) {
            playLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        } else {
            playLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
            loginInfo.setText(account.getEmail() + " is logged in.");
        }
    }
}