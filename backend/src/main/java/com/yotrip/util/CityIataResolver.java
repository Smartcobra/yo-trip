package com.yotrip.util;

import java.util.Map;

public final class CityIataResolver {

    private static final Map<String, String> CITY_TO_IATA = Map.ofEntries(
            Map.entry("delhi", "DEL"),
            Map.entry("new delhi", "DEL"),
            Map.entry("mumbai", "BOM"),
            Map.entry("bombay", "BOM"),
            Map.entry("bangalore", "BLR"),
            Map.entry("bengaluru", "BLR"),
            Map.entry("chennai", "MAA"),
            Map.entry("madras", "MAA"),
            Map.entry("kolkata", "CCU"),
            Map.entry("calcutta", "CCU"),
            Map.entry("hyderabad", "HYD"),
            Map.entry("pune", "PNQ"),
            Map.entry("goa", "GOI"),
            Map.entry("ahmedabad", "AMD"),
            Map.entry("jaipur", "JAI"),
            Map.entry("lucknow", "LKO"),
            Map.entry("kochi", "COK"),
            Map.entry("cochin", "COK"),
            Map.entry("chandigarh", "IXC"),
            Map.entry("srinagar", "SXR"),
            Map.entry("guwahati", "GAU"),
            Map.entry("patna", "PAT"),
            Map.entry("bhubaneswar", "BBI"),
            Map.entry("indore", "IDR"),
            Map.entry("nagpur", "NAG"),
            Map.entry("varanasi", "VNS"),
            Map.entry("amritsar", "ATQ"),
            Map.entry("thiruvananthapuram", "TRV"),
            Map.entry("trivandrum", "TRV")
    );

    private static final Map<String, AirportInfo> IATA_INFO = Map.ofEntries(
            Map.entry("DEL", new AirportInfo("Delhi", "Indira Gandhi International Airport")),
            Map.entry("BOM", new AirportInfo("Mumbai", "Chhatrapati Shivaji Maharaj International Airport")),
            Map.entry("BLR", new AirportInfo("Bangalore", "Kempegowda International Airport")),
            Map.entry("MAA", new AirportInfo("Chennai", "Chennai International Airport")),
            Map.entry("CCU", new AirportInfo("Kolkata", "Netaji Subhas Chandra Bose International Airport")),
            Map.entry("HYD", new AirportInfo("Hyderabad", "Rajiv Gandhi International Airport")),
            Map.entry("PNQ", new AirportInfo("Pune", "Pune Airport")),
            Map.entry("GOI", new AirportInfo("Goa", "Goa International Airport")),
            Map.entry("AMD", new AirportInfo("Ahmedabad", "Sardar Vallabhbhai Patel International Airport")),
            Map.entry("JAI", new AirportInfo("Jaipur", "Jaipur International Airport")),
            Map.entry("LKO", new AirportInfo("Lucknow", "Chaudhary Charan Singh International Airport")),
            Map.entry("COK", new AirportInfo("Kochi", "Cochin International Airport")),
            Map.entry("IXC", new AirportInfo("Chandigarh", "Chandigarh Airport")),
            Map.entry("SXR", new AirportInfo("Srinagar", "Sheikh ul-Alam International Airport")),
            Map.entry("GAU", new AirportInfo("Guwahati", "Lokpriya Gopinath Bordoloi International Airport")),
            Map.entry("PAT", new AirportInfo("Patna", "Jay Prakash Narayan Airport")),
            Map.entry("BBI", new AirportInfo("Bhubaneswar", "Biju Patnaik International Airport")),
            Map.entry("IDR", new AirportInfo("Indore", "Devi Ahilya Bai Holkar Airport")),
            Map.entry("NAG", new AirportInfo("Nagpur", "Dr. Babasaheb Ambedkar International Airport")),
            Map.entry("VNS", new AirportInfo("Varanasi", "Lal Bahadur Shastri International Airport")),
            Map.entry("ATQ", new AirportInfo("Amritsar", "Sri Guru Ram Dass Jee International Airport")),
            Map.entry("TRV", new AirportInfo("Thiruvananthapuram", "Trivandrum International Airport"))
    );

    private CityIataResolver() {}

    public static String resolveToIata(String cityOrIata) {
        if (cityOrIata == null || cityOrIata.isBlank()) {
            return "";
        }
        String trimmed = cityOrIata.trim();
        if (trimmed.length() == 3 && trimmed.equals(trimmed.toUpperCase())) {
            return trimmed.toUpperCase();
        }
        return CITY_TO_IATA.getOrDefault(trimmed.toLowerCase(), trimmed.toUpperCase());
    }

    public static AirportInfo airportInfo(String iata) {
        return IATA_INFO.getOrDefault(
                iata.toUpperCase(),
                new AirportInfo(iata, iata + " Airport")
        );
    }

    public record AirportInfo(String city, String airportName) {}
}
