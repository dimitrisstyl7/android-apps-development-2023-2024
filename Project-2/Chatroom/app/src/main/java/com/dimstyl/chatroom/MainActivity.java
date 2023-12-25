package com.dimstyl.chatroom;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, nicknameEditText;
    private Button signInButton, signUpButton, signOutButton, chatroomButton;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nicknameEditText = findViewById(R.id.nicknameEditText);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
        signOutButton = findViewById(R.id.signOutButton);
        chatroomButton = findViewById(R.id.chatroomButton);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Check if user is authenticated and set buttons visibility accordingly
        setButtonsVisibility(user != null);
    }

    public void signIn(View view) {
        if (editTextsNotEmpty(List.of(emailEditText, passwordEditText))) {
            auth.signInWithEmailAndPassword(
                    emailEditText.getText().toString(),
                    passwordEditText.getText().toString()
            ).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user = auth.getCurrentUser();
                            setButtonsVisibility(true);
                            showMessage("Success", "You are signed in!");
                        } else {
                            user = null;
                            setButtonsVisibility(false);
                            showMessage("Error", "Check your credentials!");
                        }
                    }
            );
        } else {
            showMessage("Error", "Please provide all info!");
        }
    }

    public void signUp(View view) {
        if (editTextsNotEmpty(List.of(emailEditText, passwordEditText, nicknameEditText))) {
            auth.createUserWithEmailAndPassword(
                            emailEditText.getText().toString(),
                            passwordEditText.getText().toString()
                    )
                    .addOnSuccessListener(authResult -> {
                                user = auth.getCurrentUser();

                                if (user == null) {
                                    showMessage("Error", "Something went wrong, contact support!");
                                    auth.signOut();
                                    user = null;
                                    setButtonsVisibility(false);
                                    return;
                                }

                                updateUser(user, nicknameEditText.getText().toString());
                                setButtonsVisibility(true);
                                showMessage("Success", "User profile created!");
                            }
                    )
                    .addOnFailureListener(e -> {
                                user = null;
                                setButtonsVisibility(false);
                                showMessage("Error", "Check your credentials!\nWarning: Password must be at least 6 characters long!");
                            }
                    );
        } else {
            showMessage("Error", "Please provide all info!");
        }
    }

    public void signOut(View view) {
        auth.signOut();
        user = null;
        setButtonsVisibility(false);
        clearTextFields();
        showMessage("Success", "You are signed out!");
    }

    public void chatroom(View view) {
    }

    private void updateUser(FirebaseUser user, String nickname) {
        user.updateProfile(
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(nickname)
                        .build()
        );
    }

    private void setButtonsVisibility(boolean userAuthenticated) {
        nicknameEditText.setVisibility(userAuthenticated ? View.GONE : View.VISIBLE);
        signInButton.setVisibility(userAuthenticated ? View.GONE : View.VISIBLE);
        signUpButton.setVisibility(userAuthenticated ? View.GONE : View.VISIBLE);
        signOutButton.setVisibility(userAuthenticated ? View.VISIBLE : View.GONE);
        chatroomButton.setVisibility(userAuthenticated ? View.VISIBLE : View.GONE);
    }

    private void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }

    private boolean editTextsNotEmpty(List<EditText> editTexts) {
        return editTexts.stream()
                .noneMatch(editText -> editText == null || editText.getText().toString().trim().isEmpty());
    }

    private void clearTextFields() {
        emailEditText.setText("");
        passwordEditText.setText("");
        nicknameEditText.setText("");
    }
}