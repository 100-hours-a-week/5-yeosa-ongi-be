package ongi.ongibe.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    public static LocalDateTime getStartOfMonth(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, yearMonthFormatter);
        return ym.atDay(1).atStartOfDay();
    }

    public static LocalDateTime getEndOfMonth(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, yearMonthFormatter);
        return ym.atEndOfMonth().atTime(LocalTime.MAX);
    }
}
