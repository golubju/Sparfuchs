package com.example.sparfuchs.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionAnalyser {
    List<Category> categories = new ArrayList<Category>();

    public TransactionAnalyser() {
        createCategories();
    }

    public void createCategories(){
        categories.add(new Category("LEBENSMITTEL", Arrays.asList("SUPERMARKT", "LIDL", "ALDI", "REWE", "EDEKA", "PENNY", "NETTO")));
        categories.add(new Category("DROGERIE", Arrays.asList("DROGERIE", "DM", "ROSSMANN", "APOTHEKE")));
        categories.add(new Category("MIETE", Arrays.asList("MIETE", "WOHNGENOSSENSCHAFT", "VERMIETER")));
        categories.add(new Category("NEBENKOSTEN", Arrays.asList("ENERGIE", "STADTWERKE", "STROM", "GAS", "WASSER")));
        categories.add(new Category("KOMMUNIKATION", Arrays.asList("INTERNET", "TELEKOM", "VODAFONE", "O2", "HANDY")));
        categories.add(new Category("TANKEN", Arrays.asList("TANKSTELLE", "SHELL", "ARAL", "ESSO", "TOTAL", "TANK")));
        categories.add(new Category("TRANSPORT", Arrays.asList("BAHNTICKET", "ÖPNV", "BVG", "DB")));
        categories.add(new Category("GASTRONOMIE", Arrays.asList("RESTAURANT", "MCDONALD'S", "BURGER KING", "PIZZERIA", "CAFÉ")));
        categories.add(new Category("FITNESS", Arrays.asList("FITNESS", "GYM", "SPORT", "GESUNDHEIT")));
        categories.add(new Category("FREIZEIT & UNTERHALTUNG", Arrays.asList("KINO", "NETFLIX", "SPOTIFY", "DISNEY", "THEATER")));
        categories.add(new Category("KLEIDUNG & MODE", Arrays.asList("KLEIDUNG", "MODE", "H&M", "ZARA", "C&A")));
        categories.add(new Category("ONLINE-SHOPPING", Arrays.asList("AMAZON", "EBAY", "BESTELLUNG")));
        categories.add(new Category("GESUNDHEIT", Arrays.asList("ARZT", "KRANKENKASSE", "MEDIKAMENTE")));
        categories.add(new Category("SPENDEN", Arrays.asList("SPENDE", "WOHLTÄTIGKEIT")));
        categories.add(new Category("AUSZAHLUNG", Arrays.asList("GELDAUTOMAT", "AUSZAHLUNG", "ABHEBUNG")));
        categories.add(new Category("EINZAHLUNG", Arrays.asList("EINZAHLUNG", "BUCHUNG")));
        categories.add(new Category("KONTOSTAND", Arrays.asList("KONTOSTAND", "AUSZUG")));
        categories.add(new Category("GEHALT", Arrays.asList("LOHN", "GEHALT","NEBENJOB")));
        categories.add(new Category("GESCHENKE", Arrays.asList("GESCHENK", "GEBURTSTAG")));
        categories.add(new Category("BANK", Arrays.asList("ZINSEN", "WERTPAPIER","KONTOGEBÜHREN")));

    }

    public String getCategory(TransactionEntity transactionEntity){
        String text= transactionEntity.getDescription().toUpperCase();
        for (Category category: categories){
            for(String words: category.getDescription()){
                if(text.contains(words)){
                    return category.getName();
                }
            }
        }
            return "SONSTIGES";
    }



}
