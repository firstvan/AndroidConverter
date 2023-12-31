package com.firstvan.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelConverter extends AsyncTask<String, Integer, Integer>{

    private String excelFile;
    private String fileNormalName;
    private String saveFile;
    private Double percentage;
    private PowerManager.WakeLock mWakeLock;
    private Context context;
    ProgressDialog progressDialog;
    private int itemNoColumn;
    private int pieceNoColumn;
    private boolean appendToFile;
    private InputStream excelFileStream;


    public ExcelConverter(final InputStream eFileStream, String fileNormalName, String eFile, String sFile,
                          Context context, int itNo, int piNo, boolean appendToFile){
        this.fileNormalName = fileNormalName;
        excelFileStream = eFileStream;
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


        //POI Api
        try {
            BufferedWriter bw;

            if(appendToFile) {
                bw = new BufferedWriter(new FileWriter(saveFile, true));
            }
            else {
                File newFile = new File(saveFile);
                newFile.createNewFile();
                bw = new BufferedWriter(new FileWriter(newFile));
                bw.write("cikkszam\tdarab\n");
            }

            Workbook myWorkBook;
            if (fileNormalName.endsWith("xls")) {
                // Create a POIFSFileSystem object
                POIFSFileSystem myFileSystem = new POIFSFileSystem(excelFileStream);
                // Create a workbook using the File System
                myWorkBook = new HSSFWorkbook(myFileSystem);
            } else if (fileNormalName.endsWith("xlsx")) {
                myWorkBook = new XSSFWorkbook(excelFileStream);
            } else {
                throw new UnsupportedOperationException("Hiba ilyen fájltípus nincs támogatva");
            }

            // Get the first sheet from workbook
            Sheet mySheet = myWorkBook.getSheetAt(0);
            int allRowNum = mySheet.getLastRowNum();

            /** We now need something to iterate through the cells.**/
            Iterator<Row> rowIter = mySheet.rowIterator();
            rowIter.next();
            int actualRow = 1;
            while(rowIter.hasNext()){
                Row myRow = rowIter.next();

                Cell pieceNoCell = myRow.getCell(pieceNoColumn - 1);
                String pieceNo = "";
                if(pieceNoCell == null)
                {
                    actualRow++;
                    continue;
                }
                if (CellType.NUMERIC.equals(pieceNoCell.getCellType())) {
                    pieceNo = String.valueOf(pieceNoCell.getNumericCellValue());
                } else if (CellType.STRING.equals(pieceNoCell.getCellType())) {
                    pieceNo = pieceNoCell.getStringCellValue();
                } else {
                    pieceNo = "";
                }

                if(!pieceNo.isEmpty() && !pieceNo.contains("Tétel") && !pieceNo.contains("Összes")){
                    Cell itemNoCell = myRow.getCell(itemNoColumn - 1);
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
            e.printStackTrace();
            return -2;
        } catch (IOException e) {
            e.printStackTrace();
            return -3;
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
        if (result >= 0)
            Toast.makeText(context, "Feldolgozott termek: " + result, Toast.LENGTH_LONG).show();
        else {
            Toast.makeText(context,"Feldolgozasi hiba!", Toast.LENGTH_LONG).show();
        }
    }
}
