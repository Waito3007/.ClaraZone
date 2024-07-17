package com.hoyo.cz.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Activity.SignInActivity;
import com.hoyo.cz.Activity.UDPageActivity;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.R;

import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private final Context context;
    private final List<Account> accountList;
    private final DatabaseReference databaseReference;
    private boolean isAdmin = false;

    public AccountAdapter(Context context, List<Account> accountList) {
        this.context = context;
        this.accountList = accountList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("account");
        checkAdminStatus();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Account account = accountList.get(position);
        holder.tvName.setText(account.getNameUser());
        holder.tvEmail.setText(account.getEmail());

        // Sử dụng Glide để tải ảnh, nếu không có thì hiển thị ảnh mặc định
        Glide.with(context)
                .load(account.getAvatarUser())
                .placeholder(R.drawable.clara) // Ảnh mặc định
                .circleCrop()
                .into(holder.ivAvatar);

        // Chỉ hiển thị nút Ban nếu người dùng hiện tại là admin
        if (isAdmin) {
            holder.btnBan.setVisibility(View.VISIBLE);
            // Thiết lập trạng thái nút Ban
            if (account.isStatusBan()) {
                holder.btnBan.setText("Đang vô hiệu");
            } else {
                holder.btnBan.setText("Vô hiệu hóa");
            }

            // Xử lý sự kiện click vào nút Ban
            holder.btnBan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean newStatusBan = !account.isStatusBan();
                    account.setStatusBan(newStatusBan);
                    databaseReference.child(account.getUid()).setValue(account);
                    notifyItemChanged(position); // Cập nhật giao diện

                    // Nếu tài khoản bị vô hiệu hóa, chuyển người dùng bị vô hiệu hóa về đăng nhập
                    if (newStatusBan) {
                        banAccount(account.getUid());
                    }
                }
            });
        } else {
            holder.btnBan.setVisibility(View.GONE);
        }

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

    private void checkAdminStatus() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Account currentUser = snapshot.getValue(Account.class);
                if (currentUser != null) {
                    isAdmin = currentUser.isAdmin();
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void banAccount(String userId) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null && currentUser.getUid().equals(userId)) {
            auth.signOut();
            Intent intent = new Intent(context, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }

    public static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail;
        ImageView ivAvatar;
        Button btnBan;

        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            btnBan = itemView.findViewById(R.id.btnBan);
        }
    }
}
