package com.dimstyl.chatroom;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtil {
    // Initialize Firebase Authentication
    private static final FirebaseAuth AUTH = FirebaseAuth.getInstance();

    // Initialize Firebase Realtime Database
    private static final FirebaseDatabase DATABASE = FirebaseDatabase.getInstance();

    static final String USERS = "users";
    static final String EMAIL = "email";
    static final String NICKNAME = "nickname";
    static final String EVERYONE = "everyone";
    static final String MESSAGES = "messages";

    static FirebaseUser getUser() {
        return AUTH.getCurrentUser();
    }

    static String getUid() {
        return getUser().getUid();
    }

    static String getNickname() {
        return getUser().getDisplayName();
    }

    static String getEmail() {
        return getUser().getEmail();
    }

    static boolean isSignedIn() {
        return getUser() != null;
    }

    static void signIn(String email, String password, MainActivity activity) {
        AUTH.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(authResult -> {
                    if (getNickname() == null) {
                        Log.e("FirebaseUtil", "signIn: getNickname() == null");
                        activity.showMessage("Oops...", "Something went wrong, please sign in again.");
                        signOut();
                        return;
                    }
                    activity.startAvailableUsersActivity();
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseUtil", "signIn: " + e.getMessage());
                    activity.showMessage("Error", "Please check your credentials.");
                });
    }

    static void signUp(String email, String password, String nickname, MainActivity activity) {
        AUTH.createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(authResult -> {
                            // Add user's uid, email and nickname to database (for future use - available users activity)
                            addUserToDatabase(email, nickname);

                            // Set authenticated user's nickname
                            setUserNickname(nickname, activity);
                        }
                )
                .addOnFailureListener(e -> {
                            Log.e("FirebaseUtil", "signUp: " + e.getMessage());
                            activity.showMessage("Error", "Please check your credentials.\n\nWarning:\n\t> Password must be at least 6 characters long.\n\t> Email must not be already in use.");
                        }
                );
    }

    static void signOut() {
        AUTH.signOut();
    }

    private static void setUserNickname(String nickname, MainActivity activity) {
        getUser().updateProfile(
                        new UserProfileChangeRequest.Builder()
                                .setDisplayName(nickname)
                                .build()
                )
                .addOnSuccessListener(aVoid -> {
                    if (!isSignedIn()) {
                        // If for a reason user is not signed in after successful sign up, show success sign up message and return.
                        activity.showMessage("Success", "User profile created successfully! You can now sign in.");
                        return;
                    }
                    activity.showMessage(
                            "Success",
                            "User profile created successfully!",
                            "Go to chatroom",
                            (dialog, which) -> activity.startAvailableUsersActivity(),
                            "Close",
                            (dialog, which) -> signOut()
                    );
                })
                .addOnFailureListener(e -> Log.e("FirebaseUtil", "setUserNickname: " + e.getMessage()));
    }

    private static void addUserToDatabase(String email, String nickname) {
        DatabaseReference reference = DATABASE.getReference().child(USERS).child(getUid());
        Map<String, String> userData = new HashMap<>();
        userData.put(EMAIL, email);
        userData.put(NICKNAME, nickname);
        reference.setValue(userData)
                .addOnFailureListener(e -> Log.e("FirebaseUtil", "addUserToDatabase: " + e.getMessage()));
    }

    static void getAllUsers(AvailableUsersActivity activity) {
        DatabaseReference reference = DATABASE.getReference().child(USERS);
        reference.get()
                .addOnSuccessListener(dataSnapshot ->
                        dataSnapshot.getChildren().forEach(child -> {
                                    String uid = child.getKey();
                                    Object emailObj = child.child(EMAIL).getValue();
                                    Object nicknameObj = child.child(NICKNAME).getValue();

                                    if (uid == null || emailObj == null || nicknameObj == null) {
                                        // Skip user with missing data
                                        return;
                                    }

                                    if (getUid().equals(uid)) {
                                        // Skip current user
                                        return;
                                    }

                                    // Add user to LinearLayout
                                    String email = emailObj.toString();
                                    String nickname = nicknameObj.toString();
                                    activity.addUserToLinearLayout(new User(uid, email, nickname));
                                }
                        )
                )
                .addOnFailureListener(e -> {
                            Log.e("FirebaseUtil", "getAllUsers: " + e.getMessage());
                            activity.showMessage("Oops...", "Something went wrong, please try again.");
                            activity.signOut();
                        }
                );
    }

    static void addChildEventListener(String receiverUid, ChatroomActivity activity) {
        String conversationId = getConversationId(receiverUid);
        DatabaseReference reference = DATABASE.getReference().child(MESSAGES).child(conversationId);

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Message message = snapshot.getValue(Message.class);

                if (message == null) {
                    // @TODO: if null, show error message
                    return;
                }

                activity.addMessageToLinearLayout(message);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                activity.showMessage("Oops...", "Something went wrong, please try again.");
            }
        });
    }

    static void saveMessage(String receiverUid, String messageText, ChatroomActivity activity) {
        DatabaseReference reference = DATABASE.getReference().child(MESSAGES);
        String conversationId = getConversationId(receiverUid);
        String senderUid = getUid();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String messageId = reference.child(conversationId).push().getKey();

        if (messageId == null) {
            activity.showMessage("Oops...", "Something went wrong, please try again.");
            return;
        }

        if (receiverUid.equals(EVERYONE)) {
            // Chatting with everyone
            String senderNickname = getNickname();
            reference.child(conversationId).child(messageId)
                    .setValue(new Message(senderUid, receiverUid, senderNickname, messageText, timestamp));
        } else {
            // Chatting with a specific user
            reference.child(conversationId).child(messageId)
                    .setValue(new Message(senderUid, receiverUid, messageText, timestamp));
        }
    }

    private static String getConversationId(String receiverUid) {
        return receiverUid.equals(EVERYONE) ?
                EVERYONE : getUid().compareTo(receiverUid) < 0 ? getUid() + receiverUid : receiverUid + getUid();
    }

    static boolean isSenderCurrentUser(String senderUid) {
        return getUid().equals(senderUid);
    }
}
