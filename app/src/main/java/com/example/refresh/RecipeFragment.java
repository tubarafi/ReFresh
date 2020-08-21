package com.example.refresh;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.refresh.adapter.RecipeItemListAdapter;
import com.example.refresh.database.AppDatabase;
import com.example.refresh.database.model.FoodItem;
import com.example.refresh.database.model.RecipeItem;
import com.example.refresh.util.fragmentCallbackListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class RecipeFragment extends Fragment implements RecipeItemListAdapter.OnRecipeItemListener {

    private List<RecipeItem> recipeItemList = new ArrayList<>();
    private TextView recipeItemListEmptyTextView;
    private TextView totalText;
    private TextView totalCostText;
    private RecyclerView recyclerView;
    private RecipeItemListAdapter recipeItemListAdapter;
    private List<FoodItem> selectedIngredients;
    private String loadLocation;
    private FloatingActionButton backButton;
    private AppDatabase db;
    private fragmentCallbackListener callbackListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipes, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Generated Recipes");
        db = AppDatabase.getAppDatabase(getContext());
        recipeItemListEmptyTextView = rootView.findViewById(R.id.emptyRecipeListTextView);
        totalText = rootView.findViewById(R.id.totalText);
        totalCostText = rootView.findViewById(R.id.totalCostText);
        totalText.setVisibility(View.GONE);
        totalCostText.setVisibility(View.GONE);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recipeItemListAdapter = new RecipeItemListAdapter(getActivity(), recipeItemList, this);
        recyclerView.setAdapter(recipeItemListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        backButton = rootView.findViewById(R.id.fab_goBack);
        loadLocation = getArguments().getString("loadLocation");
        if (loadLocation == "homeFrag") {
            selectedIngredients = (List<FoodItem>) getArguments().getSerializable("ingredientList");
            GetRecipesOnline(new WeakReference<>(this));
        } else {
            new RetrieveRecipeDB(this).execute();
            recipeItemListAdapter.setIsSaved();
            backButton.hide();
        }
        backButton.setOnClickListener(view -> {
                if (callbackListener != null) {
                    callbackListener.onCallback("home", null);
                }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof fragmentCallbackListener)
            callbackListener = (fragmentCallbackListener) getActivity();
    }

    private static class RetrieveRecipeDB extends AsyncTask<Void, Void, List<RecipeItem>> {

        private WeakReference<RecipeFragment> recipeFragmentWeakReference;

        // only retain a weak reference to the activity
        RetrieveRecipeDB(RecipeFragment context) {
            recipeFragmentWeakReference = new WeakReference<>(context);
        }

        @Override
        protected List<RecipeItem> doInBackground(Void... voids) {
            if (recipeFragmentWeakReference.get() != null)
                return recipeFragmentWeakReference.get().db.recipeItemDAO().getAll();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<RecipeItem> items) {
            if (items != null && items.size() > 0) {
                recipeFragmentWeakReference.get().recipeItemList.clear();
                recipeFragmentWeakReference.get().recipeItemList.addAll(items);
                // hides empty text view
                recipeFragmentWeakReference.get().recipeItemListEmptyTextView.setVisibility(View.GONE);
                recipeFragmentWeakReference.get().recipeItemListAdapter.notifyDataSetChanged();
            }
            recipeFragmentWeakReference.get().totalText.setVisibility(View.GONE);
            recipeFragmentWeakReference.get().totalCostText.setVisibility(View.GONE);
        }
    }

    private String getIngredientParams () {
        String ingredientParams = "";
        if (selectedIngredients != null) {
            for (int i = 0; i < selectedIngredients.size(); i++) {
                if (i > 0) {
                    ingredientParams = ingredientParams.concat(", ");
                }
                ingredientParams = ingredientParams.concat(selectedIngredients.get(i).getName());
            }
        }
        return ingredientParams;
    }

    public void GetRecipesOnline(final WeakReference<RecipeFragment> fragmentReference) {
        Log.d("ingredients", getIngredientParams());
        if (getIngredientParams().equals("")) {
            return;
        }
        RequestQueue queue = Volley.newRequestQueue(fragmentReference.get().getContext());
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.spoonacular.com")
                .appendPath("recipes")
                .appendPath("findByIngredients")
                .appendQueryParameter("apiKey", "c35d2984cb0a4f4c80e2bb7dbf9aae4a")
                .appendQueryParameter("ingredients", getIngredientParams())
                .appendQueryParameter("number", "15");
        String url = builder.build().toString();

        Response.Listener<String> lol = response -> {
            try {
                JSONArray recipes = new JSONArray(response);
                for (int i = 0; i < recipes.length(); i++) {
                    JSONObject recipeDetails = recipes.getJSONObject(i);
                    RecipeItem recipeItem = new RecipeItem(recipeDetails.getString("title"), recipeDetails.getString("id"), recipeDetails.getString("image"));
                    JSONArray missingIngredients = recipeDetails.getJSONArray("missedIngredients");
                    String missedIngredients = "";
                    if (missingIngredients != null) {
                        for (int j = 0; j < missingIngredients.length(); j++) {
                            if (j > 0) {
                                missedIngredients = missedIngredients.concat(", ");
                            }
                            missedIngredients = missedIngredients.concat(missingIngredients.getJSONObject(j).getString("name"));
                        }
                    }
                    recipeItem.setMissedIngredients(missedIngredients);
                    fragmentReference.get().recipeItemList.add(recipeItem);
                }
                if (fragmentReference.get().recipeItemList.size() > 0) {
                    fragmentReference.get().recipeItemListEmptyTextView.setVisibility(View.GONE);
                    fragmentReference.get().recipeItemListAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                Log.d("GetRecipesOnline Error", e.getMessage());
            }
        };

        Response.ErrorListener whoops = error -> Log.d("help", error.toString());

        StringRequest stringy = new StringRequest(Request.Method.GET, url, lol, whoops);

        queue.add(stringy);
    }

    public void NavigateToRecipeURL(RecipeItem recipeItem) {
        RequestQueue queue = Volley.newRequestQueue(this.getContext());
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("api.spoonacular.com")
                .appendPath("recipes")
                .appendPath(recipeItem.getSourceId())
                .appendPath("information")
                .appendQueryParameter("apiKey", "c35d2984cb0a4f4c80e2bb7dbf9aae4a");
        String url = builder.build().toString();

        Response.Listener<String> lol = response -> {
            try {
                JSONObject recipeInfo = new JSONObject(response);
                String sourceURL = recipeInfo.getString("sourceUrl");
                recipeItem.setSourceURL(sourceURL);
                Uri uri = Uri.parse(sourceURL); // missing 'http://' will cause crashed
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } catch (JSONException e) {
                Log.d("NavigateToRecipeURL Error", e.getMessage());
            }
        };

        Response.ErrorListener whoops = error -> Log.d("help", error.toString());

        StringRequest stringy = new StringRequest(Request.Method.GET, url, lol, whoops);

        queue.add(stringy);
    }

    @Override
    public void onOpenInBrowserClick(RecipeItem recipeItem) {
        this.NavigateToRecipeURL(recipeItem);
    }

    @Override
    public void onDeleteRecipeClick(RecipeItem recipeItem) {
        try {
            db.recipeItemDAO().delete(recipeItem);
            String name = recipeItem.getName();
            Toast.makeText(this.getContext(), "\"" + name + "\" removed.", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Log.e("Remove recipe failed", ex.getMessage() != null ? ex.getMessage() : "");
        }
    }

    @Override
    public void onSaveRecipeClick(RecipeItem recipeItem) {
        try {
            db.recipeItemDAO().insert(recipeItem);
            String name = recipeItem.getName();
            Toast.makeText(this.getContext(), "Saved recipe for \"" + recipeItem.getName() + "\"!", Toast.LENGTH_LONG).show();

        } catch (Exception ex) {
            Log.e("Add recipe failed", ex.getMessage() != null ? ex.getMessage() : "");
        }
    }
}
