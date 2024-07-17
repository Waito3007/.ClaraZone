package com.hoyo.cz.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Adapter.PostAdapter;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.Model.Follow;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.R;

import java.util.ArrayList;
import java.util.List;

public class UDPageActivity extends AppCompatActivity {

    private ImageView imageViewAvatar;
    private TextView textViewName;
    private Button btnFollow,btBack;
    private RecyclerView recyclerViewPosts;

    private DatabaseReference accountRef;
    private DatabaseReference followRef;
    private FirebaseUser currentUser;

    private String userId; // ID của người dùng được chọn

    private List<Post> postList;
    private PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ud_page);

        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        textViewName = findViewById(R.id.textViewName);
        btnFollow = findViewById(R.id.btnFollow);
        btBack = findViewById(R.id.btback);
        recyclerViewPosts = findViewById(R.id.recyclerViewPosts);
        accountRef = FirebaseDatabase.getInstance().getReference("account");
        followRef = FirebaseDatabase.getInstance().getReference("follow");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Nhận userId từ Intent
        userId = getIntent().getStringExtra("userId");
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPosts.setAdapter(postAdapter);
        loadUserData();
        loadUserPosts();
        btnFollow.setOnClickListener(v -> toggleFollow());

        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Quay về trang trước đó
                finish();
            }
        });
    }

    private void loadUserData() {
        accountRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Account account = snapshot.getValue(Account.class);
                if (account != null) {
                    textViewName.setText(account.getNameUser());
                    Glide.with(UDPageActivity.this)
                            .load(account.getAvatarUser() != null ? account.getAvatarUser() : R.drawable.avatar_macdinh)
                            .circleCrop()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(imageViewAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UDPageActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });

        // Kiểm tra xem currentUser có đang follow userId không
        if (currentUser != null) {
            followRef.orderByChild("followingId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isFollowing = false;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Follow follow = ds.getValue(Follow.class);
                        if (follow != null && follow.getUid().equals(currentUser.getUid())) {
                            isFollowing = follow.isStatusF();
                            break;
                        }
                    }
                    updateFollowButton(isFollowing);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UDPageActivity.this, "Failed to load follow status.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadUserPosts() {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("posts");
        postsRef.orderByChild("uid").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UDPageActivity.this, "Failed to load user posts.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFollow() {
        // Xử lý chức năng follow/unfollow
        // Ví dụ: cập nhật trạng thái follow/unfollow, điều chỉnh theo cấu trúc cơ sở dữ liệu của bạn
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to follow.", Toast.LENGTH_SHORT).show();
            return;
        }

        String followingId = userId;
        String uid = currentUser.getUid();

        followRef.orderByChild("followingId").equalTo(followingId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isFollowing = false;
                String followId = null;
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Follow follow = ds.getValue(Follow.class);
                    if (follow != null && follow.getUid().equals(uid)) {
                        isFollowing = follow.isStatusF();
                        followId = follow.getFid();
                        break;
                    }
                }

                if (isFollowing) {
                    // Unfollow
                    if (followId != null) {
                        followRef.child(followId).removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                updateFollowButton(false);
                            }
                        });
                    }
                } else {
                    // Follow
                    followId = followRef.push().getKey();
                    String currentDate = getCurrentTimestamp();
                    Follow newFollow = new Follow(followId, uid, followingId, true, currentDate);
                    followRef.child(followId).setValue(newFollow).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            updateFollowButton(true);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UDPageActivity.this, "Failed to toggle follow status.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentTimestamp() {
        // Lấy thời gian hiện tại
        // Ví dụ: cài đặt theo định dạng ngày tháng của bạn
        return String.valueOf(System.currentTimeMillis());
    }

    private void updateFollowButton(boolean isFollowing) {
        // Cập nhật giao diện nút follow
        if (isFollowing) {
            btnFollow.setText("Bỏ theo dõi");
        } else {
            btnFollow.setText("Theo dõi");
        }
    }
}
