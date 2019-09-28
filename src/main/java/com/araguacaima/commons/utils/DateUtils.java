/*
 * Copyright 2017 araguacaima
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.araguacaima.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Clase utilitaria para manipular fechas
 * <br>
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
    private static final Logger log = LoggerFactory.getLogger(DateUtils.class);
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
    public DateUtils() {
    }

    public String completeDateFirst(String date) {
        return completeDate(date, FIRST_SECOND);
    }

    public String completeDate(String date, String time) {
        if (null == date) {
            return null;
        } else if (date.length() == SHORT_DATE_LENGTH) {
            log.debug("Agregandole la hora '" + time + "' a la fecha '" + date + "'.");
            return date + time;
        } else {
            log.debug("No se reconoce '" + date + "' como una fecha corta valida.");
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
                log.error("Error parsing date '" + fecha + "' with format '" + sdf.toPattern() + "'." + ex.getMessage
                        ()); // toString
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
            log.error("Error parsing date '" + fecha + "' with format '" + sdf.toPattern() + "'." + ex.getMessage());
            // toString
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
            log.error("No se consiguio valor para la propiedad '" + propertyName + "'.  Usando el valor por " + "" +
                    "" + "" + "" + "" + "" + "" + "" + "defecto '" + pattern + "'.");
        }
        sdf = new SimpleDateFormat(pattern); // dd-MM-yyyy

        propertyName = "date.time";
        pattern = SystemInfo.get(propertyName);
        if (null == pattern) {
            pattern = "HH:mm:ss";
            log.error("No se consiguio valor para la propiedad '" + propertyName + "'.  Usando el valor por " + "" +
                    "" + "" + "" + "" + "" + "" + "" + "defecto '" + pattern + "'.");
        }
        sdft = new SimpleDateFormat(pattern); // HH:mm:ss

        propertyName = "date.long";
        pattern = SystemInfo.get(propertyName);
        if (null == pattern) {
            pattern = "dd-MM-yyyy hh:mm:ss, yyyy-MM-dd hh:mm:ss";
            log.error("No se consiguio valor para la propiedad '" + propertyName + "'.  Usando el valor por " + "" +
                    "" + "" + "" + "" + "" + "" + "" + "defecto '" + pattern + "'.");
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
                        formattedDate) || (StringUtils.isBlank(formattedDate));
    }

    public void main(String[] args) {
        /*
        Calendar cal1 = new GregorianCalendar();
        Calendar cal2 = new GregorianCalendar();

        cal1.set(2008, 8, 1);
        cal2.set(2008, 9, 5);
        log.debug("Days= " + daysBetween(cal1.getTime(), cal2.getTime()));
        */
        log.debug("new Date(1) = " + new Date(1328098453000L));
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
            log.error("La fecha '" + date + "' no cumple con el formato '" + format + "'.");
            return false;
        }
    }
}
