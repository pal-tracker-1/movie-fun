package org.superbiz.moviefun.blobstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class Blob {
    public final String name;
    public final byte[] content;
    public final String contentType;

    public Blob(String name, InputStream inputStream, String contentType) {
        try {
            this.name = name;
            this.contentType = contentType;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int len = 0; (len = inputStream.read(buffer)) > 0; ) {
                bos.write(buffer, 0, len);
            }
            content = bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    public String getContentType() {
        return contentType;
    }
}