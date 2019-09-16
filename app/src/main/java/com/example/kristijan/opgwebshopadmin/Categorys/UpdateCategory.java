package com.example.kristijan.opgwebshopadmin.Categorys;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kristijan.opgwebshopadmin.Base;
import com.example.kristijan.opgwebshopadmin.Common.CameraIntent;
import com.example.kristijan.opgwebshopadmin.Common.ImageRotation;
import com.example.kristijan.opgwebshopadmin.Model.Category;
import com.example.kristijan.opgwebshopadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class UpdateCategory extends Base {

    FirebaseDatabase database;
    DatabaseReference category_add;

    FirebaseStorage storage;
    StorageReference storageReference;

    ProgressDialog mdialog;
    String link,categoryIdUpdate;
    Category NewCategory,SelectedCategory;
    ImageView Preview;

    FButton select, save,camera;
    EditText CategoryName;
    private String mCurrentPhotoPath;
    int orientation;

    Bitmap bmp;
    Uri selectedImageUri;
    File imgFile;

    private final int PICK_IMAGE = 99;
    private static final int REQUEST_CAPTURE_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_category);
        getSupportActionBar().setTitle(R.string.update_categoryActivity);

        database = FirebaseDatabase.getInstance();
        category_add = database.getReference("category");

        storage=FirebaseStorage.getInstance();
        storageReference=storage.getReference("images/");

        select = (FButton) findViewById(R.id.btn_select);
        camera = (FButton) findViewById(R.id.btn_camera);
        save = (FButton) findViewById(R.id.btn_Add);
        CategoryName = (EditText) findViewById(R.id.txt_cat_title);
        Preview = (ImageView) findViewById(R.id.img_cat);

        mdialog = new ProgressDialog(UpdateCategory.this);
        mdialog.setMessage(getResources().getString(R.string.wait));

        if (getIntent() !=null)
        {
            categoryIdUpdate = getIntent().getStringExtra("categoryIdUpdate");
        }

        mdialog.show();

        category_add.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //FirebaseUser Auth_user = FirebaseAuth.getInstance().getCurrentUser();
                //Provjera dali korisnik postoji u bazi
                if (dataSnapshot.child(categoryIdUpdate).exists()) {
                    mdialog.dismiss();
                    SelectedCategory = dataSnapshot.child(categoryIdUpdate).getValue(Category.class);
                    CategoryName.setText(SelectedCategory.getName());
                    link=SelectedCategory.getImage();
                    Glide.with(getBaseContext()).load(SelectedCategory.getImage()).thumbnail(0.5f).into(Preview);
                }
                else{
                    mdialog.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pick_from_galery();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //openCameraIntent ();
                CameraIntent cameraIntent=new CameraIntent(getBaseContext());
                Intent cam=cameraIntent.openCameraIntent();
                mCurrentPhotoPath=cameraIntent.getmCurrentPhotoPath();
                startActivityForResult(cam, cameraIntent.getRequestCaptureImage());
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewCategory=new Category(link,CategoryName.getText().toString());

                    //Dodavanje nove kategorije u bazu
                    category_add.child(categoryIdUpdate).setValue(NewCategory);
                    Toast.makeText(UpdateCategory.this, R.string.category_updated, Toast.LENGTH_SHORT).show();
                    finish();
            }
        });
    }

    private void pick_from_galery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            imgFile = new File(mCurrentPhotoPath);
            if (imgFile.exists()) {
                selectedImageUri=Uri.fromFile(imgFile);

                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    bmp=ImageRotation.rotateImageIfRequired(bmp,selectedImageUri);
                    upload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK)
        {
            if(data !=null)
            {
                selectedImageUri=data.getData();

                try {
                    bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                    upload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void upload() throws IOException {
        final ProgressDialog save= new ProgressDialog(this);
        save.setMessage("Uploading...");
        save.show();
        String image_name=UUID.randomUUID().toString();
        final StorageReference imageFolder=storageReference.child("images/"+image_name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();

        imageFolder.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                save.dismiss();
                Toast.makeText(UpdateCategory.this, "Uploaded", Toast.LENGTH_SHORT).show();
                imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        link=uri.toString();
                        Glide.with(getBaseContext()).load(uri.toString()).thumbnail(0.5f).into(Preview);

                        if(imgFile!=null)
                        {
                            imgFile.delete();
                        }

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                save.dismiss();
                Toast.makeText(UpdateCategory.this, R.string.wrong, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
