package com.example.refresh.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refresh.AddShopItemActivity;
import com.example.refresh.R;
import com.example.refresh.ShoppingFragment;
import com.example.refresh.database.AppDatabase;
import com.example.refresh.database.model.ShopItem;

import java.util.List;

public class ShopItemListAdapter extends RecyclerView.Adapter<ShopItemListAdapter.ListViewHolder> {

    private Context context;
    private List<ShopItem> shopItemList;
    private LayoutInflater layoutInflater;
    private ShoppingFragment shoppingFragment;
    private AppDatabase db;

    public ShopItemListAdapter(Context context, List<ShopItem> shopItemList, ShoppingFragment shoppingFragment) {
        db = AppDatabase.getAppDatabase(context);
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.shopItemList = shopItemList;
        this.shoppingFragment = shoppingFragment;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.shop_item, parent, false);
        return new ListViewHolder(view, shoppingFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopItemListAdapter.ListViewHolder holder, int position) {
        final int itemPosition = position;
        final ShopItem shopItem = shopItemList.get(position);

        holder.shopNameTextView.setText(shopItem.getName());
        holder.quantityTextView.setText(String.valueOf(shopItem.getQuantity()));
        holder.msrpTextView.setText(String.valueOf(shopItem.getMsrp()));

        holder.crossButtonImageView.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Are you sure you want to remove " + shopItem.getName() + " from your shopping list?");
            alertDialogBuilder.setPositiveButton("Yes",
                    (arg0, arg1) -> deleteShopItem(itemPosition));

            alertDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        });

        holder.editButtonImageView.setOnClickListener(view ->
                shoppingFragment.startActivityForResult(new Intent(context, AddShopItemActivity.class).putExtra("shop_item", shopItemList.get(itemPosition)).putExtra("position", itemPosition), 100));

        if (itemPosition == shopItemList.size() - 1) {
            shoppingFragment.setTotalCost();
        }

    }

    private void deleteShopItem(int position) {
        ShopItem shopItem = shopItemList.get(position);
        String shopItemName = shopItem.getName();
        try {
            db.shopItemDAO().delete(shopItem);
            shopItemList.remove(position);
            notifyDataSetChanged();
            shoppingFragment.listVisibility();
            Toast.makeText(context, shopItemName + " has been removed.", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Log.e("Delete Shop", ex.getMessage() != null ? ex.getMessage() : "");
            Toast.makeText(context, "Error: Failed to remove " + shopItemName + ".", Toast.LENGTH_LONG).show();
        }
    }

    public class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView shopNameTextView;
        TextView quantityTextView;
        TextView msrpTextView;
        ImageView crossButtonImageView;
        ImageView editButtonImageView;
        OnShopItemListener onShopItemListener;

        ListViewHolder(View itemView, OnShopItemListener onShopItemListener) {
            super(itemView);
            this.onShopItemListener = onShopItemListener;
            itemView.setOnClickListener(this);
            shopNameTextView = itemView.findViewById(R.id.shopNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            msrpTextView = itemView.findViewById(R.id.msrpTextView);
            crossButtonImageView = itemView.findViewById(R.id.crossImageView);
            editButtonImageView = itemView.findViewById(R.id.editImageView);
        }

        @Override
        public void onClick(View view) {
            onShopItemListener.onShopItemClick(getAdapterPosition());
        }
    }

    public interface OnShopItemListener {
        void onShopItemClick(int pos);
    }

    @Override
    public int getItemCount() {
        return shopItemList.size();
    }
}

