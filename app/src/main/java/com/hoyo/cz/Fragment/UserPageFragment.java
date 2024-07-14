package com.hoyo.cz.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class UserPageFragment extends Fragment {

    private TextView userNameTextView;
    private TextView timelineBtn;
    private TextView sharePostBtn;
    private RecyclerView postsRecyclerView;
    private DatabaseReference userRef, postsRef, sharesRef;
    private List<Post> postList;
    private List<Share> shareList;
    private PostAdapter postAdapter;
    private SharePostAdapter sharePostAdapter;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_page, container, false);
        userNameTextView = view.findViewById(R.id.user_name);
        timelineBtn = view.findViewById(R.id.timeline_btn);
        sharePostBtn = view.findViewById(R.id.sharePost_btn);
        postsRecyclerView = view.findViewById(R.id.recycler_view_posts);
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
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });

            postList = new ArrayList<>();
            shareList = new ArrayList<>();
            postsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            postAdapter = new PostAdapter(getActivity(), postList);
            sharePostAdapter = new SharePostAdapter(shareList, getActivity());
            postsRecyclerView.setAdapter(postAdapter);

            loadUserPosts(userId);

            timelineBtn.setOnClickListener(v -> loadUserPosts(userId));
            sharePostBtn.setOnClickListener(v -> loadUserSharedPosts(userId));
        }

        return view;
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
                Toast.makeText(getActivity(), "Failed to load posts", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "Failed to load shared posts", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
