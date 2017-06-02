package edu.umd.umiacs.clip.sis;

import com.beust.jcommander.IDefaultProvider;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import edu.umd.umiacs.clip.tools.io.AllFiles;
import edu.umd.umiacs.clip.tools.lang.LuceneUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import static java.util.stream.Collectors.toSet;
import java.util.stream.Stream;
import javax.mail.Folder;
import static javax.mail.Folder.READ_ONLY;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;
import javax.mail.search.BodyTerm;
import javax.mail.search.OrTerm;
import javax.mail.search.SubjectTerm;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

/**
 *
 * @author Mossaab Bagdouri
 */
public class SecretSearcher {

    @Parameter(names = {"-?", "-h", "--help"}, help = true, description = "Display this help.")
    private boolean help;

    @Parameter(names = "--host", description = "IMAP host address.")
    private String host = "imap.gmail.com";

    @Parameter(names = "--port", description = "IMAP host part number.")
    private int port = 993;

    @Parameter(names = {"--username"}, description = "Username.")
    private String username;

    @Parameter(names = "--password", description = "Password.")
    private String password;

    @Parameter(names = "--terms", description = "Search terms, separated by comma.", required = true, listConverter = TermsConverter.class)
    private List<String> terms;

    @Parameter(names = "--mode", description = "Retrieval mode. Supported values: search, filter.", required = true, validateWith = RetrievalModeValidator.class)
    private String mode;

    @Parameter(names = "--stem", description = "Use stemming. Works only with --mode filter.")
    private boolean stemming = false;

    @Parameter(names = "--verbosity", description = "Verbosity level. Silent = 0, verbose = 1, print content to stdout = 2.")
    private int verbosity = 1;

    @Parameter(names = "--output", description = "Output folder (absolute path).", validateWith = OutputValidator.class)
    private String output;

    private static final IDefaultProvider DEFAULT_PROVIDER = (String optionName) -> {
        Properties properties = new Properties();
        try {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("user.properties");
            if (is != null) {
                properties.load(is);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties.getProperty(optionName.replace("-", ""));
    };

    private Store store;

    public SecretSearcher connect() {
        try {
            URLName url = new URLName("imaps", host, port, "", username, password);
            store = Session.getInstance(System.getProperties(), null).getStore(url);
            store.connect();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    public void getContent(Part part, StringBuilder sb) throws MessagingException, IOException, TikaException, SAXException {
        if (part instanceof Message) {
            sb.append("Subject: ").append(((Message) part).getSubject()).append("\n");
        }
        Object content = part.getContent();
        if (content instanceof Multipart) {
            Multipart multi = (Multipart) content;
            for (int i = 0; i < multi.getCount(); i++) {
                getContent(multi.getBodyPart(i), sb);
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
    }

    public String getContent(Part part) throws MessagingException, IOException, TikaException, SAXException {
        StringBuilder sb = new StringBuilder();
        getContent(part, sb);
        return sb.toString();
    }

    public List<String> search() throws MessagingException, IOException, TikaException, SAXException {
        OrTerm or = new OrTerm(new SubjectTerm(terms.get(0)), new BodyTerm(terms.get(0)));
        for (int i = 1; i < terms.size(); i++) {
            or = new OrTerm(or, new OrTerm(new SubjectTerm(terms.get(i)), new BodyTerm(terms.get(i))));
        }
        Set<String> seen = new HashSet<>();
        List<String> found = new ArrayList<>();
        int fileId = 0;
        for (Folder folder : store.getDefaultFolder().list("*")) {
            if (folder.getType() != Folder.HOLDS_FOLDERS) {
                folder.open(READ_ONLY);
                for (Message message : folder.search(or)) {
                    String[] headers = message.getHeader("Message-ID");
                    String id = headers == null ? (message.getSubject() + "\t" + message.getSentDate().getTime()) : headers[0];
                    if (!seen.contains(id)) {
                        seen.add(id);
                        if (verbosity == 1) {
                            System.out.println("Retrieved email: " + id);
                        }
                        String content = getContent(message);
                        if (verbosity == 2) {
                            System.out.println("Content of email: " + id);
                            System.out.println(content + "\n");
                        }
                        if (output != null) {
                            AllFiles.write(output + "/" + (fileId++) + ".txt", asList(content));
                        }
                        found.add(content);
                    }
                }
                folder.close(false);
            }
        }
        return found;
    }

    public List<String> filter() throws MessagingException, IOException, TikaException, SAXException {
        Set<String> analyzedTerms = terms.stream().
                map(LuceneUtils::tokenizeStopped).collect(toSet());
        Set<String> seen = new HashSet<>();
        List<String> found = new ArrayList<>();
        int fileId = 0;
        for (Folder folder : store.getDefaultFolder().list("*")) {
            if (folder.getType() != Folder.HOLDS_FOLDERS) {
                folder.open(READ_ONLY);
                for (Message message : folder.getMessages()) {
                    String[] headers = message.getHeader("Message-ID");
                    String id = headers == null ? (message.getSubject() + "\t" + message.getSentDate().getTime()) : headers[0];
                    if (!seen.contains(id)) {
                        seen.add(id);
                        String content = getContent(message);
                        if (Stream.of(content.split("\\s+")).
                                map(LuceneUtils::tokenizeStopped).distinct().parallel().
                                anyMatch(analyzedTerms::contains)) {
                            System.out.println(id);
                            if (verbosity == 1) {
                                System.out.println("Retrieved email: " + id);
                            }
                            if (verbosity == 2) {
                                System.out.println("Content of email: " + id);
                                System.out.println(content + "\n");
                            }
                            if (output != null) {
                                AllFiles.write(output + "/" + (fileId++) + ".txt", asList(content));
                            }
                            found.add(content);
                        }
                    }
                }
                folder.close(false);
            }
        }
        return found;
    }

    public void run() throws MessagingException, IOException, TikaException, SAXException {
        connect();
        if (mode.equals("search")) {
            search();
        } else if (mode.equals("filter")) {
            filter();
        }
    }

    public static void main(String[] args) throws Exception {
        SecretSearcher searcher = new SecretSearcher();
        JCommander cmd = JCommander.newBuilder().
                addObject(searcher).
                defaultProvider(DEFAULT_PROVIDER).
                programName("./run.sh").
                build();
        try {
            cmd.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            cmd.usage();
            System.exit(1);
        }
        searcher.run();
    }
}
