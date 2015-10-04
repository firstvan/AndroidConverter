package com.firstvan.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class ExcelConverter extends AsyncTask<String, Integer, Integer>{

    private String excelFile;
    private String saveFile;
    private Double percentage;
    private PowerManager.WakeLock mWakeLock;
    private Context context;
    ProgressDialog progressDialog;


    public ExcelConverter(String eFile, String sFile, Context context){
        excelFile = eFile;
        saveFile = sFile;
        percentage = 0.0;
        this.context = context;
    }

    public String getExcelFile() {
        return excelFile;
    }

    public void setExcelFile(String excelFile) {
        this.excelFile = excelFile;
    }

    public String getSaveFile() {
        return saveFile;
    }

    public void setSaveFile(String saveFile) {
        this.saveFile = saveFile;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    @Override
    protected Integer doInBackground(String... params) {
        int counter = 0;

        File inputWorkbook = new File(excelFile);
        if(inputWorkbook.exists()){
            Workbook w;
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(saveFile)));
                bw.write("Cikkszam\tdarab\n");
                w = Workbook.getWorkbook(inputWorkbook);
                // Get the first sheet
                Sheet sheet = w.getSheet(0);
                // Loop over column and lines
                for (int j = 1; j < sheet.getRows(); j++) {
                    Cell ean = sheet.getCell(0, j);
                    Cell db = sheet.getCell(6, j);
                    if (!db.getContents().isEmpty() && !db.getContents().contains("Tétel") && !db.getContents().contains("Összes")) {
                        String darab = db.getContents();
                        if(darab.toLowerCase().contains(" db")){
                            darab = darab.substring(0, darab.toLowerCase().indexOf(" db"));
                        }
                        else if(darab.toLowerCase().contains("db")){
                            darab = darab.substring(0, darab.toLowerCase().indexOf("db"));
                        }

                        bw.write(ean.getContents() + "\t" + darab + "\n" );
                        counter++;
                    }
                    percentage = (j / ((double) sheet.getRows())) * 100;
                    publishProgress(percentage.intValue());
                }

                bw.flush();
                bw.close();

            } catch (Exception e){
                return -1;
            }
        }

        return counter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Konvertalas folyamatban: ");
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
        });

        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        // if we get here, length is known, now set indeterminate to false
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer result) {
        mWakeLock.release();
        progressDialog.dismiss();
        if (result != null)
            Toast.makeText(context, "Feldolgozott termék: " + result, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context,"Feldolgozási hiba!", Toast.LENGTH_LONG).show();
    }
}
