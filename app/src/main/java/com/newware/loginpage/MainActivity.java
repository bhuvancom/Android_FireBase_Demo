package com.newware.loginpage;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
{
    private TextInputEditText md_et_user, md_et_pass;
    private Button btn_login;
    private TextView tv_goto_register, tv_attempts, tv_forget;
    private int attempts = 5;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getReference(); // references of Widgets


        progressDialog = new ProgressDialog(this); //instance of progress


        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser(); //checks if a user is already logged in

        if ((firebaseUser != null))
        {
            finish();
            startActivity(new Intent(MainActivity.this, LoggedInActivity.class));
        }


        //on login btn click
        btn_login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String user_email = md_et_user.getText().toString().trim();
                String user_pass = md_et_pass.getText().toString();
                if (user_email.isEmpty() || user_pass.isEmpty())
                {
                    md_et_user.setError("Fill it");
                    md_et_pass.setError("Fill it");
                    return;
                }

                //passing value
                validatingLogin(user_email, user_pass);
            }
        });


        //on text view Register click
        tv_goto_register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivity.this, Registeration_Activity.class));
            }
        });


        tv_forget.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final AlertDialog.Builder forgetDialog = new AlertDialog.Builder(MainActivity.this);

                LayoutInflater layoutInflater = getLayoutInflater();

                final View dialogView = layoutInflater.inflate(R.layout.alert, null);

                forgetDialog.setCancelable(false);
                forgetDialog.setView(dialogView);

                Button btn_send = (Button) dialogView.findViewById(R.id.btn_reset_password);
                Button btn_reset_cancel = (Button) dialogView.findViewById(R.id.btn_reset_cancel);
                final EditText reset_email = (EditText) dialogView.findViewById(R.id.et_email_to_reset);


                final AlertDialog thisWillShowDialog = forgetDialog.create();
                btn_send.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String email_to_reset = reset_email.getText().toString().trim();
                        if (email_to_reset.isEmpty())
                        {
                            Toast.makeText(MainActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                        } else
                        {
                            final ProgressDialog pd = new ProgressDialog(MainActivity.this);
                            pd.setMessage("Wait Password Reset Mail Is Being Sent..");
                            pd.setCancelable(false);
                            pd.show();
                            firebaseAuth.sendPasswordResetEmail(email_to_reset)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                pd.dismiss();
                                                Toast.makeText(MainActivity.this, "Password Reset Mail Sent",
                                                        Toast.LENGTH_SHORT).show();
                                                thisWillShowDialog.dismiss();
                                            } else
                                            {
                                                pd.dismiss();
                                                Toast.makeText(MainActivity.this, "Error Email Not Registered or Server Error",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });

                        }
                    }
                });
                btn_reset_cancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        thisWillShowDialog.dismiss();
                    }
                });
                thisWillShowDialog.show();
            }
        });

    }


    // checking if user has registered
    private void validatingLogin(String email, String pass)
    {
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Wait Logging is in Progress");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            progressDialog.dismiss();
                            //checkEmailVerified();
                            finish();
                            startActivity(new Intent(MainActivity.this, LoggedInActivity.class));
                        } else
                        {
                            attempts--;
                            if (attempts == 0)
                            {
                                btn_login.setEnabled(false);
                            }

                            tv_attempts.setText(String.format("Number of Attempts Remaining : %d", attempts));
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Either Email or Password is Incorrect");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }
                            }).show();
                        }
                    }
                });
    }


    // check if user has verified its email
    private void checkEmailVerified()
    {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean email_flag = firebaseUser.isEmailVerified();
        if (email_flag)//if true
        {
            finish();
            startActivity(new Intent(this, LoggedInActivity.class));
            // ActivityCompat.finishAffinity(this);
        } else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(true)
                    .setMessage("Email Verification Pending \nFirst Verify and then login.").show();
            firebaseAuth.signOut();
        }
    }


    //refereces
    private void getReference()
    {
        md_et_user = (TextInputEditText) findViewById(R.id.et_User);
        md_et_pass = (TextInputEditText) findViewById(R.id.et_Password);

        tv_goto_register = (TextView) findViewById(R.id.tv_register);

        tv_attempts = (TextView) findViewById(R.id.tv_attempt);

        tv_forget = (TextView) findViewById(R.id.tv_forget);

        btn_login = (Button) findViewById(R.id.btn_login);

        relativeLayout = (RelativeLayout) findViewById(R.id.rl_forget_email);
    }

}
