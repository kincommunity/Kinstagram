package kin.com.kinstagram.model;

/**
 * Created by yohaybarski on 13/12/2017.
 */

public class Wallet {
    public String publicAddress;
    public final static String PASSPHRASE = "blabla";

    public Wallet() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Wallet(String publicAddress) {
        this.publicAddress = publicAddress;
    }
}
