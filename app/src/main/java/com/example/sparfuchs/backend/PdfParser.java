package com.example.sparfuchs.backend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfParser {

    public static List<TransactionEntity> extractTransactionsFromPDF(Context context, Uri pdfUri) {
        List<TransactionEntity> transactions = new ArrayList<>();


        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(pdfUri, "r");

            if (fileDescriptor == null) return transactions;

            PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);

            StringBuilder extractedText = new StringBuilder();

            TessBaseAPI tessBaseAPI = new TessBaseAPI();
            tessBaseAPI.setVariable("user_defined_dpi", "300");


            String tessDataPath = context.getFilesDir().getAbsolutePath();

            copyTessData(context, "deu.traineddata", tessDataPath);
            File tessFile = new File(tessDataPath + "deu.traineddata");

            File tessDir = new File(tessDataPath);

            tessBaseAPI.init(tessDataPath, "deu");

            for (int i = 0; i < pdfRenderer.getPageCount(); i++) {
                System.out.println("TEST1");
                PdfRenderer.Page page = pdfRenderer.openPage(i);
                Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                System.out.println("groesse:"+page.getHeight()+page.getWidth());
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                System.out.println("TEST2");
                tessBaseAPI.setImage(bitmap);
                System.out.println("TEST3");
                String text = tessBaseAPI.getUTF8Text();
                extractedText.append(text).append("\n");
                System.out.println(extractedText);
            }

            pdfRenderer.close();
            fileDescriptor.close();
            tessBaseAPI.end();
            Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})\\s+(.+?)\\s+(-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})");
            Matcher matcher = pattern.matcher(extractedText.toString());

            while (matcher.find()) {
                String date = matcher.group(1);
                String description = matcher.group(2).trim();
                String amountStr = matcher.group(3).replace(".", "").replace(",", ".");
                double amount = Double.parseDouble(amountStr);

                transactions.add(new TransactionEntity(date, description, amount));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(transactions);

        return transactions;
    }
    private static void copyTessData(Context context, String fileName, String targetPath) {
        File tessDir = new File(targetPath);
        if (!tessDir.exists()) {
            boolean created = tessDir.mkdirs();

        }

        File tessFile = new File(targetPath + fileName);

        if (tessFile.exists()) {
            tessFile.delete();
        }

        try (InputStream in = context.getAssets().open("tessdata/" + fileName);
             FileOutputStream out = new FileOutputStream(tessFile)) {

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
