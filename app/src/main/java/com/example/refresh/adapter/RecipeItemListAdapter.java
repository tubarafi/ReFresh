package com.example.refresh.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.refresh.R;
import com.example.refresh.RecipeFragment;
import com.example.refresh.database.model.RecipeItem;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeItemListAdapter extends RecyclerView.Adapter<RecipeItemListAdapter.ListViewHolder> {

    private List<RecipeItem> recipeItemList;
    private LayoutInflater layoutInflater;
    private RecipeFragment recipeFragment;
    private Boolean isSaved = false;

    public RecipeItemListAdapter(Context context, List<RecipeItem> recipeItemList, RecipeFragment recipeFragment) {
        layoutInflater = LayoutInflater.from(context);
        this.recipeItemList = recipeItemList;
        this.recipeFragment = recipeFragment;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = layoutInflater.inflate(R.layout.recipe_item, parent, false);
        return new ListViewHolder(view, recipeFragment);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeItemListAdapter.ListViewHolder holder, int position) {
        final int itemPosition = position;
        final RecipeItem recipeItem = recipeItemList.get(position);

        holder.recipeNameTextView.setText(recipeItem.getName());
        if (recipeItem.getMissedIngredients() != "") {
            holder.missingIngredientsList.setText(recipeItem.getMissedIngredients());
        } else {
            holder.missingIngredientsList.setText("none");
        }
        Picasso.get()
                .load(recipeItem.getImageURL())
                .resize(90,90)
                .centerCrop()
                .into(holder.recipeImageView);
        holder.openInBrowserButton.setOnClickListener(view -> {
            holder.onRecipeItemListener.onOpenInBrowserClick(recipeItemList.get(itemPosition));
        });
        if (!isSaved) {
            holder.deleteRecipeButton.setVisibility(View.GONE);
            holder.saveRecipeButton.setOnClickListener(view -> {
                holder.onRecipeItemListener.onSaveRecipeClick(recipeItemList.get(itemPosition));
            });
        } else {
            holder.saveRecipeButton.setVisibility(View.GONE);
            holder.deleteRecipeButton.setOnClickListener(view -> {
                holder.onRecipeItemListener.onDeleteRecipeClick(recipeItemList.get(itemPosition));
                recipeItemList.remove(position);
                notifyDataSetChanged();
            });
        }
    }

    public class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView recipeNameTextView;
        ImageView recipeImageView;
        Button openInBrowserButton;
        Button saveRecipeButton;
        Button deleteRecipeButton;
        TextView missingIngredientsList;
        private OnRecipeItemListener onRecipeItemListener;

        public ListViewHolder(View itemView, OnRecipeItemListener onRecipeItemListener) {
            super(itemView);
            this.onRecipeItemListener = onRecipeItemListener;
            itemView.setOnClickListener(this);
            recipeNameTextView = itemView.findViewById(R.id.recipeNameTextView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
            openInBrowserButton = itemView.findViewById(R.id.openBrowserButton);
            saveRecipeButton = itemView.findViewById(R.id.saveRecipeButton);
            deleteRecipeButton = itemView.findViewById(R.id.deleteRecipeButton);
            missingIngredientsList = itemView.findViewById(R.id.missingIngredients);
        }

        @Override
        public void onClick(View view) {
        }
    }

    public void setIsSaved() {
        isSaved = true;
    }

    public interface OnRecipeItemListener {
        void onOpenInBrowserClick(RecipeItem recipeItem);
        void onSaveRecipeClick(RecipeItem recipeItem);
        void onDeleteRecipeClick(RecipeItem recipeItem);
    }

    @Override
    public int getItemCount() {
        return recipeItemList.size();
    }

}
