package org.vm.evarianttest.entity.census;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.KeyAware;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a Typelist of all States in USA. It also supports reverse lookup operations like findByAbbreviation() & findByName().
 *
 * Credit: https://gist.github.com/webdevwilson/5271984
 */
public enum USAState implements Entity, KeyAware {

    ALABAMA("Alabama", "AL"),
    ALASKA("Alaska", "AK"),
    AMERICAN_SAMOA("American Samoa", "AS"),
    ARIZONA("Arizona", "AZ"),
    ARKANSAS("Arkansas", "AR"),
    CALIFORNIA("California", "CA"),
    COLORADO("Colorado", "CO"),
    CONNECTICUT("Connecticut", "CT"),
    DELAWARE("Delaware", "DE"),
    DISTRICT_OF_COLUMBIA("District of Columbia", "DC"),
    FEDERATED_STATES_OF_MICRONESIA("Federated USAStates of Micronesia", "FM"),
    FLORIDA("Florida", "FL"),
    GEORGIA("Georgia", "GA"),
    GUAM("Guam", "GU"),
    HAWAII("Hawaii", "HI"),
    IDAHO("Idaho", "ID"),
    ILLINOIS("Illinois", "IL"),
    INDIANA("Indiana", "IN"),
    IOWA("Iowa", "IA"),
    KANSAS("Kansas", "KS"),
    KENTUCKY("Kentucky", "KY"),
    LOUISIANA("Louisiana", "LA"),
    MAINE("Maine", "ME"),
    MARYLAND("Maryland", "MD"),
    MARSHALL_ISLANDS("Marshall Islands", "MH"),
    MASSACHUSETTS("Massachusetts", "MA"),
    MICHIGAN("Michigan", "MI"),
    MINNESOTA("Minnesota", "MN"),
    MISSISSIPPI("Mississippi", "MS"),
    MISSOURI("Missouri", "MO"),
    MONTANA("Montana", "MT"),
    NEBRASKA("Nebraska", "NE"),
    NEVADA("Nevada","NV"),
    NEW_HAMPSHIRE("New Hampshire", "NH"),
    NEW_JERSEY("New Jersey", "NJ"),
    NEW_MEXICO("New Mexico", "NM"),
    NEW_YORK("New York", "NY"),
    NORTH_CAROLINA("North Carolina", "NC"),
    NORTH_DAKOTA("North Dakota", "ND"),
    NORTHERN_MARIANA_ISLANDS("Northern Mariana Islands", "MP"),
    OHIO("Ohio", "OH"),
    OKLAHOMA("Oklahoma", "OK"),
    OREGON("Oregon", "OR"),
    PALAU("Palau", "PW"),
    PENNSYLVANIA("Pennsylvania", "PA"),
    PUERTO_RICO("Puerto Rico", "PR"),
    RHODE_ISLAND("Rhode Island", "RI"),
    SOUTH_CAROLINA("South Carolina", "SC"),
    SOUTH_DAKOTA("South Dakota", "SD"),
    TENNESSEE("Tennessee", "TN"),
    TEXAS("Texas", "TX"),
    UTAH("Utah", "UT"),
    VERMONT("Vermont", "VT"),
    VIRGIN_ISLANDS("Virgin Islands", "VI"),
    VIRGINIA("Virginia", "VA"),
    WASHINGTON("Washington", "WA"),
    WEST_VIRGINIA("West Virginia", "WV"),
    WISCONSIN("Wisconsin", "WI"),
    WYOMING("Wyoming", "WY"),
    UNKNOWN("Unknown", "");

    private Key key;
    /**
     * The state's name.
     */
    private String name;

    /**
     * The state's abbreviation.
     */
    private String abbreviation;

    /**
     * The set of states addressed by abbreviations.
     */
    private static final Map<String, USAState> STATES_BY_ABBR = new HashMap<>();

    /* static initializer */
    static {
        for (USAState state : values()) {
            STATES_BY_ABBR.put(state.getAbbreviation(), state);
        }
    }

    /**
     * Constructs a new state.
     *
     * @param name the state's name.
     * @param abbreviation the state's abbreviation.
     */
    USAState(String name, String abbreviation) {
        this.name = name;
        this.abbreviation = abbreviation;
        this.key = new Key(this.abbreviation);
    }

    public String getName(){
        return name;
    }
    /**
     * Returns the state's abbreviation.
     *
     * @return the state's abbreviation.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    /**
     * Gets the enum constant with the specified abbreviation.
     *
     * @param abbr the state's abbreviation.
     * @return the enum constant with the specified abbreviation.
     */
    public static USAState findByAbbreviation(final String abbr) {
        final USAState state = STATES_BY_ABBR.get(abbr);

        if (state != null) {
            return state;
        } else {
            return UNKNOWN;
        }
    }

    public static USAState findByName(final String name) {
        final String enumName = name.toUpperCase().replaceAll(" ", "_");
        try {
            return valueOf(enumName);
        } catch (final IllegalArgumentException e) {
            return USAState.UNKNOWN;
        }
    }

    public Key key(){
        return key;
    }
    @Override
    public String toString() {
        return name;
    }
}