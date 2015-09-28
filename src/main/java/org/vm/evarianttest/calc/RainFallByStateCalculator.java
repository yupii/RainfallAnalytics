package org.vm.evarianttest.calc;

import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.census.USACounty;
import org.vm.evarianttest.entity.census.USAState;
import org.vm.evarianttest.entity.weather.WBAN;
import org.vm.evarianttest.entity.weather.WBANRainfallRecord;
import org.vm.evarianttest.filters.RainFallExcludeFileHeaderFilter;
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
 * This Calculator implements the logic to compute the Rainfall amount for all States.
 *
 * Formula to compute = Aaggregated Railfall for every State
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
public class RainFallByStateCalculator implements Calculator {
    private Logger log = Logger.getLogger(this.getClass().getName());

    private Map<String, Map<Key, Entity>> maps;
    private URI absFilePath;
    private Map<Key, Entity> wbanMap;
    private Map<Key, BigDecimal> result = Collections.synchronizedMap(new TreeMap<>());

    /**
     * Constructor
     *
     * @param absFilePath - URI for rainfall dataset
     * @param maps - Maps of Context data
     */
    public RainFallByStateCalculator(URI absFilePath, Map<String, Map<Key, Entity>> maps) {
        this.absFilePath = absFilePath;
        this.maps = maps;
        this.wbanMap = maps.get(Constants.WBAN_MASTER_MAP);
    }

    public URI getAbsFilePath() {
        return absFilePath;
    }

    @Override
    public Map<Key, BigDecimal> calculate() {
        Path path = Paths.get(getAbsFilePath());

        Util.validateInput(path);
        log.fine("Input file: " + getAbsFilePath().toASCIIString());

        try(Stream<String> lines = Files.lines(path)
                .onClose(() -> log.fine("Rainfall DataFile closed"))){
            lines.parallel()
                    //Filter the header record
                    .filter(new RainFallExcludeFileHeaderFilter())
                    .parallel()
                    .map(new Function<String, WBANRainfallRecord>() {
                        @Override
                        public WBANRainfallRecord apply(String s) {
                            return WBANRainfallRecord.build(s);
                        }
                    })
                    .parallel()
                    .forEach(new Consumer<WBANRainfallRecord>() {
                        @Override
                        public void accept(WBANRainfallRecord rec) {
                            Key wbanKey = new Key(rec.getWbanId());
                            Optional<WBAN> wban = Optional.ofNullable((WBAN) wbanMap.get(wbanKey));
                            wban.ifPresent(new Consumer<WBAN>() {
                                @Override
                                public void accept(WBAN wban) {
                                    Optional<USACounty> county = Optional.ofNullable(wban.getCounty());
                                    county.ifPresent(new Consumer<USACounty>() {
                                        @Override
                                        public void accept(USACounty usaCounty) {
                                            USAState state = wban.getCounty().getState();
                                            if (!result.containsKey(state.key())) {
                                                result.put(state.key(), new BigDecimal(rec.getPrecipitation()));
                                            } else {
                                                BigDecimal value = result.get(state.key()).add(new BigDecimal(rec.getPrecipitation()));
                                                result.put(state.key(), value);
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    })
            ;
        }catch (Exception e){
            log.log(Level.SEVERE, "Error occurred while processing RainfallByStateCalculator" + getAbsFilePath().toASCIIString(), e);
            throw new RuntimeException("Error occurred while processing RainfallByStateCalculator" + getAbsFilePath().toASCIIString(), e);
        }
        return result;
    }

    @Override
    public Object calculateSingleValue() {
        return null;
    }

    @Override
    public StringBuilder getPrettyResult() {
        StringBuilder sbr = new StringBuilder();
        sbr.append("============================================================================================").append("\n");
        sbr.append(String.format("%30s", "State Name") + " " + String.format("%50s", "Rainfall (inches)")).append("\n");
        sbr.append("============================================================================================").append("\n");
        result.forEach(new BiConsumer<Key, BigDecimal>() {
            @Override
            public void accept(Key key, BigDecimal aDouble) {
                NumberFormat formatter = new DecimalFormat("#0.00");
                sbr.append(Util.padRight(USAState.findByAbbreviation(String.valueOf(key.getKey())).getName(), 50) + Util.padLeft(formatter.format(aDouble.abs()), 40)).append("\n");
            }
        });
        sbr.append("============================================================================================").append("\n");
        return sbr;
    }
}
