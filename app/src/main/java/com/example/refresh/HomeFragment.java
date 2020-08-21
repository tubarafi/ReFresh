package com.example.refresh;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refresh.database.model.FoodItem;
import com.example.refresh.util.fragmentCallbackListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.example.refresh.adapter.FoodItemListAdapter;
import com.example.refresh.database.AppDatabase;

public class HomeFragment extends Fragment implements FoodItemListAdapter.OnFoodItemListener {

    private List<FoodItem> foodItemList = new ArrayList<>();
    private TextView foodItemListEmptyTextView;
    private TextView totalText;
    private TextView totalCostText;
    private RecyclerView recyclerView;
    private FoodItemListAdapter foodItemListAdapter;
    private AppDatabase db;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, cameraFab, imageFab, manualFab, recipeFab, cancelFab;
    private Animation add_option_fab_open, add_option_fab_close, rotate_forward, rotate_backward, show_fab, hide_fab;
    private Button genButton;
    private boolean showingCheckboxes;
    private fragmentCallbackListener callbackListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Food Item List");
        this.setHasOptionsMenu(true);
        db = AppDatabase.getAppDatabase(getContext());
        foodItemListEmptyTextView = rootView.findViewById(R.id.emptyListTextView);
        totalText = rootView.findViewById(R.id.totalText);
        totalCostText = rootView.findViewById(R.id.totalCostText);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        foodItemListAdapter = new FoodItemListAdapter(getActivity(), foodItemList, this);
        recyclerView.setAdapter(foodItemListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        fab = rootView.findViewById(R.id.fab);
        cameraFab = rootView.findViewById(R.id.fab_camera);
        imageFab = rootView.findViewById(R.id.fab_image);
        manualFab = rootView.findViewById(R.id.fab_manual);
        recipeFab = rootView.findViewById(R.id.fab_generateRecipes);
        cancelFab = rootView.findViewById(R.id.fab_cancelGeneration);
        genButton = rootView.findViewById(R.id.genRecipesButton);

        add_option_fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.add_option_fab_open);
        add_option_fab_close = AnimationUtils.loadAnimation(getContext(), R.anim.add_option_fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_backward);
        show_fab = AnimationUtils.loadAnimation(getContext(), R.anim.show_fab);
        hide_fab = AnimationUtils.loadAnimation(getContext(), R.anim.hide_fab);

        manualFab.setOnClickListener(view -> {
            AnimateAddFab();
                    startActivityForResult(new Intent(getActivity(), AddFoodItemActivity.class), 100);
        });
        cameraFab.setOnClickListener(view -> {
            startActivityForResult(new Intent(getActivity(), ScannerActivity.class), 100);
            AnimateAddFab();
        });
        imageFab.setOnClickListener(view -> {
            startActivityForResult(new Intent(getActivity(), GalleryActivity.class), 100);
            AnimateAddFab();
        });
        fab.setOnClickListener(view -> AnimateAddFab());

        recipeFab.setOnClickListener(view -> {
                if (!showingCheckboxes) {
                    genButton.setVisibility(View.VISIBLE);
                    foodItemListAdapter.toggleSelectIngredients(true);
                    showingCheckboxes = true;
                    recipeFab.hide();
                    cancelFab.show();
                    fab.hide();
                }
        });

        cancelFab.setOnClickListener(view -> {
                if (showingCheckboxes) {
                    genButton.setVisibility(View.GONE);
                    foodItemListAdapter.toggleSelectIngredients(false);
                    showingCheckboxes = false;
                    cancelFab.hide();
                    recipeFab.show();
                    fab.show();
                }
        });

        genButton.setOnClickListener(view -> {
                if (foodItemListAdapter.selectedIngredientList == null || foodItemListAdapter.selectedIngredientList.size() == 0) {
                    Toast.makeText(getContext(), "Please select at least one ingredient!", Toast.LENGTH_SHORT).show();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ingredientList", foodItemListAdapter.selectedIngredientList);
                    bundle.putString("loadLocation", "homeFrag");
                    if (callbackListener != null) {
                        callbackListener.onCallback("recipe", bundle);
                    }
                }
        });

        new RetrieveTask(this).execute();
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.support_bar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.notification_setting) {
            startActivityForResult(new Intent(getActivity(), SettingsActivity.class), 100);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void AnimateAddFab() {
        if (isFabOpen) {
            fab.startAnimation(rotate_backward);
            cameraFab.startAnimation(add_option_fab_close);
            imageFab.startAnimation(add_option_fab_close);
            manualFab.startAnimation(add_option_fab_close);
            recipeFab.startAnimation(show_fab);
            cameraFab.setClickable(false);
            imageFab.setClickable(false);
            manualFab.setClickable(false);
            recipeFab.setClickable(true);
            isFabOpen = false;
        } else {
            fab.startAnimation(rotate_forward);
            cameraFab.startAnimation(add_option_fab_open);
            imageFab.startAnimation(add_option_fab_open);
            manualFab.startAnimation(add_option_fab_open);
            recipeFab.startAnimation(hide_fab);
            cameraFab.setClickable(true);
            imageFab.setClickable(true);
            manualFab.setClickable(true);
            recipeFab.setClickable(false);
            isFabOpen = true;
        }
    }

    private static class RetrieveTask extends AsyncTask<Void, Void, List<FoodItem>> {

        private WeakReference<HomeFragment> c;

        // only retain a weak reference to the activity
        RetrieveTask(HomeFragment context) {
            c = new WeakReference<>(context);
        }

        @Override
        protected List<FoodItem> doInBackground(Void... voids) {
            if (c.get() != null)
                return c.get().db.foodItemDAO().getAll();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<FoodItem> items) {
            if (items != null) {
                if (items.size() > 0) {
                    c.get().foodItemList.clear();
                    c.get().foodItemList.addAll(items);
                    // hides empty text view
                    c.get().foodItemListEmptyTextView.setVisibility(View.GONE);
                    c.get().foodItemListAdapter.notifyDataSetChanged();
                    c.get().totalText.setVisibility(View.GONE);
                    c.get().totalCostText.setVisibility(View.GONE);
                } else {
                    c.get().foodItemListEmptyTextView.setVisibility(View.VISIBLE);
                    c.get().totalText.setVisibility(View.GONE);
                    c.get().totalCostText.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() instanceof fragmentCallbackListener)
            callbackListener = (fragmentCallbackListener) getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode > 0) {
            int pos = data.getIntExtra("position", -1);
            if (resultCode == 1) {
                foodItemList.add((FoodItem) data.getSerializableExtra("food_item"));
            } else if (resultCode == 2) {
                if (pos != -1) {
                    foodItemList.set(pos, (FoodItem) data.getSerializableExtra("food_item"));
                }
            } else if (resultCode == 3) {
                foodItemList.addAll((List<FoodItem>) Objects.requireNonNull(data.getSerializableExtra("food_items")));
            }
            listVisibility();
        }
    }

    public void listVisibility() {
        int emptyMsgVisibility = View.GONE;
        if (foodItemList.size() == 0) { // no item to display
            if (foodItemListEmptyTextView.getVisibility() == View.GONE)
                emptyMsgVisibility = View.VISIBLE;
        }
        foodItemListEmptyTextView.setVisibility(emptyMsgVisibility);
        foodItemListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFoodItemClick(int pos) {
    }
}

