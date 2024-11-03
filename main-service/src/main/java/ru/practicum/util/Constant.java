package ru.practicum.util;

import java.time.LocalDateTime;

public abstract class Constant {
    public static final String FORMATTER = "yyyy-MM-dd HH:mm:ss";
    public static final LocalDateTime CURRENT_TIME = LocalDateTime.now();
    public static final int DESCRIPTION_MAX = 7000;
    public static final int DESCRIPTION_MIN = 20;
    public static final int ANNOTATION_MAX = 2000;
    public static final int ANNOTATION_MIN = 20;
    public static final int TITLE_MAX = 120;
    public static final int TITLE_MIN = 3;
    public static final String APP_NAME = "ewm-main-service";
}
