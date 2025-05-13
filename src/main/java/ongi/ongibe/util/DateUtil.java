package ongi.ongibe.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

    public static YearMonth parseOrNow(String yearMonth){
        if (yearMonth == null || yearMonth.isBlank()){
            return YearMonth.now();
        }
        return YearMonth.parse(yearMonth, yearMonthFormatter);
    }

    public static LocalDateTime getStartOfMonth(String yearMonth) {
        YearMonth ym = parseOrNow(yearMonth);
        return ym.atDay(1).atStartOfDay();
    }

    public static LocalDateTime getEndOfMonth(String yearMonth) {
        YearMonth ym = parseOrNow(yearMonth);
        return ym.atEndOfMonth().atTime(LocalTime.MAX);
    }

    public static String getPreviousYearMonth(String yearMonth) {
        YearMonth ym = parseOrNow(yearMonth);
        return ym.minusMonths(1).format(yearMonthFormatter);
    }
}
