package com.araguacaima.commons.utils.json.parser;

import com.araguacaima.commons.utils.StringUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class EnumSerializer extends StdScalarSerializer<Enum> {

    private final static DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("YYYY-MM-DD\'T\'hh:mm:ss.sZZ");

    public EnumSerializer() {
        super(Enum.class);
    }

    protected EnumSerializer(Class<Enum> t) {
        super(t);
    }

    @Override
    public void serialize(Enum value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException {
        if (value == null) {
            jgen.writeString(StringUtils.EMPTY);
        } else {
            jgen.writeString(StringUtils.defaultIfEmpty(value.name(), StringUtils.EMPTY));
        }
    }
}