package kin.com.kinstagram;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;

import kin.com.kinstagram.R;

import kin.com.kinstagram.model.User;
import kin.com.kinstagram.model.Wallet;
import kin.sdk.core.KinAccount;
import kin.sdk.core.KinClient;
import kin.sdk.core.ServiceProvider;
import com.android.volley.RequestQueue;
import com.android.volley.Request;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 123;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(getApplicationContext());
    // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

    // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setLogo(R.drawable.kinstagram)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"onActivityResult " + requestCode + " result: " + resultCode + " data: "+  data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == ResultCodes.OK) {
                // Successfully signed in
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(getApplicationContext(), "Registration Completed: " + user.getDisplayName(), Toast.LENGTH_LONG).show();
                ServiceProvider serviceProvider =  new ServiceProvider("http://parity.rounds.video:8545", ServiceProvider.NETWORK_ID_ROPSTEN);
                try{
                    KinClient kinClient = new KinClient(getApplicationContext(), serviceProvider);
                    KinAccount kinAccount = kinClient.createAccount(Wallet.PASSPHRASE);
                   // getKin(kinAccount);
                    Log.d(TAG, "onActivityResult: public address" +kinAccount.getPublicAddress());
                    final Wallet wallet = new Wallet(kinAccount.getPublicAddress());
                    final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(databaseReference == null){
                                databaseReference.child("Users").child(user.getUid()).setValue(new User(user.getDisplayName()));
                                databaseReference.child("Wallets").child(user.getUid()).setValue(wallet);
                            }
                            Intent intent = new Intent(MainActivity.this, FeedListActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                }catch (Exception e){

                }


            } else {
                // Sign in failed, check response for error code
                // ...
            }
        }
    }
}
