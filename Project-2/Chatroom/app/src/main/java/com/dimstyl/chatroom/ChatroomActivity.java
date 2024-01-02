package com.dimstyl.chatroom;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ChatroomActivity extends AppCompatActivity {
    private EditText inputEditText;
    private LinearLayout messagesLinearLayout;
    private String receiverUid;
    private String receiverNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        messagesLinearLayout = findViewById(R.id.messagesLinearLayout);
        TextView chattingWithtextView = findViewById(R.id.chattingWithTextView);
        inputEditText = findViewById(R.id.inputEditText);

        receiverUid = getIntent().getStringExtra("receiverUid");
        if (receiverUid == null) {
            showMessage("Oops...", "Something went wrong, please try again.", "Close", (dialogInterface, i) -> finish());
            return;
        }

        receiverNickname =
                receiverUid.equals(FirebaseUtil.getUid()) ? "yourself" : getIntent().getStringExtra("receiverNickname");
        chattingWithtextView.setText(getString(R.string.chatting_with, receiverNickname));

        /*
         * Add listener for messages.
         * At the beginning, it will fill linearLayout with old messages.
         * After that, it will add new messages to linearLayout.
         * */
        FirebaseUtil.addChildEventListener(receiverUid, this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        messagesLinearLayout.removeAllViews();
        inputEditText.setText("");
    }

    void addMessageToLinearLayout(Message message) {
        TextView textView = createTextView(message);
        messagesLinearLayout.addView(textView);

        // Set focus to each message (scroll to bottom)
        messagesLinearLayout.requestChildFocus(textView, textView);
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