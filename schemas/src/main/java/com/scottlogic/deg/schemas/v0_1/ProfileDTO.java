package com.scottlogic.deg.schemas.v0_1;

import com.scottlogic.deg.schemas.common.BaseProfile;

import java.util.Collection;
import java.util.Collections;

public class ProfileDTO extends BaseProfile {
    public static final String SchemaVersion = "0.1";

    public Collection<FieldDTO> fields;
    public Collection<RuleDTO> rules;
    public String description;
    public Collection<String> imports;

    public ProfileDTO() {
        super(SchemaVersion);
    }
}
