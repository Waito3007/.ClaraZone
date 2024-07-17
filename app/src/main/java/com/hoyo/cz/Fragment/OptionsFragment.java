package com.hoyo.cz.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Activity.DeletePostActivity;
import com.hoyo.cz.Activity.EditPostActivity;
import com.hoyo.cz.R;

public class OptionsFragment extends DialogFragment {

    private String pid;

    public OptionsFragment(String pid) {
        this.pid = pid;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        TextView tvEdit = view.findViewById(R.id.tvEdit);
        TextView tvDelete = view.findViewById(R.id.tvDelete);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(pid);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("account").child(currentUserId);

        // Kiểm tra xem người dùng hiện tại có phải là người đăng bài hoặc là admin hay không
        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String postUid = snapshot.child("uid").getValue(String.class);
                if (currentUserId.equals(postUid)) {
                    // Người dùng hiện tại là người đăng bài
                    tvEdit.setVisibility(View.VISIBLE);
                    tvDelete.setVisibility(View.VISIBLE);
                } else {
                    // Kiểm tra nếu người dùng hiện tại là admin
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Boolean isAdmin = snapshot.child("admin").getValue(Boolean.class);
                            if (isAdmin != null && isAdmin) {
                                // Người dùng hiện tại là admin
                                tvEdit.setVisibility(View.VISIBLE);
                                tvDelete.setVisibility(View.VISIBLE);
                            } else {
                                // Không phải là admin và không phải là người đăng bài
                                tvEdit.setVisibility(View.GONE);
                                tvDelete.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Xử lý lỗi nếu cần
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });

        // Xử lý khi nhấn vào nút Chỉnh sửa
        tvEdit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditPostActivity.class);
            intent.putExtra("pid", pid);
            startActivity(intent);
            dismiss(); // Đóng fragment sau khi chuyển sang EditPostActivity
        });

        // Xử lý khi nhấn vào nút Xóa
        tvDelete.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), DeletePostActivity.class);
            intent.putExtra("pid", pid);
            startActivity(intent);
            dismiss(); // Đóng fragment sau khi chuyển sang DeletePostActivity
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            getDialog().getWindow().setWindowAnimations(R.style.DialogAnimation); // Thiết lập animation cho fragment
            getDialog().getWindow().setDimAmount(0.5f); // Thiết lập độ mờ của nền
        }
    }
}
