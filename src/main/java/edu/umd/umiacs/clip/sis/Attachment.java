package edu.umd.umiacs.clip.sis;

import java.io.ByteArrayInputStream;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 *
 * @author Mossaab Bagdouri
 */
public class Attachment {

    private final String name;
    private final String type;
    private final byte[] content;

    public Attachment(String name, String type, byte[] content) {
        this.name = name;
        this.type = type;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getSize() {
        if (content.length >= 1024 * 1024) {
            return ((content.length * 10) / (1024 * 1024)) / 10f + " MB";
        } else if (content.length >= 1024) {
            return ((content.length * 10) / 1024) / 10f + " KB";
        } else {
            return content.length + " B";
        }
    }

    public StreamedContent getDownload() {
        return new DefaultStreamedContent(new ByteArrayInputStream(content), type, name);
    }
}
