package betterstatusbar.status.data;

public class CalendarData {
    public static final CalendarData DEFAULT = new CalendarData();
    String animal;
    String avoid;
    String cnDay;
    String day;
    String desc;
    String gzDate;
    String gzMonth;
    String gzYear;
    String isBigMonth;
    String lDate;
    String lMonth;
    String lunarDate;
    String lunarMonth;
    String lunarYear;
    String month;
    String oDate;
    String status;
    String suit;
    String term;
    String type;
    String value;
    String year;

    public CalendarData() {

    }

    public CalendarData(BaiduCalendarData calendarData) {
        this.animal = calendarData.animal;
        this.avoid = calendarData.avoid;
        this.cnDay = calendarData.cnDay;
        this.day = calendarData.day;
        this.desc = calendarData.desc;
        this.gzDate = calendarData.gzDate;
        this.gzMonth = calendarData.gzMonth;
        this.gzYear = calendarData.gzYear;
        this.isBigMonth = calendarData.isBigMonth;
        this.lDate = calendarData.lDate;
        this.lMonth = calendarData.lMonth;
        this.lunarDate = calendarData.lunarDate;
        this.lunarMonth = calendarData.lunarMonth;
        this.lunarYear = calendarData.lunarYear;
        this.month = calendarData.month;
        this.oDate = calendarData.oDate;
        this.status = calendarData.status;
        this.suit = calendarData.suit;
        this.term = calendarData.term;
        this.type = calendarData.type;
        this.value = calendarData.value;
        this.year = calendarData.year;
    }

    public String getAnimal() {
        return animal;
    }

    public String getAvoid() {
        return avoid;
    }

    public String getCnDay() {
        return cnDay;
    }

    public String getDay() {
        return day;
    }

    public String getDesc() {
        return desc;
    }

    public String getGzDate() {
        return gzDate;
    }

    public String getGzMonth() {
        return gzMonth;
    }

    public String getGzYear() {
        return gzYear;
    }

    public String getIsBigMonth() {
        return isBigMonth;
    }

    public String getlDate() {
        return lDate;
    }

    public String getlMonth() {
        return lMonth;
    }

    public String getLunarDate() {
        return lunarDate;
    }

    public String getLunarMonth() {
        return lunarMonth;
    }

    public String getLunarYear() {
        return lunarYear;
    }

    public String getMonth() {
        return month;
    }

    public String getoDate() {
        return oDate;
    }

    public String getStatus() {
        return status;
    }

    public String getSuit() {
        return suit;
    }

    public String getTerm() {
        return term;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getYear() {
        return year;
    }
}
