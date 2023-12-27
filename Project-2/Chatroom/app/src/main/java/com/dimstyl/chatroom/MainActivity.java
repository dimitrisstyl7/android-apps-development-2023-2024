package com.dimstyl.chatroom;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, nicknameEditText;
    private Button signInButton, signUpButton, signOutButton, chatroomButton;
    private View separatorView_2;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private String UID;

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
        separatorView_2 = findViewById(R.id.separatorView_2);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Check if user is authenticated and set buttons visibility accordingly
        user = auth.getCurrentUser();
        setEditTextsAndButtonsVisibility(user != null);

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance();
    }

    public void signIn(View view) {
        if (editTextsNotEmpty(List.of(emailEditText, passwordEditText))) {
            auth.signInWithEmailAndPassword(
                    emailEditText.getText().toString(),
                    passwordEditText.getText().toString()
            ).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user = auth.getCurrentUser();

                            if (user == null) {
                                showMessage("Error", "Something went wrong, try again!");
                                auth.signOut();
                                user = null;
                                setEditTextsAndButtonsVisibility(false);
                                return;
                            }

                            UID = user.getUid();
                            setEditTextsAndButtonsVisibility(true);
                            showMessage("Success", "You are signed in!");
                        } else {
                            user = null;
                            setEditTextsAndButtonsVisibility(false);
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
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Create authenticated user
            auth.createUserWithEmailAndPassword(
                            email,
                            password
                    )
                    .addOnSuccessListener(authResult -> {
                                user = auth.getCurrentUser();

                                if (user == null) {
                                    showMessage("Error", "Something went wrong, contact support!");
                                    auth.signOut();
                                    user = null;
                                    setEditTextsAndButtonsVisibility(false);
                                    return;
                                }

                                // Set authenticated user's nickname
                                String nickname = nicknameEditText.getText().toString();
                                setUserNickname(nickname);

                                // Add user's uid, email and nickname to database (for future use - chatroom)
                                UID = user.getUid();
                                addUserToDatabase(email, nickname);

                                setEditTextsAndButtonsVisibility(true);
                                showMessage("Success", "User profile created!");
                            }
                    )
                    .addOnFailureListener(e -> {
                                user = null;
                                setEditTextsAndButtonsVisibility(false);
                                showMessage("Error", "Check your credentials!\n\nWarning:\n\t> Password must be at least 6 characters long!\n\t> Email must not be already in use!");
                            }
                    );
        } else {
            showMessage("Error", "Please provide all info!");
        }
    }

    private void addUserToDatabase(String email, String nickname) {
        DatabaseReference reference = database.getReference().child("users").child(UID);
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("nickname", nickname);
        reference.setValue(userData);
    }

    public void signOut(View view) {
        auth.signOut();
        user = null;
        setEditTextsAndButtonsVisibility(false);
        clearTextFields();
        showMessage("Success", "You are successfully signed out!");
    }

    public void chatroom(View view) {
        if (user != null) {
            Intent intent = new Intent(this, ChatroomActivity.class);
            intent.putExtra("uid", UID);
            startActivity(intent);
        } else {
            showMessage("Error", "Something went wrong, please sign in again!");
        }
    }

    private void setUserNickname(String nickname) {
        user.updateProfile(
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(nickname)
                        .build()
        );
    }

    private void setEditTextsAndButtonsVisibility(boolean userAuthenticated) {
        // Edit texts visibility
        emailEditText.setVisibility(userAuthenticated ? View.GONE : View.VISIBLE);
        passwordEditText.setVisibility(userAuthenticated ? View.GONE : View.VISIBLE);
        nicknameEditText.setVisibility(userAuthenticated ? View.GONE : View.VISIBLE);

        // Buttons visibility
        signInButton.setVisibility(userAuthenticated ? View.GONE : View.VISIBLE);
        signUpButton.setVisibility(userAuthenticated ? View.GONE : View.VISIBLE);
        signOutButton.setVisibility(userAuthenticated ? View.VISIBLE : View.GONE);
        chatroomButton.setVisibility(userAuthenticated ? View.VISIBLE : View.GONE);

        // Separator view 2 visibility
        separatorView_2.setVisibility(userAuthenticated ? View.GONE : View.VISIBLE);
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