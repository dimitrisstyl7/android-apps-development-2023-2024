package com.dimstyl.chatroom;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChatroomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        TextView chattingWithtextView = findViewById(R.id.chattingWithTextView);

        String receiverUid = getIntent().getStringExtra("receiverUid");

        if (receiverUid == null) {
            showMessage("Error", "Something went wrong, please try again.");
            return;
        }

        String receiverNickname =
                receiverUid.equals(FirebaseUtil.getUid()) ? "yourself" : getIntent().getStringExtra("receiverNickname");
        chattingWithtextView.setText(getString(R.string.chatting_with, receiverNickname));
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }

    void showMessage(String title, String message, String neutralButtonText, DialogInterface.OnClickListener onClickListenerForNeutralButton) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(neutralButtonText, onClickListenerForNeutralButton)
                .show();
    }
}