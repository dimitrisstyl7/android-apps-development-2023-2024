package com.dimstyl.chatroom;

import android.app.AlertDialog;
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

        // if user is authenticated, open chatroom
        if (FirebaseUtil.getUser() != null) {
            openChatroom();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        clearTextFields();
    }

    public void signIn(View view) {
        if (editTextsNotNullOrEmpty(List.of(emailEditText, passwordEditText))) {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            FirebaseUtil.signIn(email, password, this);
        } else {
            showMessage("Error", "Please provide all info!");
        }
    }

    public void signUp(View view) {
        if (editTextsNotNullOrEmpty(List.of(emailEditText, passwordEditText, nicknameEditText))) {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String nickname = nicknameEditText.getText().toString();
            FirebaseUtil.signUp(email, password, nickname, this);
        } else {
            showMessage("Error", "Please provide all info!");
        }
    }

    void openChatroom() {
        Intent intent = new Intent(this, ChatroomAvailableUsersActivity.class);
        intent.putExtra("uid", FirebaseUtil.getUID());
        intent.putExtra("nickname", FirebaseUtil.getNickname());
        startActivity(intent);
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", null)
                .show();
    }

    private boolean editTextsNotNullOrEmpty(List<EditText> editTexts) {
        return editTexts.stream()
                .noneMatch(editText -> editText == null || editText.getText().toString().trim().isEmpty());
    }

    private void clearTextFields() {
        emailEditText.setText("");
        passwordEditText.setText("");
        nicknameEditText.setText("");
    }
}