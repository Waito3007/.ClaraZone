package com.hoyo.cz.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
        setContentView(R.layout.activity_main);

        // Kiểm tra nếu người dùng cần phải đăng ký
        if (needsSignUp()) {
            // Chuyển hướng đến SignUpActivity
            Intent intent = new Intent(MainActivity.this, PostActivity.class);
            startActivity(intent);
            finish(); // Kết thúc MainActivity để người dùng không thể quay lại
            return; // Thoát khỏi phương thức onCreate
        }

        // Tiếp tục với logic onCreate hiện tại của bạn
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

    // Phương thức để kiểm tra xem người dùng cần đăng ký hay không
    private boolean needsSignUp() {
        // Thực hiện logic để xác định nếu người dùng cần phải đăng ký
        // Ví dụ: kiểm tra xem người dùng đã được xác thực bằng Firebase Authentication chưa
        // Bạn có thể sử dụng phương thức getCurrentUser() của Firebase Auth để kiểm tra người dùng đã đăng nhập chưa
        // Nếu chưa xác thực hoặc chưa đăng ký, return true; ngược lại, return false
        // Ví dụ kiểm tra Firebase Authentication:
        // FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // return currentUser == null; // Trả về true nếu chưa xác thực, false nếu đã xác thực
        return true; // Đây là ví dụ, bạn cần thay thế với logic thực tế của bạn
    }

}
