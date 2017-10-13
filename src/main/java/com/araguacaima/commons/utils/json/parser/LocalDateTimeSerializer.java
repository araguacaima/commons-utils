package com.araguacaima.commons.utils.json.parser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class LocalDateTimeSerializer extends StdScalarSerializer<LocalDateTime> {

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-DD\'T\'hh:mm:ss.sZZ");

    public LocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    protected LocalDateTimeSerializer(Class<LocalDateTime> t) {
        super(t);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        jgen.writeString(DATE_FORMAT.print(value));
    }
}