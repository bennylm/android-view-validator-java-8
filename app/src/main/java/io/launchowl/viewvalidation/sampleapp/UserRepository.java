package io.launchowl.viewvalidation.sampleapp;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A mock user repository.
 */
public class UserRepository {

    // A collection of existing usernames.
    private final Set<User> users = new HashSet<>(Arrays.asList(
       new User("realkiwi"), new User("happyorange"), new User("iceapple"), new User("coolblueberry")
    ));


    /**
     * Retrieves a user.
     *
     * @param userName username
     * @param onUuserRetrievedListener an {@link OnuserRetrievedListener}
     */
    void getUser(final String userName, final OnuserRetrievedListener onUuserRetrievedListener) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(300, 1500));
            onUuserRetrievedListener.onUserRetrieved(findUser(userName));
        } catch (InterruptedException e ) {
            e.printStackTrace();
        }

    }

    /**
     * Locates the user by looking for the provided username in the username collection.
     *
     * @param userName a username
     * @return a {@link User}
     */
    private User findUser(String userName) {
        for (User user : users) {
            if (user.userName().equals(userName)) {
                return user;
            }
        }

        return null;
    }

    /**
     * An interface for responding to a user retrieval request.
     */
    interface OnuserRetrievedListener {
        void onUserRetrieved(User user);
    }
}
