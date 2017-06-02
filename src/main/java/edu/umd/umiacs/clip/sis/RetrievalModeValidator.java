package edu.umd.umiacs.clip.sis;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 *
 * @author Mossaab Bagdouri
 */
public class RetrievalModeValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        if ((!value.equals("search") && !value.equals("filter"))) {
            throw new ParameterException("Parameter " + name + " accepts only one of the values: search, filter.");
        }
    }
}
