package com.company.gigalab.enumeration;

import com.company.gigalab.reader.impl.FileReader;
import com.company.gigalab.reader.impl.ImageReader;
import com.company.gigalab.reader.Reader;

import java.util.Arrays;

public enum ContentType {
    PLAIN("text/plain", "txt", new FileReader()),
    HTML("text/html", "html", new FileReader()),
    CSS("text/css", "css", new FileReader()),
    JS("application/javascript", "js", new FileReader()),
    PNG("image/png", "png", new ImageReader()),
    JPEG("image/jpeg", "jpeg", new ImageReader()),
    SVG("image/svg+xml", "svg", new FileReader());

    private final String text;
    private final String extension;
    private final Reader reader;


    ContentType(String text, String extension, Reader reader){
        this.text = text;
        this.extension = extension;
        this.reader = reader;
    }

    public String getText(){
        return text;
    }

    public String getExtension(){
        return extension;
    }


    public Reader getReader() {
        return reader;
    }

    public static ContentType findByFileName(String fileName){
        String extension = fileName.substring(fileName.lastIndexOf(".")+1);
        return Arrays.stream(ContentType.values())
                .filter(x -> x.getExtension().equalsIgnoreCase(extension))
                .findFirst()
                .orElse(PLAIN);
    }
}
