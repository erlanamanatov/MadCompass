package com.erkprog.madcompass;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.erkprog.madcompass.model.CompassModel;

public class MainActivity extends AppCompatActivity {
  TextView tvTest;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    tvTest = findViewById(R.id.testtv);

    CompassViewModel model = ViewModelProviders.of(this).get(CompassViewModel.class);
    model.getData().observe(this, new Observer<CompassModel>() {
      @Override
      public void onChanged(@Nullable CompassModel orientState) {
        if (orientState != null) {
          tvTest.setText(orientState.getOrientStr());
        }
      }
    });
  }
}


