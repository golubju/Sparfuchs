package com.example.sparfuchs.backend;

import android.content.Context;
import android.net.Uri;
import com.example.sparfuchs.backend.TransactionEntity;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfParser {

    public static List<TransactionEntity> extractTransactionsFromPDF(Context context, Uri pdfUri, String bank) {
        List<TransactionEntity> transactions = new ArrayList<>();

        try {
            InputStream inputStream = context.getContentResolver().openInputStream(pdfUri);
            PdfReader reader = new PdfReader(inputStream);
            PdfDocument pdfDoc = new PdfDocument(reader);

            StringBuilder extractedText = new StringBuilder();

            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                String pageText = PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i));
                extractedText.append(pageText).append("\n");
            }

            pdfDoc.close();
            inputStream.close();

            System.out.println("Extrahierter Text:\n" + extractedText);

            switch (bank.toUpperCase()){
                case("DKB"): return parsingDBK(extractedText);
                case ("DEUTSCHE BANK"): return parsingDeutschBank(extractedText);
                case ("SPARKASSE"):return parsingSparkasse(extractedText);
                case ("POSTBANK"): return parsingPostbank(extractedText);

                }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }

    private static List<TransactionEntity> parsingPostbank(StringBuilder extractedText) {
        return null;
    }

    private static List<TransactionEntity> parsingSparkasse(StringBuilder extractedText) {
        return null;
    }



    private static List<TransactionEntity> parsingDeutschBank(StringBuilder extractedText) {
        return null;
    }

    private static List<TransactionEntity> parsingDBK(StringBuilder extractedText) {
            List<TransactionEntity> transactions = new ArrayList<>();
            String text = extractedText.toString();
            int startIdx = text.indexOf("Kontostand");
            if (startIdx != -1) {
                text = text.substring(startIdx + "Kontostand".length()).trim();
            }
            int endIdx = text.indexOf("Kontostand");
            if (endIdx != -1) {
                text = text.substring(0, endIdx).trim();
            }
            Pattern transactionPattern = Pattern.compile(
                    "(-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*\\n\\s*(\\d{2}\\.\\d{2}\\.\\d{4})"
            );
            Matcher matcher = transactionPattern.matcher(text);
            List<Integer> transactionIndices = new ArrayList<>();
            while (matcher.find()) {
                transactionIndices.add(matcher.start());
            }
            for (int i = 0; i < transactionIndices.size(); i++) {
                int transStart = transactionIndices.get(i);
                int transEnd = (i + 1 < transactionIndices.size()) ? transactionIndices.get(i + 1) : text.length();
                String transactionText = text.substring(transStart, transEnd).trim();
                Matcher transMatcher = transactionPattern.matcher(transactionText);
                if (transMatcher.find()) {
                    String amountStr = transMatcher.group(1).replace(".", "").replace(",", ".");
                    double amount = Double.parseDouble(amountStr);
                    String date = transMatcher.group(2);
                    String description = transactionText.substring(transMatcher.end()).trim();
                    transactions.add(new TransactionEntity(date, description, amount));
                }
            }

            return transactions;


    }


    private static String extractAmount(String transactionText) {
        // Betrag kann negativ oder positiv sein und hat das Format 1.234,56 oder -123,45
        Pattern amountPattern = Pattern.compile("(-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})");
        Matcher amountMatcher = amountPattern.matcher(transactionText);

        if (amountMatcher.find()) {
            return amountMatcher.group(1).replace(".", "").replace(",", ".");
        }
        return "";
    }
}
