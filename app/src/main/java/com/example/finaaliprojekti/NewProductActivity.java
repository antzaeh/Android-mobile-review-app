package com.example.finaaliprojekti;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NewProductActivity extends AppCompatActivity {
    public static final String TAG = "NewProduct";
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;
    private Context context;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private EditText editTextName, editTextPrice, editTextPlace, editTextComments, editTextLatitude, editTextLongitude;
    private Button buttonTakePicture, buttonSaveLocation, buttonSave, buttonSpeechToText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser user;
    private Uri productImageUri;
    private CollectionReference ProductReference;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    private ActivityResultLauncher<Intent> speechToTextLauncher;

    private static final int REQUEST_SPEECH_TO_TEXT = 2; // Define your request code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_product_activity);
        context = this;

        //FireBase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();
        ProductReference = mFirestore.collection("AK_PRODUCTS");

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);


        //UI
        editTextName = findViewById(R.id.editTextName);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextPlace = findViewById(R.id.editTextPlace);
        editTextComments = findViewById(R.id.editTextComments);
        buttonTakePicture = findViewById(R.id.buttonTakePicture);
        buttonSaveLocation = findViewById(R.id.buttonSaveLocation);
        buttonSave = findViewById(R.id.buttonSave);
        buttonSpeechToText = findViewById(R.id.buttonSpeechToText);


        buttonSpeechToText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });

        buttonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        buttonSaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveLocation();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
                finish();
            }
        });
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, ("paikka on muuttunut" + location.getLatitude() + ", " + location.getLongitude()));
                mLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        speechToTextLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ArrayList<String> resultText = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            if (resultText != null && !resultText.isEmpty()) {
                                editTextComments.setText(resultText.get(0));
                            }
                        }
                    }
                });
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String imageUriString = data.getStringExtra("savedUri");
                            if (imageUriString != null) {
                                // Successfully retrieved the image URI
                                productImageUri = Uri.parse(imageUriString);
                                Log.d(TAG, "Image URI: " + productImageUri.toString());
                            } else {
                                Log.d(TAG, "Image URI string is null");
                            }
                        } else {
                            Log.d(TAG, "Intent data is null");
                        }
                    }
                }
        );
    }
    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");

        speechToTextLauncher.launch(intent);
    }
    private void takePicture() {
        Intent takePictureIntent = new Intent(this, CameraActivity.class);
        takePictureLauncher.launch(takePictureIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SPEECH_TO_TEXT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && result.size() > 0) {
                String spokenText = result.get(0);
                editTextComments.setText(spokenText);
            }
        }
    }

    private void saveLocation() {
        //tarkistetaan lupa
        try {
            kysyLupaa(context);
            //Tässä kaksi eri tapaa paikannukseen
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
            //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
            if (mLocation != null) {
                //paikkaTextView.setText("latitude: " + mLocation.getLatitude() + ", longitude: " + mLocation.getLongitude());
                 //editTextLatitude.setText(String.valueOf(mLocation.getLatitude()));
                //editTextLongitude.setText(String.valueOf(mLocation.getLongitude()));
                double latitude = mLocation.getLatitude();
                double longitude = mLocation.getLongitude();
                Toast.makeText(NewProductActivity.this, "Coordinates set", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(NewProductActivity.this, "Searching.... Please try again", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Log.d("AK", "Virhe: Sovelluksella ei ollut oikeuksia lokaatioon");
        }
    }


    public boolean kysyLupaa(final Context context) {
        Log.d("AK", "kysyLupaa()");
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("AK", " Permission is not granted");
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Log.d("AK", "Kerran kysytty, mutta ei lupaa... Nyt ei kysytä uudestaan");

            } else {
                Log.d("AK", " Request the permission");
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
            return false;
        } else {

            Log.d("AK", "Permission has already been granted");
            return true;
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("AK ", "onRequestPermissionsResult()");

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("AK", "lupa tuli!");
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.d("AK", "Haetaan paikkaa tietyin väliajoin");
                        //Request location updates:
                        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,mLocationListener);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("AK", "Ei tullu lupaa!");
                }
                return;
            }

        }
    }


    private void saveProduct() {
        String name = editTextName.getText().toString();
        String price = editTextPrice.getText().toString();
        String place = editTextPlace.getText().toString();
        String comments = editTextComments.getText().toString();

        // Handle null values or set defaults
        if (TextUtils.isEmpty(name)) {
            name = "Unknown"; // Default to "Unknown" if name is empty
        }
        if (TextUtils.isEmpty(price)) {
            price = "0"; // Default to "0" if price is empty
        }
        if (TextUtils.isEmpty(place)) {
            place = "Unknown"; // Default to "Unknown" if place is empty
        }
        if (TextUtils.isEmpty(comments)) {
            comments = ""; // Default to empty string if comments are empty
        }

        // Convert URI to string for Firestore
        String productImageUriString;
        if (productImageUri != null) {
            productImageUriString = productImageUri.toString();
        } else {
            // If productImageUri is null, use a default image from drawable
            productImageUriString = getDefaultImageUriString(); // Implement this method
        }

        // Handle mLocation
        if (mLocation == null) {
            mLocation = new Location("default_provider");
            mLocation.setLatitude(1);
            mLocation.setLongitude(1);
        }
        Product product = new Product(name, place, price, productImageUriString, comments, mLocation.getLatitude(), mLocation.getLongitude());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String userId = user.getUid();
        //Add your own collection path
        CollectionReference productsRef = db.collection(" ").document(userId).collection("Products");

        productsRef.add(product)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(NewProductActivity.this, "Saving...", Toast.LENGTH_SHORT).show();

                    Log.d("NewProductActivity", "Product added with ID: " + documentReference.getId());
                   // clearFields();
                })
                .addOnFailureListener(e -> {
                    Log.e("NewProductActivity", "Error adding product", e);
                });
    }
    private String getDefaultImageUriString() {
        // Replace with logic to retrieve a default image URI from your app's drawable resources
        // For example, if you have a default image named "default_product_image" in drawable:
        // return "android.resource://your.package.name/" + R.drawable.default_product_image;
        return "android.resource://your.package.name/" + R.drawable.placeholder_image; // Return an appropriate default image URI string
    }
    private void clearFields() {
        // Clear input fields after successful save
        editTextName.setText("");
        editTextPrice.setText("");
        editTextPlace.setText("");
        editTextComments.setText("");
        editTextLatitude.setText("");
        editTextLongitude.setText("");
    }

}
