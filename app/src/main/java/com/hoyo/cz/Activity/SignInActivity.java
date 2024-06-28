package com.hoyo.cz.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hoyo.cz.R;

public class SignInActivity extends AppCompatActivity {
    //khai bao
    EditText edEmail,edPassword; // ed dang nhap signin
    Button btLogin,btForget; //bt dang nhap signin
    Button btSignup,btBack;//bt chuyen trang
    FirebaseAuth mAuth; //pthuc cua firebase
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // Tìm bt có id là btback va btsignup
        btBack = findViewById(R.id.btback);
        btSignup = findViewById(R.id.btSignup);
        btForget = findViewById(R.id.btForget);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        // chuyen sang dang ki
        btSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        btForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });
        //dang nhap
        mAuth = FirebaseAuth.getInstance();

        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        btLogin = findViewById(R.id.btLogin);

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }
    private void signIn() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Đăng nhập thành công và email đã được xác nhận
                                Toast.makeText(SignInActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                // Chuyển đến Main
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // Đóng SignInActivity
                            } else {
                                // Đăng nhập thành công nhưng email chưa được xác nhận
                                Toast.makeText(SignInActivity.this, "Email chưa được xác nhận. Vui lòng kiểm tra email và xác nhận.", Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                            }
                        } else {
                            // Đăng nhập thất bại
                            Toast.makeText(SignInActivity.this, "Đăng nhập thất bại. Vui lòng kiểm tra lại email và mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
