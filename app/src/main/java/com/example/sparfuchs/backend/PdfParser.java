package com.example.sparfuchs.backend;

import android.content.Context;
import android.net.Uri;
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
            switch (bank.toUpperCase()){
                case("DKB"): return parsingDBK(extractedText);
                case ("DEUTSCHE BANK"): return parsingDeutschBank();
                case ("SPARKASSE"):return parsingSparkasse(extractedText);
                case ("POSTBANK"): return parsingPostbank();
                }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return transactions;
    }

    private static List<TransactionEntity> parsingPostbank() {
        return null;
    }

    private static List<TransactionEntity> parsingSparkasse(StringBuilder extractedText) {
        List<TransactionEntity> transactions = new ArrayList<>();
        String text = extractedText.toString();

        int indexOfAuszug = text.indexOf("Auszug");
        if (indexOfAuszug != -1) {
            text = text.substring(indexOfAuszug);
        }

        int indexOfKontostand = text.indexOf("Kontostand");
        if (indexOfKontostand != -1) {
            text = text.substring(0,indexOfKontostand);
        }
        Pattern transactionPattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})\\s*(\\S+.*?)\\s*(-?\\d{1,3}(?:\\.\\d{3})*,\\d{2})\\s*(.*?)(?=\\n\\d{2}\\.\\d{2}\\.\\d{4}|$)", Pattern.DOTALL);
        Matcher matcher = transactionPattern.matcher(text);

        while (matcher.find()) {
            String date = Objects.requireNonNull(matcher.group(1)).trim(); // Datum
            String firstDescriptionPart = Objects.requireNonNull(matcher.group(2)).trim(); // Erster Teil der Beschreibung
            String amountStr = Objects.requireNonNull(Objects.requireNonNull(matcher.group(3))).trim(); // Betrag
            String remainingDescription = matcher.group(4) != null ? Objects.requireNonNull(matcher.group(4)).trim() : ""; // Rest der Beschreibung

            if (remainingDescription.matches("\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
                remainingDescription = "";
            }

            amountStr = amountStr.replace(".", "").replace(",", ".");
            double amount = Double.parseDouble(amountStr);

            String description = firstDescriptionPart;
            if (!remainingDescription.isEmpty()) {
                description += " " + remainingDescription;
            }
            transactions.add(new TransactionEntity(date, description, amount));
        }
        return transactions;
    }

    private static List<TransactionEntity> parsingDeutschBank() {
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
                    String amountStr = Objects.requireNonNull(transMatcher.group(1)).replace(".", "").replace(",", ".");
                    double amount = Double.parseDouble(amountStr);
                    String date = transMatcher.group(2);
                    String description = transactionText.substring(transMatcher.end()).trim();
                    transactions.add(new TransactionEntity(date, description, amount));
                }
            }
            return transactions;
    }
}
