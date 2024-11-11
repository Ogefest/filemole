package com.lukaszgajos.filemole.domain.entity.search;

public class Path  implements  Filter {
    private final String q;

    public Path(String q) {
        this.q = q;
    }

    @Override
    public String getQuery() {

        String[] elems = q.split(" ");
        String result = "";
        for (String s : elems) {
            result += " path LIKE '%" + q + "%' AND";
        }
        result = result.substring(0, result.length() - 3);

        return result;
    }
}
