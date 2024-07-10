package com.dimstyl.chatroom;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AvailableUsersActivity extends AppCompatActivity {
    private LinearLayout usersLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_users);

        TextView userNicknameTextView = findViewById(R.id.userNicknameTextView);
        userNicknameTextView.setText(getString(R.string.signed_in_as, FirebaseUtil.getNickname()));

        usersLinearLayout = findViewById(R.id.usersLinearLayout);
    }

    @Override
    protected void onStart() {
        super.onStart();
        usersLinearLayout.removeAllViews();
        fillLinearLayoutWithUsers();
    }

    public void signOut(View view) {
        signOut();
    }

    void signOut() {
        FirebaseUtil.signOut();
        finish();
    }

    private void fillLinearLayoutWithUsers() {
        // Add button for chat with all users
        addUserToLinearLayout(null);

        // Add button for chat with current user (himself)
        addUserToLinearLayout(new User(FirebaseUtil.getUid(), FirebaseUtil.getEmail(), FirebaseUtil.getNickname()));

        // Add button for each registered user (except current user)
        FirebaseUtil.getAllUsers(this);
    }

    void addUserToLinearLayout(User user) {
        // Create new button
        Button button = createButton(user);

        // Add button to LinearLayout
        usersLinearLayout.addView(button);
    }

    private Button createButton(User user) {
        // Create new button
        Button button = new Button(this);

        // Set button text alignment, text size and background color
        button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        button.setTextSize(22);
        button.setBackgroundColor(getColor(R.color.white));

        // Set button font family
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/AkayaTelivigala-Regular.ttf");
        button.setTypeface(typeface);

        // Set button margins
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        ViewGroup.MarginLayoutParams marginParams = new ViewGroup.MarginLayoutParams(params);
        marginParams.setMargins(0, 0, 0, 10);
        button.setLayoutParams(marginParams);

        // Set button text
        if (user == null) {
            // Button for chat with all users
            button.setText(getString(R.string.chat_with_all_users));
        } else if (FirebaseUtil.getUid().equals(user.getUid())) {
            // Button for chat with current user (himself)
            button.setText(getString(R.string.chat_with_my_self, user.getNickname()));
        } else {
            // Button for chat with specific user
            button.setText(user.getNickname());
        }

        // Disable button all caps
        button.setAllCaps(false);

        // set what happens when button is clicked
        button.setOnClickListener(view -> {
            Intent intent = new Intent(this, ChatroomActivity.class);
            if (user == null) {
                // Button for chat with all users
                intent.putExtra("receiverNickname", FirebaseUtil.EVERYONE);
                intent.putExtra("receiverUid", FirebaseUtil.EVERYONE);
                startActivity(intent);
            } else {
                // Button for chat with specific user
                intent.putExtra("receiverNickname", user.getNickname());
                intent.putExtra("receiverUid", user.getUid());
                startActivity(intent);
            }
        });
        return button;
    }

    void showMessage(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .show();
    }
}