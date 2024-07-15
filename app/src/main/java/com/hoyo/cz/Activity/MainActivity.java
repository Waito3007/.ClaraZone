package com.hoyo.cz.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Fragment.HomeFragment;
import com.hoyo.cz.Fragment.ProfileFragment;
import com.hoyo.cz.Fragment.SearchFragment;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.R;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Kiểm tra nếu người dùng chưa đăng nhập
        if (currentUser == null) {
            // Chuyển đến SignInActivity
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
            finish(); // Đóng MainActivity để người dùng không thể quay lại bằng nút Back
        } else {
            // Kiểm tra thông tin người dùng
            databaseReference = FirebaseDatabase.getInstance().getReference("account").child(currentUser.getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Account account = snapshot.getValue(Account.class);
                    if (account == null || account.getNameUser() == null || account.getNameUser().isEmpty()) {
                        // Chuyển đến IPInforActivity nếu tên người dùng trống
                        Intent intent = new Intent(MainActivity.this, IPInforActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Tiếp tục với logic onCreate hiện tại của bạn
                        initMainActivity();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void initMainActivity() {
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_search) {
                // Chuyển tới AdminPageActivity thay vì SearchFragment
                Intent intent = new Intent(MainActivity.this, AdminPageActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }
            if (selectedFragment != null) {
                loadFragment(selectedFragment); // Load Fragment mới
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void loadFragment(Fragment fragment) {
        // Replace Fragment hiện tại bằng Fragment mới
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null); // Để có thể quay lại Fragment trước đó bằng nút Back
        transaction.commit();
    }
}
