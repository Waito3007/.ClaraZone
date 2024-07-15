package com.hoyo.cz.Activity;// AdminPageActivity.java

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Adapter.AccountAdapter;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.R;

import java.util.ArrayList;
import java.util.List;

public class AdminPageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AccountAdapter adapter;
    private List<Account> accountList;

    private DatabaseReference accountsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_page);

        recyclerView = findViewById(R.id.recyclerViewAccounts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        accountList = new ArrayList<>();
        adapter = new AccountAdapter(this, accountList);
        recyclerView.setAdapter(adapter);

        accountsRef = FirebaseDatabase.getInstance().getReference("account");

        loadAccounts("");

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                loadAccounts(newText);
                return true;
            }
        });
    }

    private void loadAccounts(String searchText) {
        Query query;
        if (TextUtils.isEmpty(searchText)) {
            query = accountsRef.orderByChild("nameUser");
        } else {
            query = accountsRef.orderByChild("nameUser").startAt(searchText).endAt(searchText + "\uf8ff");
        }

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accountList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Account account = ds.getValue(Account.class);
                    if (account != null) {
                        accountList.add(account);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminPageActivity.this, "Failed to load accounts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
