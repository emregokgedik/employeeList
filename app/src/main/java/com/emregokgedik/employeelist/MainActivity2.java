package com.emregokgedik.employeelist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class MainActivity2 extends AppCompatActivity {
    ImageView imageView;
    EditText nameSurnameText,departmentText,roleText,ageText;
    Button button;
    Bitmap selectedImage;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        imageView=findViewById(R.id.imageView);
        nameSurnameText=findViewById(R.id.nameSurnameText);
        departmentText=findViewById(R.id.departmentText);
        roleText=findViewById(R.id.roleText);
        ageText=findViewById(R.id.ageText);
        button=findViewById(R.id.button);

        database=this.openOrCreateDatabase("Employees",MODE_PRIVATE,null);

        Intent intent=getIntent();
        String cameFrom=intent.getStringExtra("cameFrom");
        if(cameFrom.matches("add_employee")){
            nameSurnameText.setText("");
            departmentText.setText("");
            roleText.setText("");
            ageText.setText("");
            button.setVisibility(View.VISIBLE);
            Bitmap selectImage= BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.face);
            imageView.setImageBitmap(selectImage);
        }else if(cameFrom.matches("show_employee")){
            int id=intent.getIntExtra("id",1);
            button.setVisibility(View.INVISIBLE);
            try{
                Cursor cursor=database.rawQuery("SELECT * FROM employees WHERE id=?",new String[]{String.valueOf(id)});
                int nameSurnameIx=cursor.getColumnIndex("namesurname");
                int departmentIx=cursor.getColumnIndex("department");
                int roleIx=cursor.getColumnIndex("role");
                int ageIx=cursor.getColumnIndex("age");
                int imageIx=cursor.getColumnIndex("image");
            while(cursor.moveToNext()){
                nameSurnameText.setText(cursor.getString(nameSurnameIx));
                departmentText.setText(cursor.getString(departmentIx));
                ageText.setText(cursor.getString(ageIx));
                roleText.setText(cursor.getString(roleIx));
                byte[] bytes=cursor.getBlob(imageIx);
                Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                imageView.setImageBitmap(bitmap);
            }
            cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void selectImage(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if(requestCode==1&&grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Intent intentToGallery=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==2&&resultCode==RESULT_OK&&data!=null){
            Uri imageData=data.getData();
try{
if(Build.VERSION.SDK_INT>=28){
    ImageDecoder.Source source=ImageDecoder.createSource(this.getContentResolver(),imageData);
    selectedImage=ImageDecoder.decodeBitmap(source);
    imageView.setImageBitmap(selectedImage);
}else{
    selectedImage=MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageData);
    imageView.setImageBitmap(selectedImage);
}
}catch (Exception e){
    e.printStackTrace();
}
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view){
        String nameSurname=nameSurnameText.getText().toString();
        String department=departmentText.getText().toString();
        String role=roleText.getText().toString();
        String age=ageText.getText().toString();

        Bitmap smallImage=makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray=outputStream.toByteArray();

        try{
            database=this.openOrCreateDatabase("Employees",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS employees(id INTEGER PRIMARY KEY, namesurname VARCHAR, department VARCHAR,role VARCHAR, age VARCHAR, image BLOB)");

            String sqlString="INSERT INTO employees(namesurname,department,role,age,image) VALUES(?, ?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement=database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,nameSurname);
            sqLiteStatement.bindString(2,department);
            sqLiteStatement.bindString(3,role);
            sqLiteStatement.bindString(4,age);
            sqLiteStatement.bindBlob(5,byteArray);
            sqLiteStatement.execute();

        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent=new Intent(MainActivity2.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){
        int width=image.getWidth();
        int height=image.getHeight();
        float bitmapRatio=(float) width / (float) height;
        if(bitmapRatio>1) {
            width = maximumSize;
            height=(int)(width/bitmapRatio);
        }else{
            height=maximumSize;
            width=(int)(height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }
}