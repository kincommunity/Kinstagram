package kin.com.kinstrgam;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedListActivity extends AppCompatActivity {

    private static final String TAG = "FeedListActivity";
    private static final int SELECT_PHOTO = 100;
    private static final String DATABASE_PATH = "All_Image_Uploads_Database";

    FirebaseStorage storage;
    DatabaseReference databaseReference;
    StorageReference storageReference, imageRef;

    // Creating RecyclerView.
    RecyclerView recyclerView;

    // Creating RecyclerView.Adapter.
    RecyclerView.Adapter adapter;

    // Creating Progress dialog
    ProgressDialog progressDialog;

    Uri selectedImage;
    UploadTask uploadTask;


    LinearLayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_list);

        storageReference = FirebaseStorage.getInstance().getReference();
        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(DATABASE_PATH);

        // Assign id to RecyclerView.
        recyclerView = (RecyclerView) findViewById(R.id.imageRecyclerView);

        // Setting RecyclerView size true.
        recyclerView.setHasFixedSize(true);

        // Setting RecyclerView layout as LinearLayout.
        mLayoutManager = new LinearLayoutManager(FeedListActivity.this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(mLayoutManager);

        // Assign activity this to progress dialog.
        progressDialog = new ProgressDialog(FeedListActivity.this);

        // Setting up message in Progress dialog.
        progressDialog.setMessage("Loading Images From Firebase.");

        // Showing progress dialog.
        progressDialog.show();


        // Adding Add Value Event Listener to databaseReference.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                
                List<FeedInfo> list = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {

                    FeedInfo feedInfo = postSnapshot.getValue(FeedInfo.class);
                    Log.d(TAG, "onDataChange: " + feedInfo.getName() + ", " + feedInfo.getImageUrl());
                    list.add(feedInfo);
                }

                adapter = new FeedAdapter(FeedListActivity.this, list);

                recyclerView.setAdapter(adapter);

                // Hiding the progress dialog.
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                // Hiding the progress dialog.
                progressDialog.dismiss();

            }
        });


        ImageView addImageButton = findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(v);
            }
        });
    }


    public void selectImage(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("kinstagram", "onActivityResult " + requestCode + " result: " + resultCode + " data: " + data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(FeedListActivity.this, "Image selected, ", Toast.LENGTH_SHORT).show();
                selectedImage = data.getData();
                uploadImage(null);

            }
        }

    }

    public void uploadImage(View view) {
        //create reference to images folder and assing a name to the file that will be uploaded
        imageRef = storageReference.child("images/" + selectedImage.getLastPathSegment());

        //creating and showing progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setMessage("Uploading...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        progressDialog.setCancelable(false);
        //starting upload
        uploadTask = imageRef.putFile(selectedImage);
        // Observe state change events such as progress, pause, and resume



        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                //sets and increments value of progressbar
                progressDialog.incrementProgressBy((int) progress);
            }
        });
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(FeedListActivity.this, "Error in uploading!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                Toast.makeText(FeedListActivity.this, "Upload successful", Toast.LENGTH_SHORT).show();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String currentDateTime = dateFormat.format(new Date());



                FeedInfo feedInfo = new FeedInfo("username", taskSnapshot.getDownloadUrl().toString(), currentDateTime);

                String ImageUploadId = databaseReference.push().getKey();

                databaseReference.child(ImageUploadId).setValue(feedInfo);


                progressDialog.dismiss();


            }
        });
    }
}
