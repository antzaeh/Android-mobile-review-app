package com.example.finaaliprojekti;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private FirebaseUser user;
    private List<Product> productList;
    private ProductAdapter productAdapter;
    private Context context;
    private EditText editTextSearch;
    private Spinner spinnerSort;
    private Button logOut,buttonSensorWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Retrieve user details from the Intent
        String userId = getIntent().getStringExtra("USER_ID");
        String userName = getIntent().getStringExtra("USER_NAME");
        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        // Log or use the user details
        Log.d(TAG, "User ID: " + userId);
        Log.d(TAG, "User Name: " + userName);
        Log.d(TAG, "User Email: " + userEmail);


        context = this;
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        // Check if user is logged in
        user = mAuth.getCurrentUser();
        // Initialize RecyclerView and its adapter
        RecyclerView recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(context, productList);
        recyclerViewProducts.setAdapter(productAdapter);

        // Fetch products from Firestore
        //retrieveProductsFromFirestore();
        editTextSearch = findViewById(R.id.editTextSearch);
        spinnerSort = findViewById(R.id.spinnerSort);

        // Setup FloatingActionButton click listener
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start NewProductActivity to add new product
                Intent newProductIntent = new Intent(MainActivity.this, NewProductActivity.class);
                startActivity(newProductIntent);
            }
        });
        buttonSensorWeather = findViewById(R.id.btnSensorWeather);
        buttonSensorWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SensorActivity.class);
                startActivity(intent);
            }
        });
        logOut = findViewById(R.id.btnLogout);
        logOut.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {
                                          logOut();
                                      }
                                  });

        editTextSearch = findViewById(R.id.editTextSearch);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used, but required by interface
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Called when the text is changed
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used, but required by interface
            }
        });

        // Setup sorting spinner
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.sort_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerAdapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                sortProducts(selectedOption);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    private void performSearch(String query) {
        // Implement search logic here
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.setProducts(filteredList); // Update adapter with filtered list
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the product list when activity is resumed
        retrieveProductsFromFirestore();
    }

    private void retrieveProductsFromFirestore() {
        if (user == null) {
            return; // Return if user is not logged in
        }

        String userId = user.getUid();
        //Add your own collection path
        CollectionReference productsRef = mFirestore.collection(" ").document(userId).collection("Products");
        productList.clear();
        productsRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Product product = documentSnapshot.toObject(Product.class);
                        product.setId(documentSnapshot.getId()); // Set the ID from Firestore document
                        productList.add(product);
                        Log.d(TAG, "Product retrieved: " + product.getName() + ", " + product.getPrice());
                    }
                    productAdapter.notifyDataSetChanged(); // Notify adapter about data changes
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving products", e);
                    Toast.makeText(MainActivity.this, "Failed to retrieve products", Toast.LENGTH_SHORT).show();
                });
    }

    private void sortProducts(String sortBy) {

        switch (sortBy) {
            case "Name (A-Z)":
                productList.sort(Comparator.comparing(Product::getName));
                break;
            case "Name (Z-A)":
                productList.sort((p1, p2) -> p2.getName().compareTo(p1.getName()));
                break;
            case "Price (Low to High)":
                productList.sort(Comparator.comparingDouble(p -> Double.parseDouble(p.getPrice())));
                break;
            case "Price (High to Low)":
                productList.sort((p1, p2) -> Double.compare(Double.parseDouble(p2.getPrice()), Double.parseDouble(p1.getPrice())));
                break;
            default:
                productList.sort(Comparator.comparing(Product::getName));
                break;
        }
        productAdapter.notifyDataSetChanged();
    }

    public void logOut()
    {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
