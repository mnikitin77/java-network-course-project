package org.mnikitin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

public class MessageValidator {

    private static final String DELIMITER = "\n";
    private static final Logger log = LoggerFactory.getLogger(MessageValidator.class);
    private final Pattern positiveWholeNumberPattern = Pattern.compile("\\d+");

    public boolean validate(String message) {
        var messageElements = message.split(DELIMITER);
        if (messageElements.length == 2 &&
                checkMessage(messageElements[0], messageElements[1])) {
            log.info("Message [{}] is valid", message);
            return true;
        }
        log.warn("Message [{}] is invalid", message);
        return false;
    }

    private boolean checkMessage(String sizePart, String payload) {
        if (sizePart != null && payload != null &&
                positiveWholeNumberPattern.matcher(sizePart).matches()) {
            return Integer.parseInt(sizePart) == payload.length();
        } else {
            return false;
        }
    }
}
