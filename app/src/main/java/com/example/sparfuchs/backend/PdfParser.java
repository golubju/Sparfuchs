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

    public static List<TransactionEntity> extractTransactionsFromPDF(Context context, Uri pdfUri) {
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

            // Regex für Datumszeilen (jede Transaktion beginnt mit einem Datum)
            Pattern datePattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})");
            Matcher dateMatcher = datePattern.matcher(extractedText.toString());

            List<Integer> dateIndices = new ArrayList<>();
            while (dateMatcher.find()) {
                dateIndices.add(dateMatcher.start());
            }

            // Transaktionen anhand von Datumspositionen extrahieren
            for (int i = 0; i < dateIndices.size(); i++) {
                int startIdx = dateIndices.get(i);
                int endIdx = (i + 1 < dateIndices.size()) ? dateIndices.get(i + 1) : extractedText.length();
                String transactionText = extractedText.substring(startIdx, endIdx).trim();

                // Das erste gefundene Datum als Transaktionsdatum nehmen
                Matcher dateMatch = datePattern.matcher(transactionText);
                String date = dateMatch.find() ? dateMatch.group(1) : "Unbekannt";

                // Betrag extrahieren (erster gefundener Betrag innerhalb der Transaktion)
                String amount = extractAmount(transactionText);

                // Beschreibung bereinigen (alles außer Datum und Betrag)
                String description = transactionText.replace(date, "").replace(amount, "").trim();

                transactions.add(new TransactionEntity(date, description, amount.isEmpty() ? 0.0 : Double.parseDouble(amount)));
            }

        } catch (Exception e) {
            e.printStackTrace();
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
