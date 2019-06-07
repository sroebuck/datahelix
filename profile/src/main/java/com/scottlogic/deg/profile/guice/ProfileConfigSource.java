package com.scottlogic.deg.profile.guice;

import java.io.File;
import java.nio.file.Path;

public interface ProfileConfigSource {
    File getProfileFile();
    boolean isSchemaValidationEnabled();
    Path getFirstNamePath();
    Path getLastNamePath();
}
