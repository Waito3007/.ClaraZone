package com.hoyo.cz.Fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hoyo.cz.Adapter.CommentAdapter;
import com.hoyo.cz.Model.Comment;
import com.hoyo.cz.R;

import java.util.ArrayList;
import java.util.List;

public class CommentFragment extends Fragment {

    private EditText editTextComment;
    private ImageView addImageComment, sendComment;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    public CommentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_comment, container, false);

//        editTextComment = view.findViewById(R.id.editTextComment);
//        addImageComment = view.findViewById(R.id.addImageComment);
//        sendComment = view.findViewById(R.id.sendComment);

        addImageComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    private void postComment() {
        String commentContent = editTextComment.getText().toString().trim();
        if (!TextUtils.isEmpty(commentContent)) {
            // Implement logic to post comment with or without image
            if (imageUri != null) {
                // Post comment with image
                // Upload image to Firebase Storage
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("comment_images");
                final StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

                fileReference.putFile(imageUri)
                        .addOnSuccessListener(taskSnapshot -> {
                            fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Save comment with image URL to Firebase Realtime Database
                                String imageUrl = uri.toString();
                                saveComment(commentContent, imageUrl);
                            });
                        })
                        .addOnFailureListener(e -> {
                            // Handle unsuccessful uploads
                            Toast.makeText(getActivity(), "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Post comment without image
                saveComment(commentContent, null);
            }
        } else {
            Toast.makeText(getActivity(), "Please enter your comment", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveComment(String commentContent, String imageUrl) {
        // Save comment to Firebase Realtime Database
        String postId = getActivity().getIntent().getStringExtra("postId");
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("comments").child(postId);
        String commentId = commentRef.push().getKey();

//        if (commentId != null) {
//            //Comment comment = new Comment(commentId, postId, getCurrentUserId(), commentContent, imageUrl);
//            commentRef.child(commentId).setValue(comment)
//                    .addOnSuccessListener(aVoid -> {
//                        Toast.makeText(getActivity(), "Comment added successfully", Toast.LENGTH_SHORT).show();
//                        editTextComment.setText("");
//                    })
//                    .addOnFailureListener(e -> {
//                        Toast.makeText(getActivity(), "Failed to add comment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    });
//        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private String getCurrentUserId() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }
}
