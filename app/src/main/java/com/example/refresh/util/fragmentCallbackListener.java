package com.example.refresh.util;

import android.os.Bundle;

import androidx.annotation.Nullable;

public interface fragmentCallbackListener {
    void onCallback(String fragmentName, @Nullable Bundle param);
}
