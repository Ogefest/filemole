package com.lukaszgajos.filemole.domain.entity.search;

public class MinSize implements Filter {
    private long size;

    public MinSize(String in, String multiplier) {
        size = Integer.parseInt(in);
        size = convertSize(size, multiplier);
    }

    @Override
    public String getQuery() {
        return " size > " + size;
    }

    private long convertSize(long sizeInBytes, String unit) {
        long result = sizeInBytes;

        switch (unit) {
            case "KB":
                result = sizeInBytes * 1024;
                break;
            case "MB":
                result = sizeInBytes * (1024 * 1024);
                break;
            case "GB":
                result = sizeInBytes * (1024 * 1024 * 1024);
                break;
            case "TB":
                result = sizeInBytes * (1024 * 1024 * 1024 * 1024);
                break;
            default:
                result = sizeInBytes;
                break;
        }
        return result;
    }
}
