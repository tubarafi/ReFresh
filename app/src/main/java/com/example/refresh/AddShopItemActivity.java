package com.example.refresh;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.refresh.database.AppDatabase;
import com.example.refresh.database.model.ShopItem;
import com.example.refresh.util.PriceUtil;

import java.util.Objects;


public class AddShopItemActivity extends AppCompatActivity {

    private EditText nameEditText, quantityEditText;
    private Button createButton, cancelButton;

    private AppDatabase db;
    private ShopItem shopItem;
    private boolean update = false;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shop_item);
        nameEditText = findViewById(R.id.shopNameEditText);
        quantityEditText = findViewById(R.id.quantityEditText);
        createButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);
        db = AppDatabase.getAppDatabase(AddShopItemActivity.this);
        pos = getIntent().getIntExtra("position", -1);
        if ((shopItem = (ShopItem) getIntent().getSerializableExtra("shop_item")) != null) {
            getSupportActionBar().setTitle("Update Shop Item");
            update = true;

            createButton.setText("Update");
            nameEditText.setText(shopItem.getName());
            quantityEditText.setText(String.valueOf(shopItem.getQuantity()));
        } else {
            Objects.requireNonNull(getSupportActionBar()).setTitle("Create Shop Item");
        }
        createButton.setOnClickListener(view -> {
            Context context = getApplicationContext();
            String itemName = nameEditText.getText().toString();
            String itemQuantity = quantityEditText.getText().toString();
            if (itemName.equals("")) {
                Toast.makeText(context, "Please enter an item name.", Toast.LENGTH_LONG).show();
            } else if (itemQuantity.equals("")) {
                Toast.makeText(context, "Please enter a quantity.", Toast.LENGTH_LONG).show();
            } else {
                if (update) {

                    PriceUtil.getItemPrice(result -> {
                        shopItem.setName(nameEditText.getText().toString());
                        shopItem.setQuantity(Integer.parseInt(quantityEditText.getText().toString()));
                        shopItem.setMsrp(result);

                        try {
                            db.shopItemDAO().update(shopItem);

                            setResult(shopItem, 2); // update
                            Toast.makeText(context, "Updated " + shopItem.getName() + ".", Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            Log.e("Update Shop failed", ex.getMessage() != null ? ex.getMessage() : "");
                        }
                    }, AddShopItemActivity.this, nameEditText.getText().toString(), quantityEditText.getText().toString());

                } else {
                    PriceUtil.getItemPrice(result -> {

                        ShopItem newShopItem = new ShopItem(nameEditText.getText().toString(), Integer.parseInt(quantityEditText.getText().toString()), result);

                        try {
                            db.shopItemDAO().insert(newShopItem);
                            setResult(newShopItem, 1); //create
                            Toast.makeText(context, "Added " + newShopItem.getName() + " to shopping list.", Toast.LENGTH_LONG).show();
                        } catch (Exception ex) {
                            Log.e("Add Shop failed", ex.getMessage() != null ? ex.getMessage() : "");
                        }


                    }, AddShopItemActivity.this, nameEditText.getText().toString(), quantityEditText.getText().toString());
                }
            }
        });

        cancelButton.setOnClickListener(view -> finish());
    }

    private void setResult(ShopItem shop, int flag) {
        setResult(flag, new Intent().putExtra("shop_item", shop).putExtra("position", pos));
        finish();
    }

}
