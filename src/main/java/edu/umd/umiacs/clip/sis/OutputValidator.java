package edu.umd.umiacs.clip.sis;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.io.File;

/**
 *
 * @author Mossaab Bagdouri
 */
public class OutputValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!value.startsWith("/")) {
            throw new ParameterException("Please specify absolute output folder. You indicated (" + value + ").");
        }
        File output = new File(value);
        if (output.isFile() || (output.isDirectory() && output.list().length > 0)) {
            throw new ParameterException(value + " already exists and is not empty.");
        }
    }
}
