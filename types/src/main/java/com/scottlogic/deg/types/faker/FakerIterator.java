package com.scottlogic.deg.types.faker;

import java.util.Iterator;
import java.util.function.Supplier;

class FakerIterator implements Iterator<Object> {
    private final Supplier<Object> getNextValue;

    FakerIterator(Supplier<Object> getNextValue) {
        this.getNextValue = getNextValue;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Object next() {
        return getNextValue.get();
    }
}
