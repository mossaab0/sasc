package edu.umd.umiacs.clip.sis;


import com.beust.jcommander.IStringConverter;
import java.util.List;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

/**
 *
 * @author Mossaab Bagdouri
 */
public class TermsConverter implements IStringConverter<List<String>> {

    @Override
    public List<String> convert(String files) {
        return Stream.of(files.split(",")).collect(toList());
    }
}
