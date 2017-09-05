package org.araguacaima.utils;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Clase utilitaria para manipular fechas <p>
 * Title: DateUtil.java <br>
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */

// TODO: This class uses a lot of hardcoded spanish words...

@Component
public class DateUtils {

    public static final String EMPTY_DATE_FE = "DD-MM-AAAA"; // Valor a mostrar en la pantalla por defecto
    public static final String EMPTY_DATE_FE_HHMMSS = EMPTY_DATE_FE + " HH:MM:SS";
    public static final String FIRST_SECOND = " 00:00:00";
    public static final String LAST_SECOND = " 23:59:59";
    public static final int LONG_DATE_LENGTH = 19;
    public static final long MILLISECONDS_IN_DAY = 3600L * 24L * 1000L;
    public static final int SHORT_DATE_LENGTH = 10;
    // Formato para crear objetos Date desde javascript
    public final SimpleDateFormat sdfjs = new SimpleDateFormat("yyyy,MM,dd");
    // Formato para manejo de nombres de archivos.  No formatear fechas con este formato.
    public final SimpleDateFormat sdfl = new SimpleDateFormat("yyyyMMddHHmmss");
    // Para usar en la carga masiva
    public final SimpleDateFormat sdfs = new SimpleDateFormat("yyyyMMdd");
    // Para usar en las trazas de la carga masiva
    public final SimpleDateFormat sdfslash = new SimpleDateFormat("yyyy/MM/dd");
    public final SimpleDateFormat sdfus = new SimpleDateFormat("yyyy-MM-dd");
    // Formato de Fecha standard
    //    public  final SimpleDateFormat sdf = new SimpleDateFormat(SystemInfo.get("date.short")); //
    // dd-MM-yyyy
    public SimpleDateFormat sdf; // dd-MM-yyyy
    // Formato de Fecha y Hora standard
    //    public  final SimpleDateFormat sdfe = new SimpleDateFormat(
    //            SystemInfo.get("date.long")); // dd-MM-yyyy hh:mm:ss, yyyy-MM-dd hh:mm:ss
    public SimpleDateFormat sdfe; // dd-MM-yyyy hh:mm:ss, yyyy-MM-dd hh:mm:ss
    // Formato para manejo de tiempo (Horas, Minutos, Segundos)
    //    public  final SimpleDateFormat sdft = new SimpleDateFormat(SystemInfo.get("date.time")); //
    // HH:mm:ss
    public SimpleDateFormat sdft; // HH:mm:ss

    /**
     * Constructor
     */
    private DateUtils() {
    }

    public String completeDateFirst(String date) {
        return completeDate(date, FIRST_SECOND);
    }

    public String completeDate(String date, String time) {
        if (null == date) {
            return null;
        } else if (date.length() == SHORT_DATE_LENGTH) {
            System.out.println("Agregandole la hora '" + time + "' a la fecha '" + date + "'.");
            return date + time;
        } else {
            System.out.println("No se reconoce '" + date + "' como una fecha corta valida.");
            return date;
        }
    }

    public String completeDateLast(String date) {
        return completeDate(date, LAST_SECOND);
    }

    public int daysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (MILLISECONDS_IN_DAY));
    }

    public String format(Date date, String format) {
        SimpleDateFormat sdf_ = new SimpleDateFormat(format);
        sdf_.setLenient(false);
        return sdf_.format(date);
    }

    public String formatDate(Date date) {
        return (date == null) ? null : sdf.format(date);
    }

    public String formatDateTime(Date date) {
        return (date == null) ? null : sdfe.format(date);
    }

    // Inicio metodos de SICAM
    // TODO: Sincerar que metodos se quedan

    public Date getDateByString(String fecha, boolean useUSFormat) {
        if (useUSFormat) {
            try {
                return sdfus.parse(fecha);
            } catch (Exception ex) {
                System.err.println("Error parsing date '" + fecha + "' with format '" + sdf.toPattern() + "'." + ex
                        .getMessage()); // toString
                return null;
            }
        } else {
            return getDateByString(fecha);
        }
    }

    public Date getDateByString(String fecha) {
        try {
            return sdf.parse(fecha);
        } catch (Exception ex) {
            System.err.println("Error parsing date '" + fecha + "' with format '" + sdf.toPattern() + "'." + ex
                    .getMessage()); // toString
            return null;
        }
    }

    public Date getDateWithZeroHour(Date originalDate) {
        if (null == originalDate) {
            return null;
        }
        Calendar calen = new GregorianCalendar();
        calen.setTime(originalDate);
        calen.set(Calendar.HOUR_OF_DAY, 0);
        calen.set(Calendar.MINUTE, 0);
        calen.set(Calendar.SECOND, 0);
        return calen.getTime();
    }

    /**
     * Gets a date in java.sql.Date for a java.util.Date date given.
     *
     * @param date java.util.Date object
     * @return return java.sql.Date object
     */

    public java.sql.Date getSqlDate(Date date) {
        return (date == null) ? null : new java.sql.Date(date.getTime());
    }

    // Final metodos de SICAM

    /**
     * TODO: Analizar el comportamiento de lenient
     * Si no se coloca setLenient(false) en los DateFormat, el parser tratar de manejar los errores.
     * Por ejemplo, si le llega un mes 15, le resta los 12 de exceso y devuelve el mes 3.
     * Al colocar setLenient(false) un mes 15 dara error siempre.
     */
    @PostConstruct
    public void init() {

        String propertyName = "date.short";
        String pattern = SystemInfo.get(propertyName);
        if (null == pattern) {
            pattern = "dd-MM-yyyy";
            System.err.println("No se consiguio valor para la propiedad '" + propertyName + "'.  Usando el valor por " +
                    "" + "" + "" + "" + "" + "defecto '" + pattern + "'.");
        }
        sdf = new SimpleDateFormat(pattern); // dd-MM-yyyy

        propertyName = "date.time";
        pattern = SystemInfo.get(propertyName);
        if (null == pattern) {
            pattern = "HH:mm:ss";
            System.err.println("No se consiguio valor para la propiedad '" + propertyName + "'.  Usando el valor por " +
                    "" + "" + "" + "" + "" + "defecto '" + pattern + "'.");
        }
        sdft = new SimpleDateFormat(pattern); // HH:mm:ss

        propertyName = "date.long";
        pattern = SystemInfo.get(propertyName);
        if (null == pattern) {
            pattern = "dd-MM-yyyy hh:mm:ss, yyyy-MM-dd hh:mm:ss";
            System.err.println("No se consiguio valor para la propiedad '" + propertyName + "'.  Usando el valor por " +
                    "" + "" + "" + "" + "" + "defecto '" + pattern + "'.");
        }
        sdfe = new SimpleDateFormat(pattern); // dd-MM-yyyy hh:mm:ss, yyyy-MM-dd hh:mm:ss

        sdf.setLenient(false);
        sdfe.setLenient(false);
        sdft.setLenient(false);
        sdfl.setLenient(false);
        sdfjs.setLenient(false);
        sdfs.setLenient(false);
        sdfslash.setLenient(false);
    }

    public boolean isEmpty(String formattedDate) {
        return DateUtils.EMPTY_DATE_FE.equalsIgnoreCase(formattedDate) || DateUtils.EMPTY_DATE_FE_HHMMSS
                .equalsIgnoreCase(
                formattedDate) || (StringUtils.isEmpty(formattedDate));
    }

    public void main(String[] args) {
        /*
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();

        cal1.set(2008, 8, 1);
        cal2.set(2008, 9, 5);
        System.out.println("Days= " + daysBetween(cal1.getTime(), cal2.getTime()));
        */
        System.out.println("new Date(1) = " + new Date(1328098453000L));
    }

    public String now() {
        Calendar calen = new GregorianCalendar();
        String hour = String.valueOf(calen.get(Calendar.HOUR_OF_DAY));
        String minutes = String.valueOf(calen.get(Calendar.MINUTE));
        String seconds = String.valueOf(calen.get(Calendar.SECOND));
        if (minutes.length() < 2) {
            minutes = "0" + minutes;
        }
        if (seconds.length() < 2) {
            seconds = "0" + seconds;
        }
        return hour + ":" + minutes + ":" + seconds;
    }

    public String today() {
        SimpleDateFormat sdf_ = new SimpleDateFormat("dd'/'MM'/'yyyy", new Locale("es_ES"));
        sdf_.setLenient(false);
        return sdf_.format(new Date());
    }

    // TODO: Cambiar este metodo para que use Locale para obtener los nombres de los dias y meses.
    public String todayFullString() {

        final Calendar instance = Calendar.getInstance();
        int year = instance.get(Calendar.YEAR);
        if (year < 1000) {
            year += 1900;
        }
        int day = instance.get(Calendar.DAY_OF_MONTH);
        int month = instance.get(Calendar.MONTH);
        String daym = "" + day;
        if (day < 10) {
            daym = "0" + day;
        }
        String[] dayarray = new String[]{"Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"};
        String[] montharray = new String[]{"Enero",
                "Febrero",
                "Marzo",
                "Abril",
                "Mayo",
                "Junio",
                "Julio",
                "Agosto",
                "Septiembre",
                "Octubre",
                "Noviembre",
                "Diciembre"};

        return (dayarray[day] + " " + daym + " de " + montharray[month] + " de " + year);
    }

    public boolean validateFormat(String date, String format) {
        try {
            SimpleDateFormat sdf_ = new SimpleDateFormat(format);
            sdf_.setLenient(false);
            sdf_.parse(date);
            return date.length() == format.length();
        } catch (Exception e) {
            System.err.println("La fecha '" + date + "' no cumple con el formato '" + format + "'.");
            return false;
        }
    }

    //    public  Date getCurrentDate() {
    //        return getCurrentDate(SystemInfo.getAsBoolean("date.addOffset"));
    //    }

    //    public  Date getCurrentDate(boolean addOffset) {
    //        // Falta por acomodar la condicion...
    //        return new Date(System.currentTimeMillis() - SystemInfo.getAsInt(BCVBusinessConstants
    // .CCS_TIMEZONE_OFFSET));
    //    }

}

//    /**
//     * It gets a date and roughlly translates it into something in the form "1 de Enero de 2005"
//     *
//     * @param date Date
//     * @return Date
//     * @see java.text.SimpleDateFormat api
//     *      http://java.sun.com/j2se/1.4.2/docs/api/java/text/SimpleDateFormat.html
//     * @deprecated use DateUtil.getLocalizedFormatLong().format(date) instead.
//     */
//    public  String translateMonth(Date date) {
//        return getLocalizedFormatLong().format(date);
//    }
//
//    /**
//     * Allow add days a given Date object
//     *
//     * @param date  Date
//     * @param nDays int
//     * @return java.util.Date
//     */
//    public  Date sumDaysToDate(Date date, int nDays) {
//        Calendar calendar = getLocalizedCalendar(date);
//        calendar.add(Calendar.DATE, nDays);
//        return calendar.getTime();
//    }
//
//    /**
//     * It returns the days between two given dates.
//     *
//     * @param startDate Date
//     * @param endDate   Date
//     * @return long
//     */
//    public  long daysBetween(Date startDate, Date endDate) {
////        return (endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000);
//
//        return daysBetween(getGregorianDay(startDate), getGregorianDay(endDate));
//    }
//
//    /**
//     * It returns the days between two given dates with the String format "yyyy-MM-dd"
//     *
//     * @param startDate String
//     * @param endDate   String
//     * @return long
//     */
//    public  long daysBetween(String startDate, String endDate) {
//        try {
//            return daysBetween(getFormat().parse(startDate), getFormat().parse(endDate));
//        } catch (ParseException e) {
//            log.error("Error parsing the given strings. Returning 0 instead.", e);
//            return 0;
//        }
//
//        //return daysBetween(getGregorianDay(startDate), getGregorianDay(endDate));
//    }
//
//    public  long daysBetween(long startGregorianDay, long endGregorianDay) {
//        return endGregorianDay - startGregorianDay;
//    }
//
//    /**
//     * It returns the years between two given dates.
//     *
//     * @param startDate Date
//     * @param endDate   Date
//     * @return long
//     */
//    public  long yearsBetween(Date startDate, Date endDate) {
//        return (getOnlyYearMonthDay(endDate).getTime() - getOnlyYearMonthDay(startDate).getTime()) / (24 * 60 * 60
// * 1000) / 365;
//    }
//
//    /**
//     * Allow add one second a given Date object
//     *
//     * @param date Date
//     * @return java.util.Date
//     */
//    public  Date sumSecondToDate(Date date) {
//        Calendar calendar = getLocalizedCalendar();
//        calendar.setTime(date);
//        calendar.add(Calendar.SECOND, 1);
//        return calendar.getTime();
//    }
//
//    /**
//     * Allow to get the same Date given without specifying its minutes and seconds.
//     * This is:
//     * date = yyyy-MM-dd mm:ss
//     * returned date = yyyy-MM-dd 00:00.0
//     *
//     * @param date Date
//     * @return java.util.Date
//     * @deprecated use {@link #getOnlyYearMonthDay(java.util.Date)} instead
//     */
//    public  Date getBareDate(Date date) /*throws ParseException*/ {
//        return getOnlyYearMonthDay(date);
//        //return DateUtil.getFormat().parse(DateUtil.getFormat().format(date));
//    }
//
//    /**
//     * Get the days based on the number of milliseconds that have passed since January 1, 1970 00:00:00.000 GMT.
//     *
//     * @param Y the year
//     * @param M the month
//     * @param D the day
//     * @return the days for that specific date as a BigDecimal
//     */
//    public  BigDecimal getDays(int Y, int M, int D) {
//        if (M == 1 || M == 2) {
//            Y--;
//            M += 12;
//        }
//
//        long A = Y / 100;
//        long B = A / 4;
//        long C = 1 - A - B;
//        double E = 365.25 * (Y + 4716);
//        double F = 30.6001 * (M + 1);
//
//        double days = C + D + E + F - 1524.5;
//        days -= (days % 1);
//
//        return new BigDecimal(days);
//    } // end getDays(...) method
//
//    /**
//     * Get parameter date and set current system time
//     *
//     * @param date The date to set the time to the system time
//     * @return a new Date object containing the time that was set.
//     * @see java.sql.Date
//     * @deprecated This seems like an attempt to add time to java.sql.Date objects. This makes no sense.
//     */
//    public  Date getDateWithCurrentSystemTime(Date date) {
//        Calendar result = getLocalizedCalendar(date);
//        Calendar system = getLocalizedCalendar();
//
//        result.set(Calendar.HOUR, system.get(Calendar.HOUR));
//        result.set(Calendar.MINUTE, system.get(Calendar.MINUTE));
//        result.set(Calendar.SECOND, system.get(Calendar.SECOND));
//        result.set(Calendar.AM_PM, system.get(Calendar.AM_PM));
//
//        return result.getTime();
//    }
//
//    /**
//     * This method transforms de Date argument object in one equals but with 00:00:00
//     * (hours:minutes:seconds).
//     *
//     * @param dateIn The date to transform.
//     * @return The transformed date.
//     */
//    public  Date getOnlyYearMonthDay(java.util.Date dateIn) {
//        Calendar calendar = getLocalizedCalendar();
//        calendar.setTime(dateIn);
//        calendar.set(Calendar.HOUR, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        return calendar.getTime();
//
//    }
//
//    public  Date getSystemDateOnlyYearMonthDay() {
//        return getOnlyYearMonthDay(new Date());
//    }
//
//    public  long getGregorianDay(Date date) {
//        return getGregorianDay(DateUtil.getFormat().format(date));
//    }
//
//    /**
//     * @param date String
//     * @return long
//     */
//    public  long getGregorianDay(String date) {
////            if ((StringUtil.isEmptyOrNullValue(date)) || (CONTROL_DATE.equals(date))) {
//        if (StringUtil.isEmptyOrNullValue(date)) {
//            return 0;
//        } else if (CONTROL_DATE.equals(date)) {
//            return CONTROL_DATE_NUMBER;
//        } else {
//            return calculateGregorianDate(date);
//        }
//
////        }
//    }
//
//    /**
//     * Don't use this method.  Call getGregorianDay(String date) instead.
//     *
//     * @param date String
//     * @return long
//     */
//    private  long calculateGregorianDate(String date) {
//        try {
//            java.util.Date value = getFormat().parse(date);
//            GregorianCalendar calendar = new GregorianCalendar();
//            calendar.setTime(value);
//            int year = calendar.get(Calendar.YEAR);
//            int month = calendar.get(Calendar.MONTH) + 1;
//
//            String dayString = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
//
//            StringUtil.LPadString(dayString, 2, "0");
//
//            int day = Integer.parseInt(dayString);
//
//            return getGregorianDay(year, month, day);
//        } catch (Exception e) {
//            log.error("Error parsing date: '" + date + "'.  Returning value '0'.", e);
//            return 0;
//        }
//    }
//
//    /**
//     * It calculates the Gregorian day associated with the given date
//     * It works only for dates in which the year is greater than 1538
//     *
//     * @param year  int
//     * @param month int
//     * @param day   int
//     * @return millisPrimeraFecha
//     */
//    private  long getGregorianDay(int year, int month, int day) {
//        long a = ipart((14 - month) / 12);
//        long y = year + 4800 - a;
//        long m = month + 12 * a - 3;
//        return day + ipart((153 * m + 2) / 5) + y * 365 + ipart(y / 4) - ipart(y / 100) +
//                ipart(y / 400) - 32045;
//    }
//
//    /**
//     * get the given amount rounded
//     *
//     * @param r double
//     * @return given amount rounded
//     */
//    private  long ipart(double r) {
//        return Math.round(r - 0.5);
//    }
//
//    /**
//     * Obtains the sysdate and converts it to string
//     *
//     * @return Sysdate in String
//     */
//    public  String getSystemDateString() {
//        return getFormat().format(getLocalizedCalendar().getTime());
//    }
//
//    /**
//     * Obtains the sysdate
//     *
//     * @return Sysdate
//     * @deprecated you should create a new java.util.Date object, or use java.lang.System.currentTimeMillis()
//     */
//    public  Date getSystemDate() {
//        return new Date();
//    }
//
//    /**
//     * @param year int
//     * @return boolean
//     */
//    public  boolean bisiesto(int year) {
//        return (((year % 400) == 0) || ((year > 1582) && ((year % 100) == 0)) || ((year % 4) == 0));
//    }
//
//    /**
//     * Calculate the span in number of days given two dates. The number returned is always positive.
//     *
//     * @param d1 Date
//     * @param d2 Date
//     * @return days in an int-type
//     * @deprecated should use {@link #daysBetween(java.util.Date, java.util.Date)} instead.
//     */
//    public  int getDaysBetween(Date d1, Date d2) {
//        return (int) Math.abs(daysBetween(d1, d2));
//    }
//
//    /**
//     * It obtains the previous or later date of 'days' day of the present date
//     *
//     * @param date date
//     * @param days day
//     * @return date previous ot later
//     * @deprecated use {@link #sumDaysToDate(java.util.Date, int)} instead
//     */
//    public  Date sumDayToDate(Date date, long days) {
//        Calendar calendar = getLocalizedCalendar(date);
//        calendar.add(Calendar.DAY_OF_MONTH, (int) days);
//        return (calendar.getTime());
//    }
//
//    /**
//     * returns the string with the date formatted according to the system
//     *
//     * @param year  int
//     * @param month int
//     * @param day   int
//     * @return String
//     */
//    public  String getDateString(int year, int month, int day) {
//        String format = getFormatDate();
//        String separator = getSeparatorFormat(format);
//        String newSeparator;
//
//        if (separator.equals(".")) {
//            newSeparator = "\\.";
//        } else {
//            newSeparator = separator;
//        }
//        String[] patrons = format.split(newSeparator);
//
//        StringBuffer date = new StringBuffer();
//
//        for (int i = 0; i < patrons.length; i++) {
//            String value = patrons[i].toUpperCase();
//            if (value.startsWith("M")) {
//                if (value.length() == 3) {
//                    date.append(getMonthsShortNames()[month - 1]);
//                } else {
//                    date.append(StringUtil.LPadString(String.valueOf(month), 2, "0"));
//                }
//
//            }
//            if (value.startsWith("D")) {
//                date.append(StringUtil.LPadString(String.valueOf(day), 2, "0"));
//            }
//            if (value.startsWith("Y")) {
//                date.append(year);
//            }
//            if ((i + 1) != patrons.length) {
//                date.append(separator);
//            }
//        }
//
//        return date.toString();
//    }
//
//    /**
//     * @param year  int
//     * @param month in format between 1 and 12
//     * @param day   int
//     * @return Date
//     */
//    public  Date getDate(int year, int month, int day) {
//        Calendar calendar = getLocalizedCalendar();
//        calendar.set(year, month - 1, day, 0, 0, 0);
//        return calendar.getTime();
//    }
//
//    public  Date getDate(int year, int month, int day, int hour, int minute, int second) {
//        Calendar calendar = getLocalizedCalendar();
//        calendar.set(year, month - 1, day, hour, minute, second);
//        return calendar.getTime();
//    }
//
//    private  SimpleDateFormat createFormatter(String format) {
////        log.debug("Date format = '" + format + "'");
//        return new SimpleDateFormat(format, getLocale());
//    }
//
//    public  String getFormatDate() {
//        return getFormat().toLocalizedPattern();
//    }
//
//    public  SimpleDateFormat getFormat() {
//        return createFormatter(getIsoDateFormat());
//    }
//
//    public  SimpleDateFormat getFormatLong() {
//        String format = getIsoDateFormat();
//        if (!format.matches("HH:mm:ss")) {
//            format += " HH:mm:ss";
//        }
//        return createFormatter(format);
//    }
//
//    public  SimpleDateFormat getFormatToShow() {
//        return createFormatter(getDateFormatToShow());
//    }
//
//    public  String getSeparatorFormat() {
//        return getSeparatorFormat(getFormatDate());
//    }
//
//    private  String getSeparatorFormat(String format) {
//        if (format.indexOf(' ') != -1) {
//            return " ";
//        } else if (format.indexOf('/') != -1) {
//            return "/";
//        } else if (format.indexOf('.') != -1) {
//            return ".";
//        } else if (format.indexOf('-') != -1) {
//            return "-";
//        }
//        return "";
//    }
//
//    public  String getJavaScriptFormatDate() {
//        String format = getFormatDate();
//        String separator = getSeparatorFormat(format);
//
//        String[] patrons = format.split(separator);
//
//        StringBuffer jsFormat = new StringBuffer();
//
//        for (int i = 0; i < patrons.length; i++) {
//            String value = patrons[i].toUpperCase();
//            if (value.equals("MMM")) {
//                jsFormat.append("MMM");
//            } else if (value.startsWith("M")) {
//                jsFormat.append("M");
//            }
//            if (value.startsWith("D")) {
//                jsFormat.append("d");
//            }
//            if (value.startsWith("Y")) {
//                jsFormat.append("y");
//            }
//            if ((i + 1) != patrons.length) {
//                jsFormat.append(separator);
//            }
//        }
//
//        return jsFormat.toString();
//    }
//
//    public  List loadFormats() {
//        String formats = StringUtil.emptyIfNull(AcseleConf.getProperty("formatsDates"));
//        List list = new ArrayList();
//
//        list.add("yyyy-MM-dd");//formato ISO
//        list.addAll(StringUtil.splitAsList(formats, StringUtil.COMMA));
//
//        list.add("yyyy-MMM-dd");// formato unico.. debe sacarse de una propiedad
//        return list;
//    }
//
//    public  String getDateFormatToShow() {
//        return AcseleConf.getPropertyOrDefault("dateFormatToShow", getIsoDateFormat());
//    }
//
//    public  String getIsoDateFormat() {
//        return "yyyy-MM-dd";
//    }
//
//    public  String getVarIsoDateFormat() {
//        return "var isoDateFormat = '" + getIsoDateFormat() + "';";
//    }
//
//    public  String getVarDateFormatToShow() {
//        return "var dateFormatToShow = '" + getDateFormatToShow() + "';";
//    }
//
//    public  String getValidDateFormats() {
//        StringBuffer varValidDateFormats = new StringBuffer("new Array(");
//        List formatList = loadFormats();
//        for (int i = 0; i < formatList.size(); i++) {
//            varValidDateFormats.append("'");
//            varValidDateFormats.append(formatList.get(i));
//            varValidDateFormats.append("'");
//            if ((formatList.size() - 1) == i) {
//                varValidDateFormats.append(")");
//            } else {
//                varValidDateFormats.append(",");
//            }
//        }
//
//        return varValidDateFormats.toString();
//    }
//
//    public  String getVarValidDateFormats() {
//
//        StringBuffer varValidDateFormats = new StringBuffer("var validDateFormats=new Array(");
//        // String[] formats=DateUtil.getValidDateFormats();
//        List formatList = loadFormats();
//        for (int i = 0; i < formatList.size(); i++) {
//            varValidDateFormats.append("'");
//            //varValidDateFormats.append(formats[i]);
//            varValidDateFormats.append(formatList.get(i));
//            varValidDateFormats.append("'");
//            if ((formatList.size() - 1) == i) {
//                varValidDateFormats.append(");");
//            } else {
//                varValidDateFormats.append(",");
//            }
//        }
//        log.debug("[Acsel-e] varValidDateFormats = " + varValidDateFormats.toString());
//        return varValidDateFormats.toString();
//
//    }
//
//    public  String[] getMonthsShortNames() {
//        DateFormatSymbols dfs = new DateFormatSymbols(getLocale());
//        return dfs.getShortMonths();
//    }
//
//    public  String[] getMonthNames() {
//        Locale locale = getLocale();
//        DateFormatSymbols formatSymbols = new DateFormatSymbols(locale);
//        return formatSymbols.getMonths();
//    }
//
//    public  Locale getLocale() {
//        return (UserInfo.getLocale() == null) ? Locale.getDefault() : UserInfo.getLocale();
//    }
//
//    public  String[] getDayShortNames() {
//        return (new DateFormatSymbols(getLocale())).getShortWeekdays();
//
//    }
//
//    public  String[] getDayNames() {
//        return (new DateFormatSymbols(getLocale())).getWeekdays();
//    }
//
//    public  String getVarDayNames() {
//        StringBuffer varMonths = new StringBuffer("var dayNames=new Array(");
//        String[] months = DateUtil.getDayShortNames();
//        for (int i = 1; i < months.length; i++) {
//            varMonths.append("'");
//            varMonths.append(StringUtil.convertToUnicode(months[i]));
//            varMonths.append("',");
//        }
//        String[] monthNames = DateUtil.getDayNames();
//        for (int i = 1; i < monthNames.length; i++) {
//            varMonths.append("'");
//            varMonths.append(StringUtil.convertToUnicode(monthNames[i]));
//            varMonths.append("'");
//            if (monthNames.length == i + 1) {
//                varMonths.append(");");
//            } else {
//                varMonths.append(",");
//            }
//        }
//        return varMonths.toString();
//    }
//
//    public  String getVarDayShortNames() {
//        StringBuffer varMonths = new StringBuffer("var dayShortNames=new Array(");
//        String[] monthNames = DateUtil.getDayShortNames();
//        for (int i = 1; i < monthNames.length; i++) {
//            varMonths.append("'");
//            varMonths.append(StringUtil.convertToUnicode(monthNames[i]));
//            varMonths.append("'");
//            if (monthNames.length == i + 1) {
//                varMonths.append(");");
//            } else {
//                varMonths.append(",");
//            }
//        }
//        return varMonths.toString();
//    }
//
//    public  String getVarMonthNames() {
//        StringBuffer varMonths = new StringBuffer("var MONTH_NAMES=new Array(");
//        String[] months = DateUtil.getMonthsShortNames();
//        for (int i = 0; i < months.length - 1; i++) {
//            varMonths.append("'");
//            varMonths.append(StringUtil.convertToUnicode(months[i]));
//            varMonths.append("',");
//        }
//        String[] monthNames = DateUtil.getMonthNames();
//        for (int i = 0; i < monthNames.length - 1; i++) {
//            varMonths.append("'");
//            varMonths.append(StringUtil.convertToUnicode(months[i]));
//            varMonths.append("'");
//            if (monthNames.length <= i + 2) {
//                varMonths.append(");");
//            } else {
//                varMonths.append(",");
//            }
//        }
//        return varMonths.toString();
//    }
//
//    public  String getVarOnlyMonthNames() {
//
//        StringBuffer varMonths = new StringBuffer("var MONTH_NAMES_ONLY=new Array(");
//        String[] monthNames = DateUtil.getMonthNames();
//        for (int i = 0; i < monthNames.length - 1; i++) {
//            varMonths.append("'");
//            varMonths.append(StringUtil.convertToUnicode(monthNames[i]));
//            varMonths.append("'");
//            if (monthNames.length <= i + 2) {
//                varMonths.append(");");
//            } else {
//                varMonths.append(",");
//            }
//        }
//        return varMonths.toString();
//    }
//
//    /**
//     * To compare if a date is greater than or equal to another date
//     *
//     * @param d1 Date
//     * @param d2 Date
//     * @return boolean
//     */
//    public  boolean DateGreaterThanOrEqualtoDate(Date d1, Date d2) {
//        return d1.getTime() >= d2.getTime();
//    }
//
//    /**
//     * To compare if a date is less than or equal to another date
//     *
//     * @param d1 Date
//     * @param d2 Date
//     * @return boolean
//     */
//    public  boolean DateLessThanOrEqualtoDate(Date d1, Date d2) {
//        return d1.getTime() <= d2.getTime();
//    }
//
//    /**
//     * To compare if a date is greater than another date
//     *
//     * @param d1 Date
//     * @param d2 Date
//     * @return boolean
//     */
//    public  boolean DateGreaterThanDate(Date d1, Date d2) {
//        return d1.getTime() > d2.getTime();
//    }
//
//    /**
//     * To compare if a date is less than another date
//     *
//     * @param d1 Date
//     * @param d2 Date
//     * @return boolean
//     */
//    public  boolean DateLessThanDate(Date d1, Date d2) {
//        return d1.getTime() < d2.getTime();
//    }
//
//    /**
//     * To compare if a date is between another dates including border
//     *
//     * @param db Date begin
//     * @param dm Date middle
//     * @param de Date end
//     * @return boolean
//     */
//    public  boolean DateBetweenDateInc(Date db, Date dm, Date de) {
//        return (DateGreaterThanOrEqualtoDate(dm, db)) && (DateLessThanOrEqualtoDate(dm, de));
//    }
//
//    /**
//     * To compare if a date is between another dates not including border
//     *
//     * @param db Date begin
//     * @param dm Date middle
//     * @param de Date end
//     * @return boolean
//     */
//    public  boolean DateBetweenDate(Date db, Date dm, Date de) {
//        return (DateGreaterThanDate(dm, db)) && (DateLessThanDate(dm, de));
//    }
//
//    public  String getDateToShow(String value) {
//        try {
//            java.util.Date date = getFormat().parse(value);
//            SimpleDateFormat format = new SimpleDateFormat(getDateFormatToShow(), getLocale());
//            return format.format(date);
//        } catch (ParseException e) {
//            return "";
//        }
//    }
//
//    public  String getDateToShow(java.util.Date date) {
//        SimpleDateFormat format = new SimpleDateFormat(getDateFormatToShow(), getLocale());
//        return format.format(date);
//    }
//
//    public  DateFormat getLocalizedFormatDefault() {
//        return DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale());
//    }
//
//    public  DateFormat getLocalizedFormatShort() {
//        return DateFormat.getDateInstance(DateFormat.SHORT, getLocale());
//    }
//
//    public  DateFormat getLocalizedFormatMedium() {
//        return DateFormat.getDateInstance(DateFormat.MEDIUM, getLocale());
//    }
//
//    /**
//     * It gets a date and roughlly translates it into something in the form "1 de Enero de 2005"
//     */
//    public  DateFormat getLocalizedFormatLong() {
//        return DateFormat.getDateInstance(DateFormat.LONG, getLocale());
//    }
//
//    public  Calendar getLocalizedCalendar() {
//        return Calendar.getInstance(getLocale());
//    }
//
//    public  Calendar getLocalizedCalendar(Date date) {
//        Calendar calendar = getLocalizedCalendar();
//        calendar.setTime(date);
//        return calendar;
//    }
//
//    /**
//     * Use for testing purposes only
//     *
//     * @param args command-line arguments
//     */
//    public  void main(String[] args) {
//        //TODO: move this to a Unit test
//        try {
//
//            log.debug(yearsBetween(getFormat().parse("2008-10-01"), getFormat().parse("2009-10-02")));
//
//
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//            java.util.Date d1 = sdf.parse("2008-01-03");
//            java.util.Date d2 = sdf.parse("2008-01-03");
//
//            log.debug("DateUtil.DateGreaterThanOrEqualtoDate(d1,d2) = " +
//                    DateUtil.DateGreaterThanOrEqualtoDate(d1, d2));
//
//            log.debug("DateUtil.DateLessThanOrEqualtoDate(d1,d2) = " +
//                    DateUtil.DateLessThanOrEqualtoDate(d1, d2));
//
//            log.debug("DateUtil.DateGreaterThanDate(d1,d2) = " +
//                    DateUtil.DateGreaterThanDate(d1, d2));
//
//            log.debug(
//                    "DateUtil.DateLessThanDate(d1,d2) = " + DateUtil.DateLessThanDate(d1, d2));
//
//            d1 = sdf.parse("2008-01-03");
//            d2 = sdf.parse("2008-01-03");
//            java.util.Date d3 = sdf.parse("2008-01-03");
//
//            log.debug(
//                    "DateUtil.DateBetweenDate() = " + DateUtil.DateBetweenDate(d1, d2, d3));
//            log.debug(
//                    "DateUtil.DateBetweenDateInc() = " + DateUtil.DateBetweenDateInc(d1, d2, d3));
//
//            log.debug("DateUtil.getFormatDate() = " + getIsoDateFormat());
//
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
