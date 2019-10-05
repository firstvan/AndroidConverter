package com.firstvan.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final int READ_STORAGE = 0;
    private EditText fileOpenText;
    private EditText fileSaveText;
    private EditText itemCollNo;
    private EditText pieceCollNo;
    private CheckBox check;
    private Intent myFileChooser;
    private InputStream excelFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE);
        }

        fileOpenText = (EditText)findViewById(R.id.openFileText);
        fileSaveText = (EditText)findViewById(R.id.saveFileText);
        itemCollNo = (EditText)findViewById(R.id.ItemNumberColl);
        pieceCollNo = (EditText)findViewById(R.id.PieceNumberColl);
        check = (CheckBox)findViewById(R.id.appendToFile);

        File folder = new File(Environment.getExternalStorageDirectory() + "/Atalakito");
        if (!folder.exists()) {
            folder.mkdir();
        }

        itemCollNo.setText("1");
        pieceCollNo.setText("7");

       try
        {
            Scanner scanner = new Scanner(new File(Environment.getExternalStorageDirectory() + "/Atalakito/META.txt"));
            int first = scanner.nextInt();
            int second = scanner.nextInt();
            itemCollNo.setText(String.valueOf(first));
            pieceCollNo.setText(String.valueOf(second));
            scanner.close();
        }
        catch (Exception e)
        {
            File file = new File(Environment.getExternalStorageDirectory() + "/Atalakito/META.txt");
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write("1 7");
                bw.flush();
                bw.close();
            } catch (IOException e1) {
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void OpenFileDialog_OnClick(View v)
    {
        showFileChooser();
    }

    //File picker

    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        myFileChooser = new Intent(Intent.ACTION_GET_CONTENT);
        myFileChooser.setType("*/*");

        try {
            startActivityForResult(myFileChooser, FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    if (data.getData() != null) {
                        try {
                            excelFile = getContentResolver().openInputStream(data.getData());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        fileOpenText.setText(data.getData().getPath());
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Filepicker

    public void ConvertIt(View v){
        File file = new File(Environment.getExternalStorageDirectory() + "/Atalakito/META.txt");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(itemCollNo.getText() + " " + pieceCollNo.getText());
            bw.flush();
            bw.close();
        } catch (IOException e1) {
            Toast.makeText(this, "Hiba", Toast.LENGTH_LONG);
        }

        if(fileSaveText.getText().toString().isEmpty()){
            Toast.makeText(this, "Nincs kitöltve a mentesi hely.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(fileOpenText.getText().toString().isEmpty()){
            Toast.makeText(this, "Nincs konvertalando fajl.", Toast.LENGTH_SHORT).show();
            return;
        }

        String saveFile = Environment.getExternalStorageDirectory().getPath() +
                "/Atalakito/" + fileSaveText.getText() + ".txt";

        int item = Integer.parseInt(itemCollNo.getText().toString());
        int piece = Integer.parseInt(pieceCollNo.getText().toString());
        boolean c = check.isChecked();
        final ExcelConverter excelConverter = new ExcelConverter(excelFile,
                fileOpenText.getText().toString(), saveFile, MainActivity.this, item, piece, c);
        excelConverter.execute("");
        fileOpenText.setText("");
    }
}
