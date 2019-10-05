package com.firstvan.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

import java.io.*;
import java.util.Iterator;

public class ExcelConverter extends AsyncTask<String, Integer, Integer>{

    private String excelFile;
    private String saveFile;
    private Double percentage;
    private PowerManager.WakeLock mWakeLock;
    private Context context;
    ProgressDialog progressDialog;
    private int itemNoColumn;
    private int pieceNoColumn;
    private boolean appendToFile;


    public ExcelConverter(String eFile, String sFile, Context context, int itNo, int piNo, boolean appendToFile){
        excelFile = eFile;
        saveFile = sFile;
        percentage = 0.0;
        this.context = context;
        itemNoColumn = itNo;
        pieceNoColumn = piNo;
        this.appendToFile = appendToFile;
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

    public int getItemNoColumn() {
        return itemNoColumn;
    }

    public void setItemNoColumn(int itemNoColumn) {
        this.itemNoColumn = itemNoColumn;
    }

    public int getPieceNoColumn() {
        return pieceNoColumn;
    }

    public void setPieceNoColumn(int pieceNoColumn) {
        this.pieceNoColumn = pieceNoColumn;
    }

    public boolean isAppendToFile() {
        return appendToFile;
    }

    public void setAppendToFile(boolean appendToFile) {
        this.appendToFile = appendToFile;
    }

    @Override
    protected Integer doInBackground(String... params) {
        int counter = 0;

        File inputWorkbook = new File(excelFile);
        if(inputWorkbook.exists()){

            //POI Api

            FileInputStream myInput = null;
            try {
                BufferedWriter bw;

                if(appendToFile) {
                    bw = new BufferedWriter(new FileWriter(saveFile, true));
                }
                else {
                    bw = new BufferedWriter(new FileWriter(new File(saveFile)));
                    bw.write("cikkszam\tdarab\n");
                }
                myInput = new FileInputStream(excelFile);
                // Create a POIFSFileSystem object
                POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

                // Create a workbook using the File System
                HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

                // Get the first sheet from workbook
                HSSFSheet mySheet = myWorkBook.getSheetAt(0);
                int allRowNum = mySheet.getLastRowNum();

                /** We now need something to iterate through the cells.**/
                Iterator<Row> rowIter = mySheet.rowIterator();
                rowIter.next();
                int actualRow = 1;
                while(rowIter.hasNext()){
                    HSSFRow myRow = (HSSFRow) rowIter.next();

                    HSSFCell pieceNoCell = myRow.getCell(pieceNoColumn - 1);
                    String pieceNo = "";
                    if(pieceNoCell == null)
                    {
                        actualRow++;
                        continue;
                    }
                    switch(pieceNoCell.getCellType()){
                        case Cell.CELL_TYPE_NUMERIC:
                            pieceNo = String.valueOf(pieceNoCell.getNumericCellValue());
                            break;
                        case Cell.CELL_TYPE_STRING:
                            pieceNo = pieceNoCell.getStringCellValue();
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            pieceNo = "";
                            break;
                        case Cell.CELL_TYPE_BOOLEAN:
                            pieceNo = "";
                            break;
                        case Cell.CELL_TYPE_ERROR:
                            pieceNo = "";
                            break;
                        case Cell.CELL_TYPE_FORMULA:
                            pieceNo = "";
                            break;
                    }

                    if(!pieceNo.isEmpty() && !pieceNo.contains("Tétel") && !pieceNo.contains("Összes")){
                        HSSFCell itemNoCell = myRow.getCell(itemNoColumn - 1);
                        if(itemNoCell == null){
                            continue;
                        }
                        String itemNo = itemNoCell.getStringCellValue();

                        if (pieceNo.toLowerCase().contains(" db")){
                            pieceNo = pieceNo.substring(0, pieceNo.toLowerCase().indexOf(" db"));
                        }
                        else if(pieceNo.toLowerCase().contains("db")){
                            pieceNo = pieceNo.substring(0, pieceNo.toLowerCase().indexOf("db"));
                        } else {
                            double dszam = Double.parseDouble(pieceNo);
                            pieceNo = String.format("%.0f", dszam);
                        }

                        bw.write(itemNo + "\t" + pieceNo + "\n" );
                        bw.flush();
                        counter++;
                    }

                    percentage = (actualRow / ((double) allRowNum)) * 100;
                    publishProgress(percentage.intValue());

                    actualRow++;
                }

                bw.flush();
                bw.close();
            } catch (FileNotFoundException e) {
                return -2;
            } catch (IOException e) {
                return -3;
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
            Toast.makeText(context, "Feldolgozott termek: " + result, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(context,"Feldolgozasi hiba!", Toast.LENGTH_LONG).show();
    }
}
