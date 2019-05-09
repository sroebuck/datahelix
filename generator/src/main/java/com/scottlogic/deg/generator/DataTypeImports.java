package com.scottlogic.deg.generator;

import java.util.Collection;
import java.util.Collections;

public class DataTypeImports {
    private final Collection<String> imports;

    public DataTypeImports(Collection<String> imports) {
        this.imports = imports == null ? Collections.emptySet() : imports;
    }

    public Collection<String> getImports() {
        return imports;
    }
}
