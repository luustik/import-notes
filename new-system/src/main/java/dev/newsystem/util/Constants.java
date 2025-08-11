package dev.newsystem.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String GET_NOTES_OLD_SYSTEM_ENDPOINT = "/notes";
    public static final String GET_CLIENTS_OLD_SYSTEM_ENDPOINT = "/clients";

    public static final String OLD_STATUS_ACTIVE = "ACTIVE";
    public static final short NEW_STATUS_ACTIVE = 200;
}
