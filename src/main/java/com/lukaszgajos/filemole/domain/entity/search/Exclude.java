package com.lukaszgajos.filemole.domain.entity.search;

public class Exclude implements Filter{

    private String phrase;
    public Exclude(String phrase) {
        this.phrase = phrase;
    }

    @Override
    public String getQuery() {
        String[] elems = phrase.split(" ");
        String result = "";
        for (String s : elems) {
            result += " path NOT LIKE '%" + s + "%' AND";
        }

        return result.substring(0, result.length() - 3);
    }
}
