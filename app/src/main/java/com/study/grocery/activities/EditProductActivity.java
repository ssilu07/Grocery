package com.study.grocery.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.study.grocery.Constants;
import com.study.grocery.R;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {

    //ui views
    private ImageButton backBtn;
    private ImageView productIconIv;
    private EditText titleEt, descriptionEt;
    private TextView categoryTv, quantityEt, priceEt, discountPriceEt, discountedNoteEt;
    private SwitchCompat discountSwitch;
    private Button updateProductBtn;

    private String productId;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    //IMAGE PICK CONSTANTS
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //PERMISSION arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;
    //image picked uri
    private Uri image_uri;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        //init ui views
        backBtn = findViewById(R.id.backBtn);
        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        categoryTv = findViewById(R.id.categoryTv);
        quantityEt = findViewById(R.id.quantityEt);
        priceEt = findViewById(R.id.priceEt);
        discountPriceEt = findViewById(R.id.discountPriceEt);
        discountedNoteEt = findViewById(R.id.discountedNoteEt);
        updateProductBtn = findViewById(R.id.updateProductBtn);
        discountSwitch = findViewById(R.id.discountSwitch);

        //get id of the product from intent
        productId = getIntent().getStringExtra("productId");

        //unchecked hide discontPriceEt, discontNoteEt
        discountPriceEt.setVisibility(View.GONE);
        discountedNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        loadProductDetails(); //to set on views

        //setup progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        //init permissions
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //if discontSwitch is checked: show discountPriceEt, discontNoteEt | if discountSwitch is not checked: hide discontPriceEt,discontNoteEt
        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //checked,show discountPriceEt, discontNoteEt
                    discountPriceEt.setVisibility(View.VISIBLE);
                    discountedNoteEt.setVisibility(View.VISIBLE);

                }
                else {
                    //unchecked hide discontPriceEt, discontNoteEt
                    discountPriceEt.setVisibility(View.GONE);
                    discountedNoteEt.setVisibility(View.GONE);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show dialog to pick image
                showImagePickDialog();
            }
        });
        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoryDialog();
            }
        });
        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Flow
                //1) Validate data
                //2) update data to db
                inputData();
            }
        });
    }

    private void loadProductDetails() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        //get data
                        String productId = ""+snapshot.child("productId").getValue();
                        String productTitle = ""+snapshot.child("productTitle").getValue();
                        String productDescription = ""+snapshot.child("productDescription").getValue();
                        String productCategory = ""+snapshot.child("productCategory").getValue();
                        String productQuantity = ""+snapshot.child("productQuantity").getValue();
                        String productIcon = ""+snapshot.child("productIcon").getValue();
                        String originalPrice = ""+snapshot.child("originalPrice").getValue();
                        String discountPrice = ""+snapshot.child("discountPrice").getValue();
                        String discountNote = ""+snapshot.child("discountNote").getValue();
                        String discountAvailable = ""+snapshot.child("discountAvailable").getValue();
                        String timestamp = ""+snapshot.child("timestamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        //set data to views
                        if (discountAvailable.equals("true")){
                            discountSwitch.setChecked(true);

                            discountPriceEt.setVisibility(View.VISIBLE);
                            discountedNoteEt.setVisibility(View.VISIBLE);
                        }
                        else {
                            discountSwitch.setChecked(false);

                            discountPriceEt.setVisibility(View.GONE);
                            discountedNoteEt.setVisibility(View.GONE);
                        }
                        titleEt.setText(productId);
                        descriptionEt.setText(productDescription);
                        categoryTv.setText(productCategory);
                        discountedNoteEt.setText(discountNote);
                        quantityEt.setText(productQuantity);
                        priceEt.setText(originalPrice);
                        discountPriceEt.setText(discountPrice);

                        try {
                            Picasso.get().load(productIcon).placeholder(R.drawable.ic_shop_white).into(productIconIv);
                        }
                        catch (Exception e){
                            productIconIv.setImageResource(R.drawable.ic_shop_white);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private String productTitle,productDescription,productCategory,productQuantity,originalPrice,discountPrice,discountNote;
    private boolean discountAvailable = false;

    private void inputData() {
        //input data
        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();
        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked(); //true/false

        //2) Validate data
        if (TextUtils.isEmpty(productTitle)){
            Toast.makeText(this, "Title is required...", Toast.LENGTH_SHORT).show();
            return; // don't proceed Further
        }
        if (TextUtils.isEmpty(productCategory)){
            Toast.makeText(this, "Category is required...", Toast.LENGTH_SHORT).show();
            return; // don't proceed Further
        }
        if (TextUtils.isEmpty(originalPrice)){
            Toast.makeText(this, "Price is required...", Toast.LENGTH_SHORT).show();
            return; // don't proceed Further
        }
        if (discountAvailable){
            //product is with Discount
            discountPrice = discountPriceEt.getText().toString().trim();
            discountNote = discountedNoteEt.getText().toString().trim();
            if (TextUtils.isEmpty(discountPrice)){
                Toast.makeText(this, "Discount Price is required...", Toast.LENGTH_SHORT).show();
                return; //don't proceed further
            }

        }
        else {
            //product is without discount
            discountPrice = "0";
            discountNote = "";
        }
        updateProduct();
    }

    private void updateProduct() {
        //show progress

        progressDialog.setMessage("Updating Product...");
        progressDialog.show();

        final String timestamp = ""+System.currentTimeMillis();

        if (image_uri == null){
            //upload without image
            //setup data to upload
            HashMap<String,Object> hashMap = new HashMap<>();
            hashMap.put("productTitle",""+productTitle);
            hashMap.put("productDescription",""+productDescription);
            hashMap.put("productCategory",""+productCategory);
            hashMap.put("productQuantity",""+productQuantity);
            hashMap.put("discountNote",""+discountNote);
            hashMap.put("originalPrice",""+originalPrice);
            hashMap.put("discountPrice",""+discountPrice);
            hashMap.put("discountAvailable",""+discountAvailable);

            //update to db

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            reference.child(firebaseAuth.getUid()).child("Products").child(productId).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //update sucess
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //update failed
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        else {
            //update with image

            //first upload image to storage

            //image name and path of firebase storage
            String filePathAndName = "product_images/" + "" + productId;//override previous image using same id
            //uploaded image

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //image uploaded
                            //get url of uploaded image
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){

                                //setup data to hashmap to update
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("productTitle",""+productTitle);
                                hashMap.put("productDescription",""+productDescription);
                                hashMap.put("productCategory",""+productCategory);
                                hashMap.put("productIcon","" + downloadImageUri);
                                hashMap.put("productQuantity",""+productQuantity);
                                hashMap.put("discountNote",""+discountNote);
                                hashMap.put("originalPrice",""+originalPrice);
                                hashMap.put("discountPrice",""+discountPrice);
                                hashMap.put("discountAvailable",""+discountAvailable);

                                //update to db
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                                reference.child(firebaseAuth.getUid()).child("Products").child(productId).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //update sucess
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //update failed
                                                progressDialog.dismiss();
                                                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failed uploading image
                            progressDialog.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }



    private void categoryDialog() {
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productcategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //get picked category
                        String category = Constants.productcategories[which];

                        //set picked gallery
                        categoryTv.setText(category);
                    }
                })
                .show();
    }


    private void showImagePickDialog() {
        //option to display in dialog
        String[] options = {"Camera", "Gallery"};
        //dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {
                                //camera permission allowed
                                pickFromCamera();
                            } else {
                                //not allowed ,request
                                requestCameraPermission();

                            }
                        } else {
                            //gallery clicked
                            if (checkStoragePermission()) {
                                //storage permission allowed
                                pickFromGallery();

                            } else {
                                //not allowed,request
                                requestStoragePermission();

                            }
                        }
                    }
                })
                .show();

    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {

        //intent to pick high quality image

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);

        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);


        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //handle permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted && storageAccepted) {
                        //permission allowed
                        pickFromCamera();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Camera & storage Permissions are Necessary ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (storageAccepted) {
                        //permission allowed
                        pickFromGallery();
                    } else {
                        //permission denied
                        Toast.makeText(this, "Storage Permissions is Necessary ...", Toast.LENGTH_SHORT).show();
                    }
                }
            }


            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    //image pick result
    @Override
    protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){

        if (resultCode == RESULT_OK) {

            if (requestCode == IMAGE_PICK_GALLERY_CODE) {

                //save Picked image uri
                image_uri = data.getData();
                //set to imageview
                productIconIv.setImageURI(image_uri);
            } else if (requestCode == IMAGE_PICK_CAMERA_CODE) {

                //set to imageview
                productIconIv.setImageURI(image_uri);
            }

        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}