package com.daniel.automationhero.model;

import java.util.Iterator;
import java.util.Optional;

public class ChunkElement {
    private Iterator<String> chunkIterator;
    private String value;

    public ChunkElement(Iterator<String> chunkStream) {
        this.chunkIterator = chunkStream;
        value = !isEmpty() ? chunkIterator.next() : null;
    }

    public Optional<String> readValue() {
        if (value != null) {
            return Optional.of(value);
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getValueAndPollNew() {
        Optional<String> result = readValue();
        if (chunkIterator.hasNext()) {
            value = chunkIterator.next();
        } else {
            value = null;
        }
        return result;
    }

    public boolean isEmpty() {
        return !chunkIterator.hasNext();
    }
}
