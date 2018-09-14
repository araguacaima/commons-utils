package com.araguacaima.commons.utils.filter;

import com.github.bohnman.squiggly.context.provider.AbstractSquigglyContextProvider;
import com.github.bohnman.squiggly.parser.SquigglyParser;
import net.jcip.annotations.ThreadSafe;

/**
 * Provider implementation that just takes a fixed filter expression.
 */
@ThreadSafe
public class CommonSquigglyContextProvider extends AbstractSquigglyContextProvider {

    private String filter;

    public CommonSquigglyContextProvider(SquigglyParser parser) {
        super(parser);
    }

    @Override
    public boolean isFilteringEnabled() {
        if (filter == null) {
            return false;
        }

        if ("**".equals(filter)) {
            return false;
        }

        return true;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    protected String getFilter(Class beanClass) {
        return filter;
    }
}
