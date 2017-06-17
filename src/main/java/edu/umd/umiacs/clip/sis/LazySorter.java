package edu.umd.umiacs.clip.sis;

import java.util.Comparator;
import org.primefaces.model.SortOrder;

/**
 *
 * @author Mossaab Bagdouri
 */
public class LazySorter implements Comparator<Email> {

    private final String sortField;
    private final SortOrder sortOrder;

    public LazySorter(String sortField, SortOrder sortOrder) {
        this.sortField = sortField;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(Email car1, Email car2) {
        try {
            Object value1 = Email.class.getField(this.sortField).get(car1);
            Object value2 = Email.class.getField(this.sortField).get(car2);

            int value = ((Comparable) value1).compareTo(value2);

            return SortOrder.ASCENDING.equals(sortOrder) ? value : -1 * value;
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
