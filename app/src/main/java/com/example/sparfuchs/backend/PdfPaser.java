package com.example.sparfuchs.backend;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfPaser {
    List<Transaction> transactions = new ArrayList<Transaction>();
    File file;

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public File getFile() {
        return file;
    }


    public PdfPaser(File file) {
        this.file = file;
        analyseText(getTextFromPdf());
    }

    public String getTextFromPdf() {

        try {
            PDDocument document;
            document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return (text);

        } catch (IOException e) {
            System.err.println("Fehler beim Laden der PDF-Datei: " + e.getMessage());
            return null;
        }
    }

    private String removeTextBeforeFirstDate(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String dateRegex = "\\d{2}\\.\\d{2}\\.\\d{4}";
        Pattern datePattern = Pattern.compile(dateRegex);
        Matcher dateMatcher = datePattern.matcher(text);

        if (dateMatcher.find()) {
            return text.substring(dateMatcher.start()).trim();
        }
        return text;
    }

    public double amountStrToDouble(String amountStr){
        amountStr = amountStr.replace(".", "");
        amountStr = amountStr.replace(",", ".");
        amountStr = amountStr.replace(" ", "");
        boolean negativ= amountStr.contains("-");
        amountStr = amountStr.replace("-", "");
        amountStr = amountStr.replace("+", "");

        double amount = 0.0;
        try {
            amount = Double.parseDouble(amountStr);
            if(negativ){
                amount=amount*(-1);
            }
        } catch (NumberFormatException e) {
            System.err.println("Fehler beim Parsen des Betrags: " + amountStr);
        }
        return amount;
    }


    public void analyseText(String text) {
        if (text != null) {
            text = removeTextBeforeFirstDate(text);

            Pattern datePattern = Pattern.compile("\\d{2}\\.\\d{2}\\.(?:\\s*\\n*\\s*)\\d{4}");
            Pattern amountPattern = Pattern.compile("[\\+-]?\\s*\\d{1,3}(?:\\.\\d{3})*,\\d{2}\\s*[\\+-]?(?=(\\s*[a-zA-Z]|\\s|$|[^0-9]))");
            Matcher dateMatcher = datePattern.matcher(text);
            Matcher amountMatcher = amountPattern.matcher(text);

            String lastDate = null;
            int lastDateEnd = -1;

            while (dateMatcher.find()) {
                String date = dateMatcher.group().replaceAll("\\s+", "").trim();
                int currentDateStart = dateMatcher.start();
                int currentDateEnd = dateMatcher.end();

                if (lastDate != null && lastDateEnd > 0) {
                    Matcher tempAmountMatcher = amountPattern.matcher(text.substring(lastDateEnd, currentDateStart));
                    if (!tempAmountMatcher.find()) {
                        lastDate = date;
                        lastDateEnd = currentDateEnd;
                        continue;
                    }
                }

                lastDate = date;
                lastDateEnd = currentDateEnd;

                if (amountMatcher.find()) {
                    String amountStr = amountMatcher.group().trim();
                    double amount= amountStrToDouble(amountStr);

                    String description;

                    if (lastDateEnd < amountMatcher.start()) {
                        description = text.substring(lastDateEnd, amountMatcher.start()).trim();
                    } else {
                        int nextDateStart = text.length();
                        if (dateMatcher.find()) {
                            nextDateStart = dateMatcher.start();
                        }
                        description = text.substring(amountMatcher.end(), nextDateStart).trim();
                    }

                    Transaction transaction = new Transaction(lastDate, description.isEmpty() ? "keine Beschreibung" : description, amount);
                    transactions.add(transaction);
                }
            }
        }
    }


}
