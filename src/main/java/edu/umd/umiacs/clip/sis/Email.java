package edu.umd.umiacs.clip.sis;

import static edu.umd.umiacs.clip.sis.MessageConverter.ATTACHMENT_BINARY;
import static edu.umd.umiacs.clip.sis.MessageConverter.BCC;
import static edu.umd.umiacs.clip.sis.MessageConverter.BODY_HTML;
import static edu.umd.umiacs.clip.sis.MessageConverter.BODY_TEXT;
import static edu.umd.umiacs.clip.sis.MessageConverter.CC;
import static edu.umd.umiacs.clip.sis.MessageConverter.DATE;
import static edu.umd.umiacs.clip.sis.MessageConverter.FROM;
import static edu.umd.umiacs.clip.sis.MessageConverter.MESSAGE_ID;
import static edu.umd.umiacs.clip.sis.MessageConverter.SUBJECT;
import static edu.umd.umiacs.clip.sis.MessageConverter.TO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.document.Document;
import static edu.umd.umiacs.clip.sis.MessageConverter.ATTACHMENT_PARSED;
import static edu.umd.umiacs.clip.sis.MessageConverter.FILE_NAME;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Mossaab Bagdouri
 */
public class Email implements Serializable {

    private String id = "";
    private Pair<String, String> from;
    private List<Pair<String, String>> to = new ArrayList<>();
    private List<Pair<String, String>> cc = new ArrayList<>();
    private List<Pair<String, String>> bcc = new ArrayList<>();
    private List<Attachment> files = new ArrayList<>();
    private String subject = "";
    private String body = "";
    private String html = "";
    private String attachment = "";
    private int annotation = 0;
    private Date date;
    private float score;

    public Email() {
    }

    protected static Pair<String, String> getAddressPair(String addr) {
        int index = addr.lastIndexOf(" ");
        return index > 0
                ? Pair.of(addr.substring(0, index).trim(), addr.substring(index + 1).trim())
                : Pair.of(addr, addr);
    }

    public Email(Map<String, Boolean> annotations, Document doc) {
        id = doc.get(MESSAGE_ID);
        subject = doc.get(SUBJECT);
        body = doc.get(BODY_TEXT);
        html = doc.get(BODY_HTML);
        attachment = doc.get(ATTACHMENT_PARSED);
        from = getAddressPair(doc.get(FROM));
        String addresses = doc.get(TO);
        if (addresses != null && !addresses.isEmpty()) {
            Stream.of(addresses.split("\n")).map(Email::getAddressPair).
                    forEach(to::add);
        }
        addresses = doc.get(CC);
        if (addresses != null && !addresses.isEmpty()) {
            Stream.of(addresses.split("\n")).map(Email::getAddressPair).
                    forEach(cc::add);
        }

        Set<String> ccSet = cc.stream().map(Pair::getRight).collect(toSet());
        addresses = doc.get(BCC);
        if (addresses != null && !addresses.isEmpty()) {
            Stream.of(addresses.split("\n")).map(Email::getAddressPair).
                    filter(pair -> !ccSet.contains(pair.getRight())).
                    forEach(bcc::add);
        }
        date = new Date(new Long(doc.get(DATE)));

        String[] names = doc.getValues(FILE_NAME);
        if (names.length > 0) {
            String[] types = doc.getValues(FILE_NAME);
            BytesRef[] binaries = doc.getBinaryValues(ATTACHMENT_BINARY);
            for (int i = 0; i < names.length; i++) {
                files.add(new Attachment(names[i], types[i], binaries[i].bytes));
            }
        }

        if (annotations.get(id) != null) {
            this.annotation = annotations.get(id) ? 1 : 2;
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the from to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the from
     */
    public Pair<String, String> getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(Pair<String, String> from) {
        this.from = from;
    }

    /**
     * @return the to
     */
    public List<Pair<String, String>> getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(List<Pair<String, String>> to) {
        this.to = to;
    }

    /**
     * @return the cc
     */
    public List<Pair<String, String>> getCc() {
        return cc;
    }

    /**
     * @param cc the cc to set
     */
    public void setCc(List<Pair<String, String>> cc) {
        this.cc = cc;
    }

    /**
     * @return the bcc
     */
    public List<Pair<String, String>> getBcc() {
        return bcc;
    }

    /**
     * @param bcc the bcc to set
     */
    public void setBcc(List<Pair<String, String>> bcc) {
        this.bcc = bcc;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the attachment
     */
    public String getAttachment() {
        return attachment;
    }

    /**
     * @param attachment the attachment to set
     */
    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    /**
     * @return the annotation
     */
    public int getAnnotation() {
        return annotation;
    }

    /**
     * @param annotation the annotation to set
     */
    public void setAnnotation(int annotation) {
        this.annotation = annotation;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the score
     */
    public float getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(float score) {
        this.score = score;
    }

    public boolean isProtect() {
        return annotation == 1;
    }

    public void setProtect(boolean val) {
        annotation = val ? 1 : 0;
    }

    public boolean isRelease() {
        return annotation == 2;
    }

    public void setRelease(boolean val) {
        annotation = val ? 2 : 0;
    }

    public String getProtectStyleClass() {
        switch (annotation) {
            case 1:
                return "protect";
            default:
                return "inactive";
        }
    }

    public String getReleaseStyleClass() {
        switch (annotation) {
            case 2:
                return "release";
            default:
                return "inactive";
        }
    }

    /**
     * @return the html
     */
    public String getHtml() {
        return html;
    }

    /**
     * @param html the html to set
     */
    public void setHtml(String html) {
        this.html = html;
    }

    /**
     * @return the files
     */
    public List<Attachment> getFiles() {
        return files;
    }

    /**
     * @param files the files to set
     */
    public void setFiles(List<Attachment> files) {
        this.files = files;
    }
}
