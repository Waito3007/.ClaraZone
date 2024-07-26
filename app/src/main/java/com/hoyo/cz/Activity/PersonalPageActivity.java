package com.hoyo.cz.Activity;

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
import com.hoyo.cz.Adapter.SharePostAdapter;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.Model.Share;
import com.hoyo.cz.R;

import java.util.ArrayList;
import java.util.List;

public class PersonalPageActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private Button btBack;
    private TextView timelineBtn;
    private TextView sharePostBtn;
    private RecyclerView postsRecyclerView;
    private ImageView userAvatar;
    private DatabaseReference userRef, postsRef, sharesRef;
    private List<Post> postList;
    private List<Share> shareList;
    private PostAdapter postAdapter;
    private SharePostAdapter sharePostAdapter;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_page);

        userNameTextView = findViewById(R.id.user_name);
        btBack = findViewById(R.id.btBack);
        userAvatar = findViewById(R.id.user_Avatar);
        timelineBtn = findViewById(R.id.timeline_btn);
        sharePostBtn = findViewById(R.id.sharePost_btn);
        postsRecyclerView = findViewById(R.id.recycler_view_posts);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("account").child(userId);
            postsRef = FirebaseDatabase.getInstance().getReference("posts");
            sharesRef = FirebaseDatabase.getInstance().getReference("share");

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Account account = snapshot.getValue(Account.class);
                    if (account != null) {
                        userNameTextView.setText(account.getNameUser());
                        Glide.with(PersonalPageActivity.this)
                                .load(account.getAvatarUser() != null ? account.getAvatarUser() : R.drawable.avatar_macdinh)
                                .circleCrop()
                                .placeholder(R.drawable.avatar_macdinh)
                                .error(R.drawable.avatar_macdinh)
                                .into(userAvatar);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PersonalPageActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
            btBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Quay về trang trước đó
                    finish();
                }
            });
            postList = new ArrayList<>();
            shareList = new ArrayList<>();
            postsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            postAdapter = new PostAdapter(this, postList);
            sharePostAdapter = new SharePostAdapter(shareList, this);
            postsRecyclerView.setAdapter(postAdapter);

            loadUserPosts(userId);

            timelineBtn.setOnClickListener(v -> loadUserPosts(userId));
            sharePostBtn.setOnClickListener(v -> loadUserSharedPosts(userId));
        }
    }

    private void loadUserPosts(String userId) {
        postsRef.orderByChild("uid").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    postList.add(post);
                }
                postsRecyclerView.setAdapter(postAdapter);
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PersonalPageActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserSharedPosts(String userId) {
        sharesRef.orderByChild("uid").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shareList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Share share = dataSnapshot.getValue(Share.class);
                    shareList.add(share);
                }
                postsRecyclerView.setAdapter(sharePostAdapter);
                sharePostAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PersonalPageActivity.this, "Failed to load shared posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
