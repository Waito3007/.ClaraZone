package com.hoyo.cz.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hoyo.cz.R;

public class DeletePostActivity extends AppCompatActivity {

    private Button btnDelete;
    private ProgressBar progressBar;
    private DatabaseReference postsRef;
    private String pid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_post);

        btnDelete = findViewById(R.id.btnDelete);
        progressBar = findViewById(R.id.progressBar);

        // Lấy pid từ Intent
        pid = getIntent().getStringExtra("pid");

        // Tham chiếu đến node "posts" trong Firebase Realtime Database
        postsRef = FirebaseDatabase.getInstance().getReference("posts").child(pid);

        btnDelete.setOnClickListener(v -> deletePost());
    }

    private void deletePost() {
        progressBar.setVisibility(View.VISIBLE);
        postsRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DeletePostActivity.this, "Xóa bài viết thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(DeletePostActivity.this, "Lỗi khi xóa bài viết", Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);
        });
    }
}
