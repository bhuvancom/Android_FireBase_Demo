package com.newware.loginpage;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Registeration_Activity extends AppCompatActivity
{
    private static int PICK_CODE = 666;
    Uri imgUriPath;
    String name;
    String password;
    String email_val;
    String mobile;
    private EditText et_user_name, et_user_password, et_userEmail, et_user_mobile;
    private Button btn_register;
    private TextView tv_goto_login;
    private ImageView iv_profile_pic;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder builder;
    private FirebaseAuth firebaseAuth;

    private FirebaseStorage firebaseStorage;

    private StorageReference storageReference;


    // to get data send by gallery with req code 666
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if ((requestCode == PICK_CODE) && (resultCode == RESULT_OK) && (data.getData() != null))
        {
            System.out.println("we are in if cond now try block run");
            imgUriPath = data.getData();// this is full path of image

            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUriPath);// got image in bitmap

                iv_profile_pic.setImageBitmap(bitmap);//setting image


            } catch (IOException e)
            {

                e.printStackTrace();
            }

        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration_);
        getReferences();


        firebaseAuth = FirebaseAuth.getInstance(); // getting instance of firebase auth
        firebaseStorage = FirebaseStorage.getInstance();


        iv_profile_pic.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();

                intent.setType("image/*"); //application/*  //audio/mp3 or *
                intent.setAction(Intent.ACTION_GET_CONTENT); //to get contetn


                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_CODE);


            }
        });


        btn_register.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (validating())
                {
                    //todo register user
                    String user_email = et_userEmail.getText().toString().trim();
                    String user_pass = et_user_password.getText().toString();
                    progressDialog = new ProgressDialog(Registeration_Activity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Registering... Please Wait");
                    progressDialog.show();

                    firebaseAuth.createUserWithEmailAndPassword(user_email, user_pass)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    if (task.isSuccessful())
                                    {


                                        sendUserDataToFireBaseDataBase();
                                        //sendEmailVerification();
                                        progressDialog.dismiss();

                                        builder = new AlertDialog.Builder(Registeration_Activity.this);
                                        builder.setMessage("Registered , Click Ok to Continue");
                                        builder.setCancelable(false);
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                firebaseAuth.signOut();
                                                startActivity(new Intent(Registeration_Activity.this, MainActivity.class));
                                            }
                                        }).show();


                                    } else
                                    {
                                        progressDialog.dismiss();
                                        builder = new AlertDialog.Builder(Registeration_Activity.this);
                                        builder.setMessage("Registration Failed , May be You have already Registered !! \n" +
                                                " Try Login Using This Email");
                                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                startActivity(new Intent(Registeration_Activity.this,
                                                        MainActivity.class));
                                                ActivityCompat.finishAffinity(Registeration_Activity.this);
                                            }
                                        }).show();

                                    }
                                }
                            });
                }
            }
        });

        tv_goto_login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(Registeration_Activity.this, MainActivity.class));

                ActivityCompat.finishAffinity(Registeration_Activity.this);
            }
        });
    }


    private boolean validating()
    {
        boolean result = false;
        name = et_user_name.getText().toString().trim();
        password = et_user_password.getText().toString().trim();
        email_val = et_userEmail.getText().toString().trim();
        mobile = et_user_mobile.getText().toString();

        if (name.isEmpty() || password.isEmpty() || email_val.isEmpty() || mobile.isEmpty() || imgUriPath == null)
        {
            et_user_name.setError("Fill It ");
            et_userEmail.setError("Fill It ");
            et_user_password.setError("Fill It ");
            et_user_mobile.setError("Fill It ");
            Toast.makeText(this, "Check if u selected image ?", Toast.LENGTH_SHORT).show();
        } else
        {

            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            CharSequence inputStr = email_val;

            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(inputStr);

            if (!(matcher.matches()))
                et_userEmail.setError("Check Email");

            else if (password.length() < 8)
                et_user_password.setError("Password is too short long");

            else if (name.length() < 4)
                et_user_name.setError("Name is too short");

            else if (mobile.length() < 10)
                et_user_mobile.setError("10 digits required");

            else
                result = true;
        }
        if (email_val.equals("parulpanday4@gmail.com") || email_val.equals("Parulpanday4@gmail.com"))
        {
            Toast.makeText(this, "hey parul ", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    // to send mail for verification of email
    private void sendEmailVerification()
    {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null)
        {
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(Registeration_Activity.this,
                                "Verification Mail sent to your mail", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();

                        finish();
                        startActivity(new Intent(Registeration_Activity.this, MainActivity.class));

                    } else
                    {
                        Toast.makeText(Registeration_Activity.this,
                                "Mail Can't Be sent , check Email address",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
        }

    }


    // sending data to database
    private void sendUserDataToFireBaseDataBase()
    {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();//getting instance of FireBaseDataBase

        DatabaseReference databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid());// Gettng Reference of DataBase with user id

        // Creating new user node, which returns the unique key value
        // new user node would be /users/$userid/

        User userProfile = new User(name, email_val, mobile);// text data to upload
        databaseReference.setValue(userProfile);

        storageReference = FirebaseStorage.getInstance().getReference();
        //media data
        StorageReference imgRefe = storageReference.child(firebaseAuth.getUid()).
                child("images").child("profile_pic");//creating users folder

        // uid/images/profile_pic/$img
        UploadTask uploadTask = imgRefe.putFile(imgUriPath);
        uploadTask.addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(Registeration_Activity.this,
                        "Image upload fail", Toast.LENGTH_SHORT).show();
            }
        })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(Registeration_Activity.this,
                                "Image uploaded", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void getReferences()
    {
        et_user_name = findViewById(R.id.et_User_name);
        et_user_password = findViewById(R.id.et_user_pass);
        et_userEmail = findViewById(R.id.et_user_email);
        et_user_mobile = findViewById(R.id.et_user_mob);
        btn_register = findViewById(R.id.btn_register);

        iv_profile_pic = findViewById(R.id.iv_user_img);

        tv_goto_login = findViewById(R.id.tv_login);
    }
}
