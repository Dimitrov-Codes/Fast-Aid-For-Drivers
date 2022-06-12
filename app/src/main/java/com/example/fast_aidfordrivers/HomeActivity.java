package com.example.fast_aidfordrivers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.example.fast_aidfordrivers.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
    Button btnDrawer;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        drawer = findViewById(R.id.drawer_layout);
        btnDrawer = findViewById(R.id.btnDrawer);
        btnDrawer.setOnClickListener(v->{
            drawer.open();

        });
        NavController navController = Navigation.findNavController(findViewById(R.id.fragment));
        NavigationView nav = findViewById(R.id.nav_view);

        NavigationUI.setupWithNavController((NavigationView) findViewById(R.id.nav_view), navController);

        //Setting up an individual MenuOnclickListener to Logout user
        nav.getMenu().getItem(2).setOnMenuItemClickListener(item -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return true;
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(
                Navigation.findNavController(this, R.id.fragment),
                findViewById(R.id.drawer_layout));
    }
}