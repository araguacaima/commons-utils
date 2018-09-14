package com.araguacaima.commons.utils.filter;


import com.araguacaima.commons.utils.uri.IUriSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alejandro on 09/12/2014.
 */
public class Uri {
    private IUriSource uri;
    private Path path;
    private QueryString queryString;
    private List<IMessage> messages = new ArrayList<>();


    public Uri(IUriSource uri) {
        if (uri == null) {
            throw new IllegalArgumentException("Uri can not be null");
        }
        this.uri = uri;
    }

    public IUriSource getUri() {
        return uri;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public QueryString getQueryString() {
        return queryString;
    }

    public void setQueryString(QueryString queryString) {
        this.queryString = queryString;
    }

    public String getUrlBase() {
        return uri.getUrlBase();
    }

    public List<IMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<IMessage> messages) {
        this.messages = messages;
    }

    public void addComments(List<IMessage> messages) {
        this.messages.addAll(messages);
    }

    public void addComment(IMessage excelMessage) {
        this.messages.add(excelMessage);
    }
}
