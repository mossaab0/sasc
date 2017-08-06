package edu.umd.umiacs.clip.sis;

import static edu.umd.umiacs.clip.sis.MessageConverter.BCC;
import static edu.umd.umiacs.clip.sis.MessageConverter.BODY_TEXT;
import static edu.umd.umiacs.clip.sis.MessageConverter.CC;
import static edu.umd.umiacs.clip.sis.MessageConverter.FILE_NAME;
import static edu.umd.umiacs.clip.sis.MessageConverter.FROM;
import static edu.umd.umiacs.clip.sis.MessageConverter.MESSAGE_ID;
import static edu.umd.umiacs.clip.sis.MessageConverter.SUBJECT;
import static edu.umd.umiacs.clip.sis.MessageConverter.TO;
import java.io.IOException;
import java.util.ArrayList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import static org.apache.lucene.search.SortField.Type.LONG;
import static org.apache.lucene.search.SortField.Type.SCORE;
import static org.apache.lucene.search.SortField.Type.STRING;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import static org.primefaces.model.SortOrder.DESCENDING;
import static edu.umd.umiacs.clip.sis.MessageConverter.ATTACHMENT_PARSED;
import static org.apache.commons.lang3.StringUtils.capitalize;

/**
 *
 * @author Mossaab Bagdouri
 */
public class LazyEmailDataModel extends LazyDataModel<Email> {

    private final transient IndexSearcher is;
    private String q;
    private final List<Email> data = new ArrayList<>();
    private final Map<String, Email> map = new HashMap<>();
    private final Map<String, Integer> annotations;
    private float[] predictions;
    private Email firstEmail;
    private boolean activeLearning;

    public LazyEmailDataModel(IndexSearcher is, Map<String, Integer> annotations, String q) {
        this.is = is;
        this.q = q;
        this.annotations = annotations;
    }

    public LazyEmailDataModel(IndexSearcher is, Map<String, Integer> annotations, float[] predictions, boolean activeLearning) {
        this.is = is;
        this.predictions = predictions;
        this.annotations = annotations;
        this.activeLearning = activeLearning;
    }

    @Override
    public Email getRowData(String rowKey) {
        return map.get(rowKey);
    }

    @Override
    public Object getRowKey(Email email) {
        return email.getId();
    }

    private void process(Query query, int first, int pageSize, String sortField, SortOrder sortOrder) {
        data.clear();
        map.clear();
        SortField sort = (sortField == null || sortField.equals("score"))
                ? new SortField(null, SCORE, sortOrder == DESCENDING)
                : new SortField(capitalize(sortField),
                        sortField.equals("date") ? LONG : STRING,
                        sortOrder == DESCENDING);
        if (is != null) {
            try {
                TopDocs hits = is.search(query, first + pageSize, new Sort(sort), true, false);
                for (int i = first; i < hits.scoreDocs.length && i < first + pageSize; i++) {
                    Email email = new Email(annotations, is.doc(hits.scoreDocs[i].doc));
                    email.setScore(hits.scoreDocs[i].score);
                    data.add(email);
                    map.put(email.getId(), email);
                    if (i == first) {
                        firstEmail = email;
                    }
                }
                setRowCount(hits.totalHits);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setRowCount(0);
        }
    }

    private void predict(int first, int pageSize, SortOrder sortOrder) {
        data.clear();
        map.clear();
        Stream<Pair<Integer, Float>> stream = range(0, predictions.length).
                boxed().map(i -> Pair.of(i, predictions[i]));
        if (sortOrder != null) {
            if (sortOrder.equals(DESCENDING)) {
                stream = stream.sorted(comparing(Pair::getRight, reverseOrder()));
            } else {
                stream = stream.sorted(comparing(Pair::getRight));
            }
        }
        List<Integer> selected = stream.skip(first).limit(pageSize).
                map(Pair::getLeft).collect(toList());
        for (int i = 0; i < selected.size(); i++) {
            try {
                Email email = new Email(annotations, is.doc(selected.get(i)));
                email.setScore(predictions[selected.get(i)]);
                data.add(email);
                map.put(email.getId(), email);
                if (i == 0) {
                    firstEmail = email;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setRowCount(predictions.length);
    }

    private void activeLearning() {
        data.clear();
        map.clear();
        List<Integer> candidates = range(0, predictions.length).boxed().
                map(i -> Pair.of(i, Math.abs(predictions[i]))).
                sorted(comparing(Pair::getRight)).
                map(Pair::getLeft).
                collect(toList());
        
        for (int candidate : candidates) {
            try {
                firstEmail = new Email(annotations, is.doc(candidate));
                if (!annotations.containsKey(firstEmail.getId())) {
                    firstEmail.setScore(predictions[candidate]);
                    data.add(firstEmail);
                    map.put(firstEmail.getId(), firstEmail);
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        setRowCount(1);
    }

    @Override
    public List<Email> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        if (predictions != null) {
            if (activeLearning) {
                activeLearning();
            } else {
                predict(first, pageSize, sortOrder);
            }
        } else if (q != null && !q.isEmpty()) {
            MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{SUBJECT, BODY_TEXT, ATTACHMENT_PARSED, FROM, FILE_NAME, TO, CC, BCC}, new EnglishAnalyzer());
            Query query = null;
            try {
                query = parser.parse(q);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            process(query, first, pageSize, sortField, sortOrder);
        } else {
            BooleanQuery.Builder bQuery = new BooleanQuery.Builder();
            if (annotations != null) {
                annotations.keySet().stream().
                        forEach(id -> bQuery.add(new TermQuery(new Term(MESSAGE_ID, id)),
                        BooleanClause.Occur.SHOULD));
            }
            process(bQuery.build(), first, pageSize, sortField, sortOrder);
        }
        return data;
    }

    /**
     * @return the firstEmail
     */
    public Email getFirstEmail() {
        return firstEmail;
    }
}
