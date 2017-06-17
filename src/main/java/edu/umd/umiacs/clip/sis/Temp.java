package edu.umd.umiacs.clip.sis;

import java.util.List;
import static java.util.stream.Collectors.toList;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 *
 * @author Mossaab Bagdouri
 */
public class Temp {

    public static void main(String[] args) throws Exception {
        QueryParser parser = new QueryParser("field", new EnglishAnalyzer());
        List<String> list = new ApplicationBean().getLexicons().stream().
                flatMap(pair -> pair.getRight().stream()).
                map(Pair::getRight).
                collect(toList());
        for (String query : list) {
            System.out.println(query);
            System.out.println(parser.parse(query));
        }
    }
}
