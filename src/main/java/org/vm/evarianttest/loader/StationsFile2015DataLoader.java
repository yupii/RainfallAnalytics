package org.vm.evarianttest.loader;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.census.USAStatAreaPopulationRecord;
import org.vm.evarianttest.entity.census.USAStatisticalArea;
import org.vm.evarianttest.entity.weather.WBAN;
import org.vm.evarianttest.util.Util;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * This class implements a Data Loader for 2015Stations.txt file which contains the list of all WBANs that had measurements for the period of 2015 May. This data links the
 * WBANs to Statistical area by using matching State, Location Name and Name.
 *
 * @author vivekm
 * @since 1.0
 */
public class StationsFile2015DataLoader implements DataLoader{
    private Logger log = Logger.getLogger(this.getClass().getName());

    // Filter column references
    public static final int STN_2015_FILE_WBAN_ID_COL_IDX = 0;
    public static final int STN_2015_FILE_CITY_NAME_COL_IDX = 6;
    public static final int STN_2015_FILE_STATE_COL_IDX = 7;

    private AtomicInteger recRead = new AtomicInteger(0);
    private AtomicInteger wbansFound = new AtomicInteger(0);

    private URI stations2015File;
    private Map<Key, Entity> statAreaMap;
    private Map<Key, Entity> wbanMap;
    private Map<String, Map<Key, Entity>> maps;

    /**
     * Constructor
     *
     * @param stations2015File - URI for Stations file
     * @param maps - Maps of Context Data
     */
    public StationsFile2015DataLoader(URI stations2015File, Map<String, Map<Key, Entity>> maps){
        this.stations2015File = stations2015File;
        this.maps = maps;
        this.statAreaMap = maps.get(Constants.STAT_AREA_MAP_NAME);
        this.wbanMap = maps.get(Constants.WBAN_MASTER_MAP);
    }

    @Override
    public void load() throws DataLoaderException {
        Path path = Paths.get(stations2015File);
        Util.validateInput(path);
        log.fine("Input file: " + stations2015File.toASCIIString());

        try(Stream<String> lines = Files.lines(path)
                .onClose(() -> log.fine("Stations 2015 File closed"))){
            lines.parallel()
                    .forEach(new Consumer<String>() {
                        @Override
                        public void accept(String line) {
                            recRead.getAndIncrement();

                            String[] inputArr = Util.splitPSVLine(line);
                            Key wbanProspect = makeWBANKey(inputArr);
                            if (wbanMap.containsKey(wbanProspect)) {
                                WBAN wbanFound = (WBAN) wbanMap.get(wbanProspect);
                                wbanFound.setStationLocatedAt(Util.cleanString(inputArr[STN_2015_FILE_CITY_NAME_COL_IDX]) + "," + Util.cleanString(inputArr[STN_2015_FILE_STATE_COL_IDX]));
                                wbanFound.getCounty().addWeatherStation(wbanFound);

                                statAreaMap.values().forEach(new Consumer<Entity>() {
                                    @Override
                                    public void accept(Entity entity) {
                                        boolean linkWBAN = false;
                                        USAStatAreaPopulationRecord rec = (USAStatAreaPopulationRecord) entity;
                                        USAStatisticalArea area = rec.getArea();
                                        if (area.getWeatherStations().containsKey(wbanFound)) {
                                            if (wbanFound.getArea() == null) {
                                                linkWBAN = true;
                                            }
                                        }

                                        if (!wbanFound.getStationLocatedAt().isEmpty()) {
                                            String locationName = Util.cleanString(inputArr[STN_2015_FILE_CITY_NAME_COL_IDX]);
                                            String stateAbbr = Util.cleanString(inputArr[STN_2015_FILE_STATE_COL_IDX]);
                                            String cleanedSAName = String.valueOf(area.key().getKey());

                                            if (cleanedSAName.contains(locationName) && cleanedSAName.contains(stateAbbr)) {
                                                linkWBAN = true;
                                                area.addWeatherStation(wbanFound);
                                            }
                                        }
                                        if (linkWBAN) {
                                            wbansFound.getAndIncrement();
                                            wbanFound.setArea(rec.getArea());
                                        }
                                    }
                                });
                            }
                        }
                    });
        } catch (Exception ex){
            ex.printStackTrace();
            log.log(Level.SEVERE, "Error occurred while processing " + stations2015File.toASCIIString(), ex);
            throw new DataLoaderException("Error occurred while processing " + stations2015File.toASCIIString(), ex);
        }
        System.out.println("Total Number of WBAN records read " + recRead + " === linked : " + wbansFound);
    }

    private Key makeWBANKey(String[] inputArr) {
        return new Key(Util.cleanString(inputArr[STN_2015_FILE_WBAN_ID_COL_IDX]));
    }

    @Override
    public Map<String, Map<Key, Entity>> getDataMaps() throws DataLoaderException {
        return this.maps;
    }
}