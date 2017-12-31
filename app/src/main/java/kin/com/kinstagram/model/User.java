package kin.com.kinstagram.model;

/**
 * Created by yohaybarski on 13/12/2017.
 */

public class User {
    public String name;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name) {
        this.name = name;
    }
}
