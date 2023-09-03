package com.lichenaut.kuahelper.util;

import com.lichenaut.kuahelper.KUAHelper;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

public class KHFilter extends AbstractFilter {

    private final HashSet<String> validUnis;

    public KHFilter(KUAHelper plugin) {
        try {validUnis = Files.readAllLines(Path.of(plugin.getDataFolder() + File.separator + "valid_mails.txt"), StandardCharsets.UTF_8)
                .stream()
                .map(line -> line.split(",", 2)[0])
                .collect(HashSet::new, HashSet::add, HashSet::addAll);
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    @Override
    public Result filter(LogEvent event) {
        String msg = event.getMessage().getFormattedMessage();
        if (msg.contains("/send") || msg.contains("ERROR]: [BuycraftX]") // Gets rid of BuycraftX bug messages
                || validUnis.stream().anyMatch(uni -> event.getMessage().getFormattedMessage().contains("@" + uni))) return Result.DENY;
        return Result.NEUTRAL;
    }
}
