package com.example.refresh.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import com.example.refresh.AddFoodItemActivity;
import com.example.refresh.HomeFragment;
import com.example.refresh.R;
import com.example.refresh.database.AppDatabase;
import com.example.refresh.database.model.FoodItem;

public class FoodItemListAdapter extends RecyclerView.Adapter<FoodItemListAdapter.ListViewHolder> {

    private Context context;
    private List<FoodItem> foodItemList;
    private LayoutInflater layoutInflater;
    private HomeFragment homeFragment;
    private AppDatabase db;
    private boolean showCheckbox;
    public ArrayList<FoodItem> selectedIngredientList = new ArrayList<>();

    public FoodItemListAdapter(Context context, List<FoodItem> foodItemList, HomeFragment homeFragment) {
        db = AppDatabase.getAppDatabase(context);
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.foodItemList = foodItemList;
        this.homeFragment = homeFragment;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.food_item, parent, false);
        return new ListViewHolder(view, homeFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemListAdapter.ListViewHolder holder, int position) {
        final int itemPosition = position;
        final FoodItem foodItem = foodItemList.get(position);

        holder.foodNameTextView.setText(foodItem.getName());
        holder.quantityTextView.setText(String.valueOf(foodItem.getQuantity()));
        if (foodItem.getRemindMeOnDate() == null || foodItem.getRemindMeOnDate().equals("")) {
            holder.remindDateTitleTextView.setVisibility(View.GONE);
        } else {
            holder.remindDateTitleTextView.setVisibility(View.VISIBLE);
            holder.remindDateTextView.setText(foodItem.getRemindMeOnDate());
        }

        holder.crossButtonImageView.setOnClickListener(view -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Are you sure you want to remove " + foodItem.getName() + " from your fridge?");
            alertDialogBuilder.setPositiveButton("Yes",
                    (arg0, arg1) -> deleteFoodItem(itemPosition));

            alertDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        });


        holder.editButtonImageView.setOnClickListener(view -> homeFragment.startActivityForResult(new Intent(context, AddFoodItemActivity.class).putExtra("food_item", foodItemList.get(itemPosition)).putExtra("position", itemPosition), 100));

        holder.selectIngredientBox.setOnClickListener(view -> {
                if (holder.selectIngredientBox.isChecked()) {
                    selectedIngredientList.add(foodItemList.get(itemPosition));
                } else {
                    selectedIngredientList.remove(foodItemList.get(itemPosition));
                }
        });

        if (this.showCheckbox) {
            holder.editButtonImageView.setVisibility(View.GONE);
            holder.crossButtonImageView.setVisibility(View.GONE);
            holder.selectIngredientBox.setVisibility(View.VISIBLE);
        } else {
            holder.editButtonImageView.setVisibility(View.VISIBLE);
            holder.crossButtonImageView.setVisibility(View.VISIBLE);
            holder.selectIngredientBox.setVisibility(View.GONE);
            holder.selectIngredientBox.setChecked(false);
        }
    }

    private void deleteFoodItem(int position) {
        FoodItem foodItem = foodItemList.get(position);
        String foodName = foodItem.getName();
        try {
            db.foodItemDAO().delete(foodItem);
            foodItemList.remove(position);
            homeFragment.listVisibility();
            notifyDataSetChanged();
            Toast.makeText(context, foodName + " has been removed.", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Log.e("Delete Food", ex.getMessage() != null ? ex.getMessage() : "");
            Toast.makeText(context, "Error: Failed to remove " + foodName + ".", Toast.LENGTH_LONG).show();
        }
    }

    public void toggleSelectIngredients(boolean isActive) {
        if (isActive)
            this.showCheckbox = true;
        else
            this.showCheckbox = false;
        this.notifyDataSetChanged();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView foodNameTextView;
        TextView quantityTextView;
        TextView remindDateTitleTextView;
        TextView remindDateTextView;
        ImageView crossButtonImageView;
        ImageView editButtonImageView;
        CheckBox selectIngredientBox;
        private OnFoodItemListener onFoodItemListener;

        ListViewHolder(View itemView, OnFoodItemListener onFoodItemListener) {
            super(itemView);
            this.onFoodItemListener = onFoodItemListener;
            itemView.setOnClickListener(this);
            foodNameTextView = itemView.findViewById(R.id.recipeNameTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            remindDateTitleTextView = itemView.findViewById(R.id.remindDateTitle);
            remindDateTextView = itemView.findViewById(R.id.remindDateTextView);
            crossButtonImageView = itemView.findViewById(R.id.crossImageView);
            editButtonImageView = itemView.findViewById(R.id.editImageView);
            selectIngredientBox = itemView.findViewById(R.id.selectIngredientBox);
        }

        @Override
        public void onClick(View view) {
            onFoodItemListener.onFoodItemClick(getAdapterPosition());
        }
    }

    public interface OnFoodItemListener {
        void onFoodItemClick(int pos);
    }

    @Override
    public int getItemCount() {
        return foodItemList.size();
    }
}

