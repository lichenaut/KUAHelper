package com.lichenaut.kuahelper.util;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;

public class KHFilter extends AbstractFilter {

    @Override
    public Result filter(LogEvent event) {
        if (event.getMessage().getFormattedMessage().contains("/send")) return Result.DENY;
        return Result.NEUTRAL;
    }
}
