package com.araguacaima.commons.utils.json.parser;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class DateTimeDeserializer extends StdScalarDeserializer<DateTime> {
    private final static DateTimeFormatter DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd\'T\'HH:mm:ss.SSSZZ");

    public DateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public DateTime deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        String dateStr = null;
        String timeStr = null;
        String fieldName = null;

        while (jp.hasCurrentToken()) {
            JsonToken token = jp.nextToken();
            if (token == JsonToken.FIELD_NAME) {
                fieldName = jp.getCurrentName();
            } else if (token == JsonToken.VALUE_STRING) {
                if (StringUtils.equals(fieldName, "date")) {
                    dateStr = jp.getValueAsString();
                } else if (StringUtils.equals(fieldName, "time")) {
                    timeStr = jp.getValueAsString();
                } else {
                    throw new JsonParseException("Unexpected field name", jp.getTokenLocation());
                }
            } else if (token == JsonToken.END_OBJECT) {
                break;
            }
        }
        if (dateStr != null && timeStr != null) {
            return DateTime.parse(dateStr + "T" + timeStr, DATETIME_FORMAT);
        }
        return null;
    }
}