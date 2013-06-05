package com.github.jberkel.whassup;

import java.io.File;

public interface DBProvider {
    /** @return the current DB, which is guaranteed to be present and readable */
    File getDBFile();
}
