package com.hoyo.cz.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.R;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private List<Account> accounts;
    public AccountAdapter(List<Account> accounts){
        this.accounts = accounts;
    }

    @NonNull
    @Override
    public AccountAdapter.AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.bind(account);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public void updateList(List<Account> newList) {
        accounts = newList;
        notifyDataSetChanged();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivAvatarUser;
        private TextView tvAccountName;
        public AccountViewHolder(View itemView){
            super(itemView);
            ivAvatarUser = itemView.findViewById(R.id.ivAvatarUser);
            tvAccountName = itemView.findViewById(R.id.tvAccountName);
        }
        public void bind (Account account){
            tvAccountName.setText(account.getNameUser());
            if(account.getAvatarUser() != null && !account.getAvatarUser().isEmpty()){
                Glide.with(itemView.getContext())
                        .load(account.getAvatarUser())
                        .placeholder(R.drawable.avatar_macdinh)
                        .into(ivAvatarUser);
            }
            else {
                ivAvatarUser.setImageResource(R.drawable.avatar_macdinh);
            }
        }
    }
}