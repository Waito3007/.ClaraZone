package com.hoyo.cz.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UDPageFragment extends Fragment {
    private ImageView imageViewUserAvatar;
    private TextView nameUser;
    private Button btnFollow;
    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private String uid;
    private DatabaseReference userRef;
    private DatabaseReference postsRef;
    private DatabaseReference followRef;
    private FirebaseUser currentUser;

    public UDPageFragment() {
        // Required empty public constructor
    }

    public static UDPageFragment newInstance(String uid) {
        UDPageFragment fragment = new UDPageFragment();
        Bundle args = new Bundle();
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getString("uid");
        }
        userRef = FirebaseDatabase.getInstance().getReference("account");
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        followRef = FirebaseDatabase.getInstance().getReference("follow");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        postList = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ud_page, container, false);

        imageViewUserAvatar = view.findViewById(R.id.imageViewUserAvatar);
        nameUser = view.findViewById(R.id.nameUser);
        btnFollow = view.findViewById(R.id.btnFollow);
        recyclerViewPosts = view.findViewById(R.id.recyclerViewPosts);

        recyclerViewPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerViewPosts.setAdapter(postAdapter);

        loadUserDetails();
        loadUserPosts();
        handleFollowButton();

        return view;
    }

    private void loadUserDetails() {
        userRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Account account = snapshot.getValue(Account.class);
                if (account != null) {
                    nameUser.setText(account.getNameUser());
                    Glide.with(getContext())
                            .load(account.getAvatarUser() != null ? account.getAvatarUser() : R.drawable.avatar_macdinh)
                            .circleCrop()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(imageViewUserAvatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải thông tin người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserPosts() {
        postsRef.orderByChild("uid").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Post post = ds.getValue(Post.class);
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleFollowButton() {
        if (currentUser == null || currentUser.getUid().equals(uid)) {
            btnFollow.setVisibility(View.GONE);
            return;
        }

        String currentUserId = currentUser.getUid();
        followRef.orderByChild("followingId").equalTo(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean[] isFollowing = {false};
                String[] followId = {null};

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Follow follow = ds.getValue(Follow.class);
                    if (follow != null && follow.getUid().equals(currentUserId)) {
                        isFollowing[0] = follow.isStatusF();
                        followId[0] = follow.getFid();
                        break;
                    }
                }

                updateFollowButton(isFollowing[0]);

                btnFollow.setOnClickListener(v -> {
                    if (isFollowing[0]) {
                        // Unfollow
                        if (followId[0] != null) {
                            followRef.child(followId[0]).removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    isFollowing[0] = false;
                                    updateFollowButton(isFollowing[0]);
                                }
                            });
                        }
                    } else {
                        // Follow
                        if (followId[0] == null) {
                            followId[0] = followRef.push().getKey();
                        }
                        String currentDate = getCurrentTimestamp();
                        Follow newFollow = new Follow(followId[0], currentUserId, uid, true, currentDate);
                        followRef.child(followId[0]).setValue(newFollow).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                isFollowing[0] = true;
                                updateFollowButton(isFollowing[0]);
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Lỗi khi tải trạng thái follow", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            btnFollow.setText("Bỏ theo dõi");
        } else {
            btnFollow.setText("Theo dõi");
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("h'h' m'm' 'ngày' d/M/yyyy", Locale.getDefault()).format(new Date());
    }
}
