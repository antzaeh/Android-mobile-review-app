package com.example.finaaliprojekti;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private double latitude;
    private double longitude;
    private String productId;
    private TextToSpeech textToSpeech;
    private TextView textViewComments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        Log.d("ProductDetailsActivity", "onCreate() called");
        // Initialize views
        TextView textViewName = findViewById(R.id.textViewName);
        TextView textViewPlace = findViewById(R.id.textViewPlace);
        TextView textViewPrice = findViewById(R.id.textViewPrice);
        ImageView imageView = findViewById(R.id.imageView);
        textViewComments = findViewById(R.id.textViewComments);
        Button buttonDelete = findViewById(R.id.buttonDelete);
        Button buttonShowOnMap = findViewById(R.id.buttonShowOnMap);
        Button buttonTXT = findViewById(R.id.buttonTextToSpeech);

        // Initialize TextToSpeech
        textToSpeech = new TextToSpeech(this, this);

        // Get product details from intent extras
        Intent intent = getIntent();
        if (intent != null) {
            productId = intent.getStringExtra("productId");
            String productName = intent.getStringExtra("productName");
            String place = intent.getStringExtra("place");
            String price = intent.getStringExtra("price");
            String comments = intent.getStringExtra("comments");
            String imageUri = intent.getStringExtra("image");
            latitude = intent.getDoubleExtra("latitude", 0.0);
            longitude = intent.getDoubleExtra("longitude", 0.0);
            Log.d("ProductDetailsActivity", "Product ID: " + productId);
            // Set data to views
            textViewName.setText("Name: " + productName);
            textViewPlace.setText("Place: " + place);
            textViewPrice.setText("Price: " + price);
            textViewComments.setText("Comments: " + comments);
            Glide.with(this)
                    .load(Uri.parse(imageUri)) // Parse the URI string to URI and load with Glide
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(imageView);

            // Delete button click listener
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ProductDetailsActivity", "Delete button clicked");
                    deleteProduct();
                }
            });

            // Show on map button click listener
            buttonShowOnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create an intent to start MapActivity
                    Intent mapIntent = new Intent(ProductDetailsActivity.this, MapActivity.class);
                    // Pass the latitude and longitude values
                    mapIntent.putExtra("latitude", latitude);
                    mapIntent.putExtra("longitude", longitude);
                    startActivity(mapIntent);
                }
            });
        }
        ;
        buttonTXT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakComments();
            }
        });
    }

    private void deleteProduct() {
        // Build the AlertDialog for confirmation
        new AlertDialog.Builder(ProductDetailsActivity.this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this product?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // User confirmed, proceed with deletion
                        performDelete();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void performDelete() {
        // Actual deletion logic
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference productRef = db.collection("AK_PRODUCTS")
                .document(userId)
                .collection("Products")
                .document(productId);

        productRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProductDetailsActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after deletion
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProductDetailsActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                    Log.e("ProductDetailsActivity", "Error deleting product", e);
                });
    }

    @Override
    protected void onDestroy() {
        // Shutdown TextToSpeech engine
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TextToSpeech", "Language not supported");
            }
        } else {
            Log.e("TextToSpeech", "Initialization failed");
        }
    }

    private void speakComments() {
        String commentsPrefix = "Comments: ";
        String comments = textViewComments.getText().toString();

        // Check if the comments start with the prefix; if so, remove it
        if (comments.startsWith(commentsPrefix)) {
            comments = comments.substring(commentsPrefix.length());
        }

        // Trim any leading or trailing whitespace
        comments = comments.trim();

        // Speak the comments using TextToSpeech
        textToSpeech.speak(comments, TextToSpeech.QUEUE_FLUSH, null, null);
    }
}
