package com.firstvan.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends ActionBarActivity {

    private EditText fileOpenText;
    private EditText fileSaveText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileOpenText = (EditText)findViewById(R.id.openFileText);
        fileSaveText = (EditText)findViewById(R.id.saveFileText);

        File folder = new File(Environment.getExternalStorageDirectory() + "/Atalakito");
        if (!folder.exists()) {
            folder.mkdir();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Fajl kiválasztasa konvertalasra" +
                            ""),
                    FILE_SELECT_CODE);
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
                    Uri uri = data.getData();

                    String path = null;
                    path = uri.getPath();
                    fileOpenText.setText(path);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Filepicker

    public void ConvertIt(View v){
        if(fileSaveText.getText().toString().isEmpty()){
            Toast.makeText(this, "Nincs kitöltve a mentesi hely.", Toast.LENGTH_SHORT).show();
        }

        if(fileOpenText.getText().toString().isEmpty()){
            Toast.makeText(this, "Nincs konvertalando fajl.", Toast.LENGTH_SHORT).show();
        }

        String saveFile = "/sdcard/Atalakito/" + fileSaveText.getText() + ".txt";
        final ExcelConverter excelConverter = new ExcelConverter(fileOpenText.getText().toString(), saveFile, MainActivity.this, 1, 7);
        excelConverter.execute("");



    }
}
