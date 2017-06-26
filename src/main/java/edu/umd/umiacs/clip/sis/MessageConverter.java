package edu.umd.umiacs.clip.sis;

import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.Date;
import java.util.stream.Stream;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.util.BytesRef;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 *
 * @author Mossaab Bagdouri
 */
public class MessageConverter {

    public static final String SUBJECT = "Subject";
    public static final String THREAD_ID = "X-GM-THRID";
    public static final String MESSAGE_ID = "Message-ID";
    public static final String LABELS = "X-Gmail-Labels";
    public static final String DATE = "Date";
    public static final String FROM = "From";
    public static final String BODY_TEXT = "plain";
    public static final String BODY_HTML = "html";
    public static final String ATTACHMENT = "attachment";
    public static final String FILE_NAME = "file-name";
    public static final String MIME_TYPE = "mime-type";
    public static final String MIME = "mime";
    public static final String TO = RecipientType.TO.toString();
    public static final String CC = RecipientType.CC.toString();
    public static final String BCC = RecipientType.BCC.toString();
    private static final FieldType POSITIONS_STORED = new FieldType();

    static {
        POSITIONS_STORED.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        POSITIONS_STORED.setStored(true);
    }

    private final Message message;

    public MessageConverter(Message message) {
        this.message = message;
    }

    private static StringBuilder addContent(Part part, StringBuilder sb)
            throws MessagingException, IOException, TikaException, SAXException {
        if (part instanceof Message) {
            sb.append("Subject: ").append(((Message) part).getSubject()).append("\n");
        }
        Object content = part.getContent();
        if (content instanceof Multipart) {
            Multipart multi = (Multipart) content;
            for (int i = 0; i < multi.getCount(); i++) {
                addContent(multi.getBodyPart(i), sb);
            }
        } else if (content instanceof MimeMessage) {
            MimeMessage mime = (MimeMessage) content;
            sb.append("Mime (").append(part.getContentType().split(";")[0]).
                    append("):\n").append(mime.getContent().toString().trim()).append("\n");
        } else if (part.getFileName() != null) {
            sb.append("File (").append(part.getFileName()).append("):\n").
                    append(new Tika().parseToString(part.getInputStream()).trim()).
                    append("\n");
        } else {
            sb.append("Body (").append(part.getContentType().split(";")[0]).
                    append("):\n").append(content.toString().trim()).append("\n");
        }
        return sb;
    }

    private static void addContent(Part part, Document document)
            throws MessagingException, IOException, TikaException, SAXException {
        if (part instanceof Message) {
            Message message = (Message) part;
            if (message.getSubject() == null) {
                return;
            }
            document.add(new Field(SUBJECT, message.getSubject(), POSITIONS_STORED));
            document.add(new SortedDocValuesField(SUBJECT, new BytesRef(message.getSubject())));
            String[] threadid = message.getHeader(THREAD_ID);
            if (threadid != null && threadid.length > 0) {
                document.add(new StringField(THREAD_ID, threadid[0], Store.YES));
            }
            String[] labels = message.getHeader(LABELS);
            if (labels != null && labels.length > 0) {
                Stream.of(labels[0].split(",")).forEach(label -> document.add(new Field(LABELS, label, POSITIONS_STORED)));
            }
            long sent = message.getSentDate().getTime();
            document.add(new StoredField(DATE, sent));
            document.add(new NumericDocValuesField(DATE, sent));
            Stream.of(message.getFrom()).
                    forEach(address -> {
                        String converted = convertAddress(address);
                        document.add(new Field(FROM, converted, POSITIONS_STORED));
                        document.add(new SortedDocValuesField(FROM, new BytesRef(converted)));
                    });
            String[] messageid = message.getHeader(MESSAGE_ID);
            if (messageid != null && messageid.length > 0) {
                document.add(new StringField(MESSAGE_ID, messageid[0], Store.YES));
            }
            for (RecipientType type : asList(RecipientType.TO, RecipientType.CC, RecipientType.BCC)) {
                Address[] recipients = message.getRecipients(type);
                if (recipients != null) {
                    for (Address recipient : recipients) {
                        document.add(new Field(type.toString(), convertAddress(recipient), POSITIONS_STORED));
                    }
                }
            }
        }
        Object content = part.getContent();
        if (content instanceof Multipart) {
            Multipart multi = (Multipart) content;
            for (int i = 0; i < multi.getCount(); i++) {
                addContent(multi.getBodyPart(i), document);
            }
        } else if (content instanceof MimeMessage) {
            MimeMessage mime = (MimeMessage) content;
            document.add(new StringField(MIME_TYPE, part.getContentType(), Store.YES));
            document.add(new StringField(MIME, mime.getContent().toString().trim(), Store.YES));
        } else if (part.getFileName() != null) {
            if (part.getFileName() != null) {
                document.add(new Field(FILE_NAME, part.getFileName(), POSITIONS_STORED));
                document.add(new Field(ATTACHMENT, new Tika().parseToString(part.getInputStream()).trim(), POSITIONS_STORED));
            }
        } else {
            if (part.getContentType().split(";")[0].equals("text/plain")) {
                document.add(new Field(BODY_TEXT, content.toString().trim(), POSITIONS_STORED));
            } else {
                document.add(new Field(BODY_HTML, content.toString().trim(), POSITIONS_STORED));
            }
        }
    }

    private static String convertAddress(Address address) {
        String personal = ((InternetAddress) address).getPersonal();
        String addr = ((InternetAddress) address).getAddress();
        StringBuilder sb = new StringBuilder();
        if (personal != null) {
            sb.append(personal).append(" ");
        }
        if (addr != null) {
            sb.append(addr).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        try {
            addContent(message, sb);
        } catch (IOException | MessagingException | TikaException | SAXException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public Document toDocument() {
        Document document = new Document();
        try {
            MessageConverter.addContent(message, document);
        } catch (IOException | MessagingException | TikaException | SAXException e) {
            e.printStackTrace();
        }
        return document;
    }
}
