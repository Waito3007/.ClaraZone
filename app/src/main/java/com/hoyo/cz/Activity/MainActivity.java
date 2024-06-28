package com.hoyo.cz.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hoyo.cz.Fragment.HomeFragment;
import com.hoyo.cz.Fragment.ProfileFragment;
import com.hoyo.cz.Fragment.SearchFragment;
import com.hoyo.cz.R;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Kiểm tra nếu người dùng chưa đăng nhập
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Chuyển đến SignInActivity
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish(); // Đóng MainActivity để người dùng không thể quay lại bằng nút Back
        } else {
            // Tiếp tục với logic onCreate hiện tại của bạn
            setContentView(R.layout.activity_main);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (itemId == R.id.nav_search) {
                        selectedFragment = new SearchFragment();
                    } else if (itemId == R.id.nav_profile) {
                        selectedFragment = new ProfileFragment();
                    }
                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
                        return true;
                    }
                    return false;
                }
            });
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }
}
