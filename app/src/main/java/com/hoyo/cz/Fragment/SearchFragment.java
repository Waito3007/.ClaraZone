package com.hoyo.cz.Fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Adapter.AccountAdapter;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.R;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView rvSearchResults;
    private EditText edSearch;
    private AccountAdapter accountAdapter;
    private List<Account> accountList;
    private List<Account> filteredAccountList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        edSearch = view.findViewById(R.id.edSearch);
        rvSearchResults = view.findViewById(R.id.rvSearchResults);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));
        accountList = new ArrayList<>();
        filteredAccountList = new ArrayList<>();

        accountAdapter = new AccountAdapter(filteredAccountList);
        rvSearchResults.setAdapter(accountAdapter);

        loadAccount();

        edSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        return view;
    }

    private void loadAccount() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("account");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accountList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Account account = dataSnapshot.getValue(Account.class);
                    if (account != null) {
                        accountList.add(account);
                        Log.d("FirebaseData", "Account loaded" + account.getNameUser());
                    } else {
                        Log.d("FirebaseData", "Null account object found.");
                    }
                }
                filteredAccountList.clear();
                filteredAccountList.addAll(accountList);
                accountAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error loading data from Firebase: " + error.getMessage());
            }
        });
    }

    public void filter(String text) {
        filteredAccountList.clear();
        Log.d("SearchFilter", "Filtering with text: " + text);
        for (Account account : accountList) {
            if (account.getNameUser().toLowerCase().contains(text.toLowerCase())) {
                filteredAccountList.add(account);
            }
        }
        accountAdapter.notifyDataSetChanged();
        Log.d("SearchFilter", "Filtered results: " + filteredAccountList.size());
        for (Account account : filteredAccountList) {
            Log.d("SearchFilter", "Found account: " + account.getNameUser());
        }
    }
}