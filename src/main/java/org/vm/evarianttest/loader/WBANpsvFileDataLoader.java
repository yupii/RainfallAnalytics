package org.vm.evarianttest.loader;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.census.USAStatAreaPopulationRecord;
import org.vm.evarianttest.entity.census.USACounty;
import org.vm.evarianttest.entity.census.USAState;
import org.vm.evarianttest.entity.census.USAStatisticalArea;
import org.vm.evarianttest.entity.weather.WBAN;
import org.vm.evarianttest.util.Util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a Data Loader for WBAN Master file which loads all the WBANs available in the PSV file wbanmasterlist.psv in www.ncdc.noaa.gov site.
 * WBANs are then linked to Statistical area by using matching State and County name.
 *
 * @author vivekm
 * @since 1.0
 */
public class WBANpsvFileDataLoader implements DataLoader<Key,Entity> {
    private Logger log = Logger.getLogger(this.getClass().getName());

    // File column index locations for various values
    public static final int WBAN_FILE_ID_COL_IDX = 1;
    public static final int WBAN_FILE_STATION_NAME_COL_IDX = 2;
    public static final int WBAN_FILE_STATE_COL_IDX = 3;
    public static final int WBAN_FILE_COUNTY_COL_IDX = 4;
    private AtomicInteger updatedPopulationRecsCounter = new AtomicInteger(0);

    public static final String FILE_HEADER = "WBAN_ID";

    private URI wbanMasterFile;
    private Map<Key, Entity> statAreaMap;
    private Map<Key, Entity> wbanMap = new LinkedHashMap<>();
    private Map<String, Map<Key, Entity>> maps;

    /**
     * Constructor
     *
     * @param wbanMasterFile - URI of WBAN Master file
     * @param maps - Maps of Context Data
     */
    public WBANpsvFileDataLoader(URI wbanMasterFile, Map<String, Map<Key, Entity>> maps){
        this.wbanMasterFile = wbanMasterFile;
        this.maps = maps;
        this.statAreaMap = maps.get(Constants.STAT_AREA_MAP_NAME);
    }

    @Override
    public void load() throws DataLoaderException {
        Path path = Paths.get(wbanMasterFile);
        Util.validateInput(path);

        Map<Key, List<Key>> countySAmap = getCountySAmap();

        // Took the traditional approach instead of Stream as the WBAN Master File has some special non-UTF-8 characters and Stream apis
        // don't have the ability to set the decoder. When I read as Stream, it resulted in MalformedInputException.
        // Here we are setting the encoding to UTF-8 and skipping the Malformed record (WBAN ID - 94039, PINE RIDGE). Ideally we could report the record by overriding
        // CharsetDecoder.implOnMalformedInput() - vm
        try(InputStream is = new FileInputStream(path.toFile())) {
            CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
            decoder.onMalformedInput(CodingErrorAction.IGNORE);
            InputStreamReader reader = new InputStreamReader(is, decoder);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();

            wbanReader:
            while(line != null){
                if(line.contains(FILE_HEADER)){
                    line = bufferedReader.readLine();
                    continue wbanReader;
                }

                String[] inputArr = Util.splitPSVLine(line);

                if(inputArr.length > 2 && inputArr[WBAN_FILE_ID_COL_IDX] != null && inputArr[WBAN_FILE_STATION_NAME_COL_IDX] != null){
                    // Build WBAN Object and link County.
                    WBAN wban = getWBAN(inputArr);
                    wbanMap.put(wban.key(), wban);

                    if(wban.getCounty() != null){
                        // Link the USAStatisticalArea if the County is available. This filters the WBANs that we are not concerned with. Since population record in this case is smaller
                        // looping is not an issue
                        //System.out.println("Checking WBAN : " + wban.toString() + " County ===> " + wban.getCounty().key().toString());
                        List<Key> areaKeys = countySAmap.get(wban.getCounty().key());
                        if(areaKeys != null){
                            if(log.isLoggable(Level.FINE)) log.fine("Hit found : " + wban.toString() + " County ===> " + wban.getCounty().key().toString());
                            areaKeys = countySAmap.get(wban.getCounty().key());
                            areaKeys.forEach(areaKey -> {
                                USAStatAreaPopulationRecord rec = (USAStatAreaPopulationRecord) statAreaMap.get(areaKey);
                                USAStatisticalArea area = rec.getArea();
                                area.addWeatherStation(wban);
                                wban.setArea(area);
                                updatedPopulationRecsCounter.getAndIncrement();
                            });
                        }
                    }
                }else{
                    log.log(Level.FINE, "Skipping WBAN Master file record " + line);
                }
                line = bufferedReader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "Error occurred while processing " + wbanMasterFile.toASCIIString(), e);
            throw new DataLoaderException("Error occurred while processing " + wbanMasterFile.toASCIIString(), e);
        }
        System.out.println("Total Number of WBANs added into Map : " + wbanMap.size());
        System.out.println("Total Number of MSA records linked by WBAN Counties : " + updatedPopulationRecsCounter);

        this.maps.put(Constants.WBAN_MASTER_MAP, wbanMap);
    }

    /**
     * This method builds a map of County keys and list of corresponding Statistical Area keys for lookup purpose.
     *
     * @return - County Key - List of corresponding SA keys
     */
    private Map<Key, List<Key>> getCountySAmap() {
        Map<Key, List<Key>> countySAmap = Collections.synchronizedMap(new LinkedHashMap<>());
        statAreaMap.values().forEach(new Consumer<Entity>() {
            @Override
            public void accept(Entity entity) {
                USAStatAreaPopulationRecord rec = (USAStatAreaPopulationRecord) entity;
                rec.getArea().getCounties().keySet().parallelStream().forEach(new Consumer<Key>() {
                    @Override
                    public void accept(Key countyKey) {
                        List<Key> areasKey = countySAmap.get(countyKey);
                        if(areasKey == null){
                            areasKey = new LinkedList<>();
                        }
                        areasKey.add(rec.getArea().key());
                        countySAmap.put(countyKey, areasKey);
                    }
                });
            }
        });
        return countySAmap;
    }

    @Override
    public Map<String, Map<Key, Entity>> getDataMaps() throws DataLoaderException {
        return this.maps;
    }

    /**
     * This method builds a WBAN instance.
     *
     * @param input - Input record
     * @return - WBAN instance
     */
    private WBAN getWBAN(String[] input) {
        WBAN wban = new WBAN(Util.cleanString(input[WBAN_FILE_ID_COL_IDX]), Util.cleanString(input[WBAN_FILE_STATION_NAME_COL_IDX]));
        if(input.length > 4 && input[WBAN_FILE_COUNTY_COL_IDX] != null && input[WBAN_FILE_STATE_COL_IDX] != null){
            wban.setCounty(getCounty(input));
            wban.getCounty().addWeatherStation(wban);
        }
        return wban;
    }

    /**
     * This method builds a County instance.
     *
     * @param input - Input record
     * @return - USACounty instance
     */
    private USACounty getCounty(String[] input){
        String stateAbbr = Util.cleanString(input[WBAN_FILE_STATE_COL_IDX]);
        USAState state = USAState.findByAbbreviation(stateAbbr);
        return new USACounty(Util.cleanString(input[WBAN_FILE_COUNTY_COL_IDX]), state);
    }
}