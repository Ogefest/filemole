package com.lukaszgajos.filemole.domain.entity.search;

public class Path  implements  Filter {
    private final String q;

    public Path(String q) {
        this.q = q;
    }

    @Override
    public String getQuery() {
        return "path LIKE '%" + q + "%'";
    }
}
