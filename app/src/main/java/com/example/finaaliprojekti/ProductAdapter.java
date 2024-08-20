package com.example.finaaliprojekti;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.textViewProductName.setText(product.getName());
        holder.textViewProductPlace.setText( product.getPlace());
        holder.textViewProductPrice.setText(product.getPrice());
        Log.d("ProductAdapter", "onBindViewHolder: " + product.getName() + " " + product.getPlace() + " " + product.getPrice());
        // Load image using a library like Glide or Picasso
        Glide.with(holder.itemView.getContext())
                .load(Uri.parse(product.getImage()))
                .placeholder(R.drawable.ic_launcher_background) // Add a placeholder image
                .into(holder.imageViewProductImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open ProductDetailsActivity
                // Start ProductDetailsActivity with product details
                Intent intent = new Intent(context, ProductDetailsActivity.class);
                intent.putExtra("productId", product.getId());
                Log.d("onBindViewHolder", "onClick: " + product.getId());
                intent.putExtra("productName", product.getName());
                intent.putExtra("place", product.getPlace());
                intent.putExtra("price", product.getPrice());
                intent.putExtra("comments", product.getComments());
                intent.putExtra("image", product.getImage());
                intent.putExtra("latitude", product.getLatitude());
                intent.putExtra("longitude", product.getLongitude());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void setProducts(List<Product> products) {
        this.productList = products; // Update the list of products
        notifyDataSetChanged(); // Notify adapter about the data change
    }
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewProductName, textViewProductPlace, textViewProductPrice;
        public ImageView imageViewProductImage;

        public ProductViewHolder(View view) {
            super(view);
            textViewProductName = view.findViewById(R.id.textViewProductName);
            textViewProductPlace = view.findViewById(R.id.textViewProductPlace);
            textViewProductPrice = view.findViewById(R.id.textViewProductPrice);
            imageViewProductImage = view.findViewById(R.id.imageViewProductImage);
        }
    }
}
