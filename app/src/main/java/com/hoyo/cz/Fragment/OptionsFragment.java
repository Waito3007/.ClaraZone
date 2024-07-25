package com.hoyo.cz.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
import com.hoyo.cz.Model.PostSaved;
import com.hoyo.cz.R;

public class OptionsFragment extends DialogFragment {

    private String pid;
    private boolean isPostSaved = false;

    public OptionsFragment(String pid) {
        this.pid = pid;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_options, container, false);

        TextView tvEdit = view.findViewById(R.id.tvEdit);
        TextView tvDelete = view.findViewById(R.id.tvDelete);
        TextView tvSave = view.findViewById(R.id.tvSave);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(pid);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("account").child(currentUserId);
        DatabaseReference savedPostsRef = FirebaseDatabase.getInstance().getReference("post_saved");

        // Kiểm tra xem bài viết đã được lưu chưa
        savedPostsRef.orderByChild("pid").equalTo(pid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostSaved postSavedObj = snapshot.getValue(PostSaved.class);
                    if (postSavedObj != null && postSavedObj.getUid().equals(currentUserId)) {
                        isPostSaved = true;
                        tvSave.setText("Bỏ lưu bài viết");
                        break;
                    }
                }
                if (!isPostSaved) {
                    tvSave.setText("Lưu bài viết");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi khi kiểm tra trạng thái bài viết", Toast.LENGTH_SHORT).show();
            }
        });

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

        // Xử lý khi nhấn vào nút Lưu/Bỏ Lưu
        tvSave.setOnClickListener(v -> {
            if (isPostSaved) {
                savedPostsRef.orderByChild("pid").equalTo(pid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            PostSaved postSavedObj = snapshot.getValue(PostSaved.class);
                            if (postSavedObj != null && postSavedObj.getUid().equals(currentUserId)) {
                                savedPostsRef.child(postSavedObj.getPsid()).removeValue();
                                Toast.makeText(getContext(), "Đã bỏ lưu bài viết", Toast.LENGTH_SHORT).show();
                                tvSave.setText("Lưu bài viết");
                                isPostSaved = false;
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Lỗi khi bỏ lưu bài viết", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                String psid = savedPostsRef.push().getKey();
                PostSaved postSavedObj = new PostSaved(psid, currentUserId, pid, true);
                savedPostsRef.child(psid).setValue(postSavedObj);
                Toast.makeText(getContext(), "Đã lưu bài viết", Toast.LENGTH_SHORT).show();
                tvSave.setText("Bỏ lưu bài viết");
                isPostSaved = true;
            }
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
