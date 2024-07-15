package com.hoyo.cz.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hoyo.cz.Activity.UDPageActivity;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.R;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private final Context context;
    private final List<Account> accountList;

    public AccountAdapter(Context context, List<Account> accountList) {
        this.context = context;
        this.accountList = accountList;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accountList.get(position);
        holder.tvName.setText(account.getNameUser());
        holder.tvEmail.setText(account.getEmail());
        Glide.with(context).load(account.getAvatarUser()).circleCrop().into(holder.ivAvatar);

        // Xử lý sự kiện click vào item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang UDPageActivity và truyền userId
                Intent intent = new Intent(context, UDPageActivity.class);
                intent.putExtra("userId", account.getUid()); // Truyền userId của người dùng được click
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return accountList.size();
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        ImageView ivAvatar;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}
