package com.dimstyl.chatroom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText emailEditText, passwordEditText, nicknameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        nicknameEditText = findViewById(R.id.nicknameEditText);

        // if user is signed in, open available users activity
        if (FirebaseUtil.isSignedIn()) {
            startAvailableUsersActivity();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // if user is still signed in, sign out him
        if (FirebaseUtil.isSignedIn()) {
            FirebaseUtil.signOut();
        }
        clearTextFields();
    }

    public void signIn(View view) {
        if (editTextsEmpty(List.of(emailEditText, passwordEditText))) {
            showMessage("Error", "Please provide all info!");
            return;
        }
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        FirebaseUtil.signIn(email, password, this);
    }

    public void signUp(View view) {
        if (editTextsEmpty(List.of(emailEditText, passwordEditText, nicknameEditText))) {
            showMessage("Error", "Please provide all info!");
            return;
        }

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String nickname = nicknameEditText.getText().toString();

        if (notMatchNicknameLengthConstraint(nickname)) {
            showMessage("Error", "Nickname must be between 3 and 15 characters long!");
            return;
        }

        FirebaseUtil.signUp(email, password, nickname, this);
    }

    void startAvailableUsersActivity() {
        startActivity(new Intent(this, AvailableUsersActivity.class));
    }

    private boolean editTextsEmpty(List<EditText> editTexts) {
        return editTexts.stream().anyMatch(editText -> editText.getText().toString().trim().isEmpty());
    }

    private void clearTextFields() {
        emailEditText.setText("");
        passwordEditText.setText("");
        nicknameEditText.setText("");
    }

    private boolean notMatchNicknameLengthConstraint(String nickname) {
        return nickname.length() < 3 || nickname.length() > 15;
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }

    void showMessage(String title, String message,
                     String positiveButtonText, DialogInterface.OnClickListener onClickListenerForPositiveButton,
                     String negativeButtonText, DialogInterface.OnClickListener onClickListenerForNegativeButton) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, onClickListenerForPositiveButton)
                .setNegativeButton(negativeButtonText, onClickListenerForNegativeButton)
                .show();
    }
}