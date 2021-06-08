package com.emregokgedik.employeelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> nameSurnameArray;
    ArrayList<Integer> idArray;
    ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listView);
        nameSurnameArray=new ArrayList<String>();
        idArray=new ArrayList<Integer>();

        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,nameSurnameArray);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent=new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("id",idArray.get(position));
            intent.putExtra("cameFrom","show_employee");
            startActivity(intent);
            }
        });
        getData();
    }

    public void getData(){

        try{
            SQLiteDatabase database=this.openOrCreateDatabase("Employees",MODE_PRIVATE,null);
            Cursor cursor=database.rawQuery("SELECT * FROM employees",null);

            int nameSurnameIx=cursor.getColumnIndex("namesurname");
            int idIx=cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                nameSurnameArray.add(cursor.getString(nameSurnameIx));
                idArray.add(cursor.getInt(idIx));
            }
            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.add_employee,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==(R.id.add_employee)){
        Intent intent=new Intent(this, MainActivity2.class);
        intent.putExtra("cameFrom","add_employee");
        startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}