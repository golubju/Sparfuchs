package com.example.sparfuchs.backend;

import java.util.ArrayList;
import java.util.List;

public class Category {

        private String name;
        private List<String> descriptions = new ArrayList<String>();

        public Category(String name) {
            this.name = name;
        }

        public Category(String name, List<String> list) {
            this.name = name;
            this.descriptions=list;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getDescription() {
            return descriptions;
        }

        public void setDescription(List<String> description) {
            this.descriptions = description;
        }

        public void addDescriptions(String description) {
            descriptions.add(description);
        }




    }