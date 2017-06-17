package edu.umd.umiacs.clip.sis;

import static edu.umd.umiacs.clip.sis.MessageConverter.ATTACHMENT;
import static edu.umd.umiacs.clip.sis.MessageConverter.BODY_TEXT;
import static edu.umd.umiacs.clip.sis.MessageConverter.DATE;
import static edu.umd.umiacs.clip.sis.MessageConverter.FROM;
import static edu.umd.umiacs.clip.sis.MessageConverter.MESSAGE_ID;
import static edu.umd.umiacs.clip.sis.MessageConverter.SUBJECT;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.document.Document;

/**
 *
 * @author Mossaab Bagdouri
 */
public class Email implements Serializable {

    private String id = "";
    private Pair<String, String> from;
    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private String subject = "";
    private String body = "";
    private String attachment = "";
    private int annotation = 0;
    private Date date;
    private float score;

    public Email() {
    }

    public Email(Map<String, Boolean> annotations, Document doc) {
        id = doc.get(MESSAGE_ID);
        subject = doc.get(SUBJECT);
        body = doc.get(BODY_TEXT);
        attachment = doc.get(ATTACHMENT);
        String addr = doc.get(FROM);
        int index = addr.lastIndexOf(" ");
        if (index > 0) {
            from = Pair.of(addr.substring(0, index).trim(), addr.substring(index + 1).trim());
        } else {
            from = Pair.of(addr, addr);
        }
        date = new Date(new Long(doc.get(DATE)));
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
    public List<String> getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(List<String> to) {
        this.to = to;
    }

    /**
     * @return the cc
     */
    public List<String> getCc() {
        return cc;
    }

    /**
     * @param cc the cc to set
     */
    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    /**
     * @return the bcc
     */
    public List<String> getBcc() {
        return bcc;
    }

    /**
     * @param bcc the bcc to set
     */
    public void setBcc(List<String> bcc) {
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
}
