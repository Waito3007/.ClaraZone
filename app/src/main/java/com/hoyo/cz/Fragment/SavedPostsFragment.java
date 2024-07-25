package com.hoyo.cz.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hoyo.cz.Adapter.PostAdapter;
import com.hoyo.cz.Model.Post;
import com.hoyo.cz.Model.PostSaved;
import com.hoyo.cz.R;

import java.util.ArrayList;
import java.util.List;

public class SavedPostsFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> savedPostsList;
    private DatabaseReference savedPostsRef, postsRef;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_posts, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_saved_posts);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        savedPostsList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), savedPostsList);
        recyclerView.setAdapter(postAdapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        savedPostsRef = FirebaseDatabase.getInstance().getReference("post_saved");
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        loadSavedPosts();

        return view;
    }

    private void loadSavedPosts() {
        savedPostsRef.orderByChild("uid").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                savedPostsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PostSaved postSaved = snapshot.getValue(PostSaved.class);
                    if (postSaved != null) {
                        loadPostDetails(postSaved.getPid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi khi tải bài viết đã lưu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPostDetails(String pid) {
        postsRef.child(pid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                if (post != null) {
                    savedPostsList.add(post);
                    postAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi khi tải chi tiết bài viết", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
