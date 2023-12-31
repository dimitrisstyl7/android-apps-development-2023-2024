package com.dimstyl.chatroom;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtil {
    // Initialize Firebase Authentication
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    // Initialize Firebase Realtime Database
    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();

    static final String USERS = "users";

    static final String EMAIL = "email";

    static final String NICKNAME = "nickname";

    static final String EVERYONE = "everyone";

    static FirebaseUser getUser() {
        return auth.getCurrentUser();
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
        auth.signInWithEmailAndPassword(
                email,
                password
        ).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!isSignedIn() || getNickname() == null) {
                            activity.showMessage("Oops...", "Something went wrong, please sign in again.");
                            signOut();
                            return;
                        }
                        activity.startAvailableUsersActivity();
                    } else {
                        activity.showMessage("Error", "Please check your credentials.");
                    }
                }
        );
    }

    static void signUp(String email, String password, String nickname, MainActivity activity) {
        auth.createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(authResult -> {
                            // Set authenticated user's nickname
                            setUserNickname(nickname);

                            // Add user's uid, email and nickname to database (for future use - available users activity)
                            addUserToDatabase(email, nickname);

                            if (!isSignedIn()) {
                                // If for a reason user is not signed in after successful sign up, show success sign up message and return.
                                activity.showMessage("Success", "User profile created successfully! You can now sign in.");
                                return;
                            }

                            while (getNickname() == null) {
                                // Wait for nickname to be updated
                            }

                            activity.showMessage("Success", "User profile created successfully!",
                                    "Go to chatroom", (dialog, which) -> {
                                        activity.startAvailableUsersActivity();
                                    },
                                    "Close", (dialog, which) -> signOut());
                        }
                )
                .addOnFailureListener(e -> {
                            activity.showMessage("Error", "Please check your credentials.\n\nWarning:\n\t> Password must be at least 6 characters long!\n\t> Email must not be already in use!");
                        }
                );
    }

    static void signOut() {
        auth.signOut();
    }

    private static void setUserNickname(String nickname) {
        getUser().updateProfile(
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(nickname)
                        .build()
        );
    }

    private static void addUserToDatabase(String email, String nickname) {
        DatabaseReference reference = database.getReference().child("users").child(getUid());
        Map<String, String> userData = new HashMap<>();
        userData.put(EMAIL, email);
        userData.put(NICKNAME, nickname);
        reference.setValue(userData);
    }

    static void getAllUsers(AvailableUsersActivity activity) {
        DatabaseReference reference = database.getReference().child(USERS);
        reference.get().addOnSuccessListener(dataSnapshot ->
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
                            User user = new User(uid, email, nickname);
                            activity.addUserToLinearLayout(user);
                        }))
                .addOnFailureListener(e -> {
                    activity.showMessage("Error", "Something went wrong, please try again.");
                    activity.signOut();
                });
    }
}
