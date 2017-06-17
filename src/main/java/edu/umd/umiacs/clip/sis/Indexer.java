package edu.umd.umiacs.clip.sis;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import javax.mail.Session;
import net.fortuna.mstor.MStorMessage;
import net.fortuna.mstor.data.MboxFile;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Mossaab Bagdouri
 */
public class Indexer {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.mime.address.strict", "false");
        String mboxPath = "/fs/clip-secrets/mossaab_maghress.com/archive.mbox";
        String indexPath = "/fs/clip-secrets/mossaab_maghress.com/index";

        Session session = Session.getInstance(props, null);

        System.out.println(new Date() + " - Started indexing.");
        MboxFile file = new MboxFile(new File(mboxPath));
        int count = file.getMessageCount();
        try (IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexPath).toPath()), new IndexWriterConfig(new EnglishAnalyzer()))) {
            for (int i = 0; i < count; i++) {
                iw.addDocument(new MessageConverter(new MStorMessage(session, file.getMessageAsStream(i))).toDocument());
                if (i > 0 && i % 1000 == 0) {
                    System.out.println(new Date() + " - Messages indexed: " + (i + 1));
                    iw.commit();
                }
            }
            iw.commit();
            System.out.println(new Date() + " - Started merging.");
            iw.forceMerge(1);
            iw.commit();
        }
        System.out.println(new Date() + " - Finished indexing.");
    }
}
