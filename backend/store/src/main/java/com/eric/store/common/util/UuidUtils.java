package com.eric.store.common.util;

import java.util.UUID;

public class UuidUtils {
    public static UUID parseUuidOrNull(String string) {
        String s = StringUtils.normalize(string);
        return (s == null) ? null : UUID.fromString(s);
    }
}
