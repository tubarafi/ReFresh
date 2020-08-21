package com.example.refresh;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refresh.adapter.ShopItemListAdapter;
import com.example.refresh.database.AppDatabase;
import com.example.refresh.database.model.FoodItem;
import com.example.refresh.database.model.ShopItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class ShoppingFragment extends Fragment implements ShopItemListAdapter.OnShopItemListener {

    private List<ShopItem> shopItemList = new ArrayList<>();

    private TextView shopItemListEmptyTextView;
    private TextView totalText;
    private TextView totalCostText;
    private RecyclerView recyclerView;
    private ShopItemListAdapter shopItemListAdapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_shopping, container, false);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar().setTitle("Shopping List");
        db = AppDatabase.getAppDatabase(getContext());
        shopItemListEmptyTextView = rootView.findViewById(R.id.emptyListTextView);
        totalText = rootView.findViewById(R.id.totalText);
        totalCostText = rootView.findViewById(R.id.totalCostText);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        shopItemListAdapter = new ShopItemListAdapter(getActivity(), shopItemList, this);
        recyclerView.setAdapter(shopItemListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(view -> startActivityForResult(new Intent(getActivity(), AddShopItemActivity.class), 100));
        new ShoppingFragment.RetrieveTask(this).execute();
        return rootView;
    }

    private static class RetrieveTask extends AsyncTask<Void, Void, List<ShopItem>> {

        private WeakReference<ShoppingFragment> c;

        // only retain a weak reference to the activity
        RetrieveTask(ShoppingFragment context) {
            c = new WeakReference<>(context);
        }

        @Override
        protected List<ShopItem> doInBackground(Void... voids) {
            if (c.get() != null)
                return c.get().db.shopItemDAO().getAll();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<ShopItem> items) {
            if (items != null) {
                if (items.size() > 0) {
                    c.get().shopItemList.clear();
                    c.get().shopItemList.addAll(items);
                    // hides empty text view
                    c.get().shopItemListEmptyTextView.setVisibility(View.GONE);
                    c.get().totalText.setVisibility(View.VISIBLE);
                    c.get().totalCostText.setVisibility(View.VISIBLE);
                    c.get().shopItemListAdapter.notifyDataSetChanged();
                } else {
                    c.get().shopItemListEmptyTextView.setVisibility(View.VISIBLE);
                    c.get().totalText.setVisibility(View.GONE);
                    c.get().totalCostText.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode > 0) {
            int pos = data.getIntExtra("position", -1);
            if (resultCode == 1) {
                shopItemList.add((ShopItem) data.getSerializableExtra("shop_item"));
            } else if (resultCode == 2) {
                if (pos != -1) {
                    shopItemList.set(pos, (ShopItem) data.getSerializableExtra("shop_item"));
                }
            }
            listVisibility();
        }
    }

    public void listVisibility() {
        int emptyMsgVisibility = View.GONE;
        int totalMsgVisibility = View.VISIBLE;
        if (shopItemList.size() == 0) { // no item to display
            if (shopItemListEmptyTextView.getVisibility() == View.GONE)
                emptyMsgVisibility = View.VISIBLE;
                totalMsgVisibility = View.GONE;
        }
        shopItemListEmptyTextView.setVisibility(emptyMsgVisibility);
        totalText.setVisibility(totalMsgVisibility);
        totalCostText.setVisibility(totalMsgVisibility);
        shopItemListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onShopItemClick(int pos) {
        final int position = pos;

        ShopItem item = shopItemList.get(position);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        View view = getLayoutInflater().inflate(R.layout.dialog_shop_to_fridge, null);
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setMessage("Move " + item.getName() + " to the fridge?");

        EditText remindDateEditText = view.findViewById(R.id.shopingEditText);
        remindDateEditText.setInputType(InputType.TYPE_NULL);

        alertDialogBuilder.setPositiveButton("OK", null);
        alertDialogBuilder.setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());

        AlertDialog alertDialog = alertDialogBuilder.create();


        alertDialog.setOnShowListener(dialogInterface -> {

            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                ShopItem item1 = shopItemList.get(position);

                if (remindDateEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a date.", Toast.LENGTH_LONG).show();
                } else {
                    FoodItem foodItem = new FoodItem(item1.getName(), remindDateEditText.getText().toString(), item1.getQuantity(), "");
                    try {
                        db.foodItemDAO().insert(foodItem);
                        db.shopItemDAO().delete(item1);
                        shopItemList.remove(position);
                        shopItemListAdapter.notifyDataSetChanged();
                        alertDialog.dismiss();
                    } catch (Exception ex) {
                        Log.e("Move Shop item failed", ex.getMessage() != null ? ex.getMessage() : "");
                    }
                }

            });
        });

        alertDialog.show();

        remindDateEditText.setOnClickListener(v -> {
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            DatePickerDialog picker = new DatePickerDialog(getContext(), (view1, year1, monthOfYear, dayOfMonth) -> remindDateEditText.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year1), year, month, day);
            picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            picker.show();
        });
    }

    public void setTotalCost() {
        float total = 0;

        for (int i = 0; i < shopItemList.size(); i++) {
            String msrp = shopItemList.get(i).getMsrp().replace("$", "");
            float cost = Float.parseFloat(msrp);
            total = total + cost;
        }

        totalCostText.setText(String.format("$%.2f", total));
    }


}
