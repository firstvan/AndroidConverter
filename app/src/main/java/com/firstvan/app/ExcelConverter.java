package com.firstvan.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;
//import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
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


    public ExcelConverter(String eFile, String sFile, Context context, int itNo, int piNo){
        excelFile = eFile;
        saveFile = sFile;
        percentage = 0.0;
        this.context = context;
        itemNoColumn = itNo;
        pieceNoColumn = piNo;
        Toast.makeText(context, String.valueOf(pieceNoColumn), Toast.LENGTH_LONG);
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
            /*Workbook w;
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
                    if (!db.getContents().isEmpty() && !db.getContents().contains("Tetel") && !db.getContents().contains("Osszes")) {
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
            }*/

            //POI Api

            FileInputStream myInput = null;
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(saveFile)));
                bw.write("Cikkszam\tdarab\n");

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
