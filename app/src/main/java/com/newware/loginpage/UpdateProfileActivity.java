package com.newware.loginpage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class UpdateProfileActivity extends AppCompatActivity
{
    private static int PICK_CODE = 666;
    ProgressDialog progressDialog2;
    private EditText up_user_name, up_user_email, up_user_mob;
    private ImageView profile_pic;
    private Button btn_update;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Uri imgUriPath;

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

                profile_pic.setImageBitmap(bitmap);//setting image


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
        setContentView(R.layout.activity_update_profile);
        getreference();

        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();

        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();//root of storage


        databaseReference = firebaseDatabase.getReference(firebaseAuth.getUid()); // only auth users id
        // Toast.makeText(this, "user : "+firebaseUser, Toast.LENGTH_SHORT).show();

        if (firebaseUser != null)
        {//data changing

            databaseReference.addValueEventListener(new ValueEventListener()
            {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    User currentUser = dataSnapshot.getValue(User.class);//get value

                    if (currentUser == null)
                    {

                        Toast.makeText(UpdateProfileActivity.this, "Null Hai", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    up_user_email.setText(currentUser.getEmail());
                    up_user_name.setText(currentUser.getName());
                    up_user_mob.setText(currentUser.getMobile());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    Toast.makeText(UpdateProfileActivity.this, "Database error : " + databaseError.getCode(),
                            Toast.LENGTH_SHORT).show();
                }
            });
            storageReference.child(firebaseAuth.getUid()).child("images/profile_pic").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
            {
                @Override
                public void onSuccess(Uri uri)
                {
                    Picasso.get().load(uri).fit().centerCrop().into(profile_pic);
                }
            });
        }

        //image clicked
        profile_pic.setOnClickListener(new View.OnClickListener()
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

        btn_update.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                progressDialog2 = new ProgressDialog(UpdateProfileActivity.this);
                progressDialog2.setCancelable(false);
                progressDialog2.setMessage("Updating Please Wait");
                progressDialog2.show();
                updateData();
                progressDialog2.dismiss();
                UpdateProfileActivity.super.onBackPressed();
            }

        });


    }

    private void updateData()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            progressDialog2.create();
        }

        //text data upload
        String updated_name = up_user_name.getText().toString().trim();
        String updated_email = up_user_email.getText().toString().trim();
        String updated_mob = up_user_mob.getText().toString().trim();
        User userpdated = new User(updated_name, updated_email, updated_mob);

        databaseReference.setValue(userpdated);


        //media upload

        StorageReference imgRefe = storageReference.child(firebaseAuth.getUid()).
                child("images").child("profile_pic");

        if (imgUriPath != null)
        {
            UploadTask uploadTask = imgRefe.putFile(imgUriPath);

            uploadTask.addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    Toast.makeText(UpdateProfileActivity.this,
                            "Image upload fail", Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(UpdateProfileActivity.this,
                                        "Image uploaded ", Toast.LENGTH_SHORT).show();
                            } else
                            {
                                Toast.makeText(UpdateProfileActivity.this,
                                        "Image uploaded failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }


    private void getreference()
    {
        up_user_name = findViewById(R.id.et_user_name_up);
        up_user_email = findViewById(R.id.et_user_email_up);
        up_user_mob = findViewById(R.id.et_user_mobile_up);
        profile_pic = findViewById(R.id.iv_user_img_upd);

        btn_update = findViewById(R.id.btn_update);
    }
}
