package com.hoyo.cz.Activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hoyo.cz.Model.Account;
import com.hoyo.cz.R;

public class SignUpActivity extends AppCompatActivity {

    private EditText edEmail, edPassword, edConfirmPassword;
    private Button btSignup, btBack;
    private TextView tvCz;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private boolean isAdminConfirmationEnabled = false;
    private int clickCount = 0;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("account");
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassword);
        edConfirmPassword = findViewById(R.id.edConfirmPassword);
        btSignup = findViewById(R.id.btSignup);
        btSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdminConfirmationEnabled) {
                    // Nếu đã bật xác nhận admin, gửi email xác nhận cho admin
                    signUpAdmin("sangvu2015dp1@gmail.com");
                } else {
                    // Nếu chưa bật xác nhận admin, đăng ký tài khoản người dùng
                    signUpUser();
                }
                //mAuth.signOut();
            }
        });

        //code back ve trang truoc
        btBack = findViewById(R.id.btback);
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Đóng SignInActivity và trở về fragment_profile
            }
        });

        // Lắng nghe sự kiện khi TextView được nhấn
        TextView tvCz = findViewById(R.id.tvCz);
        tvCz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tăng giá trị của biến đếm
                clickCount++;

                // Kiểm tra nếu đã nhấn 5 lần
                if (clickCount == 5) {
                    // Hiển thị ô đánh dấu
                    findViewById(R.id.cbProtocol).setVisibility(View.VISIBLE);
                    // Bật chức năng xác nhận admin
                    isAdminConfirmationEnabled = true;
                }
            }
        });
    }

    private void signUpUser() {
        // Hiển thị vòng loading
        showLoading();
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();
        // Kiểm tra mật khẩu có ít nhất 8 kí tự và chứa kí tự đặc biệt hay không
        if (!isPasswordValid(password)) {
            Toast.makeText(SignUpActivity.this, "Mật khẩu phải có ít nhất 8 kí tự và chứa kí tự đặc biệt", Toast.LENGTH_SHORT).show();
            // Ẩn vòng loading nếu có lỗi xảy ra
            hideLoading();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Mật khẩu và xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            // Ẩn vòng loading nếu có lỗi xảy ra
            hideLoading();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendVerificationEmail();
                            mAuth.signOut();
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(SignUpActivity.this, "Email đã được đăng ký", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(SignUpActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                            }
                            // Ẩn vòng loading nếu đăng ký thất bại
                            hideLoading();
                        }
                    }
                });
    }

    private void signUpAdmin(String userEmail) {
        // Hiển thị vòng loading
        showLoading();

        final String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Mật khẩu và xác nhận mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            hideLoading();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();
                            String userName = email;

                            Account userData = new Account(userId, email);
                            userData.setAvatarUser("https://firebasestorage.googleapis.com/v0/b/greentoon-937f6.appspot.com/o/avatars%2FGwdz6c6FAjYLKfDC3LlwaWyGWg12.jpg?alt=media&token=29a002c7-f96e-4b4c-8d96-6108ccfa6f68");
                            userData.setNameUser(userName);

                            if (email.equals(userEmail)) {
                                userData.setAdmin(true);
                            } else {
                                userData.setAdmin(false);
                            }

                            databaseReference.child(userId).setValue(userData);

                            if (email.equals(userEmail)) {
                                sendAdminConfirmationEmail(userEmail);
                            } else {
                                sendVerificationEmail();
                            }
                        } else {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Toast.makeText(SignUpActivity.this, "Email đã được đăng ký", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(SignUpActivity.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                            }
                            hideLoading();
                        }
                        mAuth.signOut();
                    }
                });
    }

    private void sendVerificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            String userEmail = user.getEmail();

            Account userData = new Account(userId, userEmail);
            userData.setAvatarUser("https://firebasestorage.googleapis.com/v0/b/greentoon-937f6.appspot.com/o/avatars%2FGwdz6c6FAjYLKfDC3LlwaWyGWg12.jpg?alt=media&token=29a002c7-f96e-4b4c-8d96-6108ccfa6f68");
            userData.setNameUser("");
            databaseReference.child(userId).setValue(userData);
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Gửi email xác nhận thành công", Toast.LENGTH_SHORT).show();
                                hideLoading();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Gửi email xác nhận thất bại", Toast.LENGTH_SHORT).show();
                                hideLoading();
                            }
                        }
                    });
        }
    }

    private boolean isPasswordValid(String password) {
        // Kiểm tra mật khẩu có ít nhất 8 kí tự và chứa kí tự đặc biệt hay không
        return password.length() >= 8 && !password.matches("[a-zA-Z0-9 ]*");
    }
    private void showLoading() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng ký...");
        progressDialog.setCancelable(false); // Không cho phép hủy bỏ
        progressDialog.show();
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void sendAdminConfirmationEmail(String adminEmail) {
        // Gửi email xác nhận cho admin
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpActivity.this, "Gửi email xác nhận cho Admin thành công", Toast.LENGTH_SHORT).show();
                                hideLoading();
                            } else {
                                Toast.makeText(SignUpActivity.this, "Gửi email xác nhận cho Admin thất bại", Toast.LENGTH_SHORT).show();
                                hideLoading();
                            }
                        }
                    });
        }
    }
}
