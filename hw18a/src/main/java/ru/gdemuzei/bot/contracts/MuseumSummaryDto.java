package ru.gdemuzei.bot.contracts;

public record MuseumSummaryDto(
        String id,
        String name,
        String address,
        String website,
        String telegramChannel,
        Double distanceKm,
        double  lat,
        double  lon
) {}