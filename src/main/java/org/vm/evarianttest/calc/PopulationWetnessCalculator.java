package org.vm.evarianttest.calc;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.census.USAStatAreaPopulationRecord;
import org.vm.evarianttest.entity.census.USAStatisticalArea;
import org.vm.evarianttest.entity.weather.WBANRainfallRecord;
import org.vm.evarianttest.filters.RainFallExcludeFileHeaderFilter;
import org.vm.evarianttest.filters.RainFallFileMay2015PeriodIncludeFilter;
import org.vm.evarianttest.filters.RainFallFileMidNightTo7AMHourWindowExcludeFilter;
import org.vm.evarianttest.filters.RainfallFileWBANIncludeFilter;
import org.vm.evarianttest.loader.Constants;
import org.vm.evarianttest.util.Util;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * This Calculator implements the logic to compute the Population Wetness for a given set of Statistical Areas.
 * Formula to compute Population Wetness = population * (aggregated Railfall for that Statistical Area)
 *
 * Processor process:
 *  Open Stream
 *  Apply all Filters
 *  On filtered records -> Apply in parallel -> Reducers
 *  Return result
 *
 *  @author vivekm
 *  @since 1.0
 */
public class PopulationWetnessCalculator implements Calculator{
    private Logger log = Logger.getLogger(this.getClass().getName());

    private Map<String, Map<Key, Entity>> maps;
    private URI absFilePath;
    private Map<Key, Entity> statAreaMap;
    private Map<Key, BigDecimal> result = Collections.synchronizedMap(new TreeMap<>());
    private Map<Key, List<Key>> wbanSAmap = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Constructor
     *
     * @param absFilePath - URI for the Rainfall dataset
     * @param maps - Map of context data
     */
    public PopulationWetnessCalculator(URI absFilePath, Map<String, Map<Key, Entity>> maps){
        this.absFilePath = absFilePath;
        this.maps = maps;
        this.statAreaMap = maps.get(Constants.STAT_AREA_MAP_NAME);
    }

    public URI getAbsFilePath() {
        return absFilePath;
    }

    @Override
    public Map<Key, BigDecimal> calculate() {
        Path path = Paths.get(getAbsFilePath());

        Util.validateInput(path);
        log.fine("Input file: " + getAbsFilePath().toASCIIString());
        buildWbanMSAmap();

        try(Stream<String> lines = Files.lines(path)
                .onClose(() -> log.fine("Rainfall DataFile closed"))){
            lines.parallel()
                    // Filter the Header record
                    .filter(new RainFallExcludeFileHeaderFilter())
                    .parallel()
                    // Filter WBANs that are attached to the Statistical Areas
                    .filter(new RainfallFileWBANIncludeFilter(wbanSAmap))
                    .map(new Function<String, WBANRainfallRecord>() {
                        @Override
                        public WBANRainfallRecord apply(String s) {
                            return WBANRainfallRecord.build(s);
                        }
                    })
                    .parallel()
                    // Filter the period of May 2015
                    .filter(o -> {
                        return new RainFallFileMay2015PeriodIncludeFilter().test(o);
                    })
                    .parallel()
                    // Skip hours between midNight (0) and 7 AM
                    .filter(o -> {
                        return new RainFallFileMidNightTo7AMHourWindowExcludeFilter().test(o);
                    })
                    .parallel()
                    .forEach(new Consumer<WBANRainfallRecord>() {
                        @Override
                        public void accept(WBANRainfallRecord rec) {
                            Key wbanKey = new Key(rec.getWbanId());
                            Optional<List<Key>> areaKeys = Optional.ofNullable(wbanSAmap.get(wbanKey));
                            areaKeys.ifPresent(new Consumer<List<Key>>() {
                                @Override
                                public void accept(List<Key> keys) {
                                    keys.stream().parallel().forEach(new Consumer<Key>() {
                                        @Override
                                        public void accept(Key key) {
                                            // Aggregate the rainfall data per SA
                                            if (!result.containsKey(key)) {
                                                result.put(key, new BigDecimal(rec.getPrecipitation()));
                                            } else {
                                                BigDecimal value = result.get(key).add(new BigDecimal(rec.getPrecipitation()));
                                                result.put(key, value);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    })
            ;
        } catch (Exception ex){
            log.log(Level.SEVERE, "Error occurred while processing " + getAbsFilePath().toASCIIString(), ex);
            throw new RuntimeException("Error occurred while processing " + getAbsFilePath().toASCIIString(), ex);
        }
        computeWetness(result);
        return result;
    }

    /**
     * This method implements the formula for wetness on the aggregated rainfall data.
     *
     * @param result - Aggregated rainfall data
     */
    private void computeWetness(Map<Key, BigDecimal> result) {
        result.forEach(new BiConsumer<Key, BigDecimal>() {
            @Override
            public void accept(Key key, BigDecimal rainfall) {
                USAStatAreaPopulationRecord pop = (USAStatAreaPopulationRecord) statAreaMap.get(key);
                BigDecimal wetnessAmt = rainfall.multiply(new BigDecimal(pop.getCurrentPopulation()));
                result.put(key, wetnessAmt);
            }
        });
    }

    /**
     * This method builds a map of WBANs and associated List of Statistical Areas.
     */
    private void buildWbanMSAmap() {
        this.statAreaMap.values().forEach(new Consumer<Entity>() {
            @Override
            public void accept(Entity entity) {
                USAStatAreaPopulationRecord rec = (USAStatAreaPopulationRecord) entity;
                USAStatisticalArea area = rec.getArea();
                area.getWeatherStations().keySet().parallelStream().forEach(new Consumer<Key>() {
                    @Override
                    public void accept(Key wbanKey) {
                        List<Key> areaKeys = wbanSAmap.get(wbanKey);
                        if (areaKeys == null) {
                            areaKeys = new LinkedList<Key>();
                        }
                        areaKeys.add(area.key());
                        wbanSAmap.put(wbanKey, areaKeys);
                    }
                });
            }});
        log.log(Level.FINE, "WBAN - SA Map size " + wbanSAmap.size());
    }

    @Override
    public Object calculateSingleValue() {
        return null;
    }

    @Override
    public StringBuilder getPrettyResult() {
        StringBuilder sbr = new StringBuilder();
        sbr.append("============================================================================================").append("\n");
        sbr.append(String.format("%30s", "MSA") + " " + String.format("%50s", "WetnessValue (Person-inches)")).append("\n");
        sbr.append("============================================================================================").append("\n");
        result.forEach(new BiConsumer<Key, BigDecimal>() {
            @Override
            public void accept(Key key, BigDecimal aDouble) {
                NumberFormat formatter = new DecimalFormat("#0.00");
                sbr.append(Util.padRight(((USAStatAreaPopulationRecord) statAreaMap.get(key)).getArea().getName(), 50) + Util.padLeft(formatter.format(aDouble.abs()), 40)).append("\n");
            }
        });
        sbr.append("============================================================================================").append("\n");
        return sbr;
    }
}