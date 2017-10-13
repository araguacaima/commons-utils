package com.araguacaima.commons.utils.json.parser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class DateTimeSerializer extends StdScalarSerializer<DateTime> {

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");

    public DateTimeSerializer() {
        super(DateTime.class);
    }

    protected DateTimeSerializer(Class<DateTime> t) {
        super(t);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws
            IOException {
        jgen.writeString(DATE_FORMAT.print(value));
    }
}