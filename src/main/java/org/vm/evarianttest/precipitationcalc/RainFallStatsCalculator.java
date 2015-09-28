package org.vm.evarianttest.precipitationcalc;

import org.vm.evarianttest.calc.Calculator;
import org.vm.evarianttest.calc.PopulationWetnessCalculator;
import org.vm.evarianttest.calc.RainFallByStateCalculator;
import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.census.USAStatisticalAreaType;
import org.vm.evarianttest.loader.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This is the entry to the Rainfall Analytics system.
 *
 * @author vivekm
 * @since 1.0
 */
public class RainFallStatsCalculator {

    private Map<String, Map<Key, Entity>> result = null;
    private URI censusFile = null;
    private URI countyMSALinkFile = null;
    private URI wbanMasterFile = null;
    private URI stations201505File = null;

    private List<USAStatisticalAreaType> typeToLoad;

    public RainFallStatsCalculator(URI censusFile, URI countyMSALinkFile, URI wbanMasterFile, URI stations201505File, List<USAStatisticalAreaType> typeToLoad){
        this.censusFile = censusFile;
        this.countyMSALinkFile = countyMSALinkFile;
        this.wbanMasterFile = wbanMasterFile;
        this.stations201505File = stations201505File;
        this.typeToLoad = typeToLoad;
    }

    /**
     * This method loads the context data
     *
     * @throws DataLoaderException
     */
    public void load() throws DataLoaderException {
        result = loadFile(new CensusPopXLSFileDataLoader(censusFile, typeToLoad));
        result = loadFile(new CountyXLSFileDataLoader(countyMSALinkFile, result));
        result = loadFile(new WBANpsvFileDataLoader(wbanMasterFile, result));
        result = loadFile(new StationsFile2015DataLoader(stations201505File, result));
    }

    private Map<String, Map<Key, Entity>> loadFile(DataLoader loader) throws DataLoaderException {
        loader.load();
        return loader.getDataMaps();
    }

    /**
     * This method calculates the PopulationWetnessByMSA statistic.
     *
     * @param file - Rainfall Dataset
     * @param outputFilePath - Output path to which the results will be written. Pass null if you wish to not write the output
     * @return - Computed results
     */
    public Map<Key, BigDecimal> calculatePopulationWetnessByMSA(URI file, String outputFilePath){
        return calculate(new PopulationWetnessCalculator(file, result), outputFilePath);
    }

    /**
     * This method calculates the RainfallByState statistic.
     *
     * @param file - Rainfall Dataset
     * @param outputFilePath - Output path to which the results will be written. Pass null if you wish to not write the output
     * @return - Computed results
     */
    public Map<Key, BigDecimal> calculateRainfallByState(URI file, String outputFilePath){
        return calculate(new RainFallByStateCalculator(file, result), outputFilePath);
    }

    private Map<Key, BigDecimal> calculate(Calculator calc, String outputFilePath){
        Map<Key, BigDecimal> result = calc.calculate();
        if(outputFilePath != null)
            writeResultToFile(calc, outputFilePath);
        return result;
    }

    private void writeResultToFile(Calculator calc, String path){
        try {
            Files.write(Paths.get(path), calc.getPrettyResult().toString().getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns the Context Data.
     *
     * @return - Maps of Context Data
     */
    public Map<String, Map<Key, Entity>> getResult() {
        return result;
    }

    public static void main(String[] args) throws URISyntaxException, DataLoaderException {

        URI censusFile = Paths.get("./src/main/resources/CPH-T-5.xls").toAbsolutePath().toUri();
        URI countyMSALinkFile = Paths.get("./src/main/resources/MSA-County-Census-List1.xls").toUri();
        URI wbanMasterFile = Paths.get("./src/main/resources/wbanmasterlist.psv").toUri();
        URI stations201505File = Paths.get("./src/main/resources/201505station.txt").toUri();
        URI rainfall201505DataFile = Paths.get("./src/main/resources/201505precip.txt").toUri();

        List<USAStatisticalAreaType> typeToLoad = new ArrayList<>();
        typeToLoad.add(USAStatisticalAreaType.METROPOLITAN_STATISTICAL_AREA);

        Instant startLoad = Instant.now();
        RainFallStatsCalculator rfsCalc = new RainFallStatsCalculator(censusFile, countyMSALinkFile, wbanMasterFile, stations201505File, typeToLoad);
        rfsCalc.load();
        Instant endLoad = Instant.now();

        Instant startFirstCalc = Instant.now();
        Map<Key, BigDecimal> resultOne = rfsCalc.calculatePopulationWetnessByMSA(rainfall201505DataFile, "./src/main/out/PopulationWetnessByMSAResult.txt");
        Instant endFirstCalc = Instant.now();

        Instant startSecondCalc = Instant.now();
        Map<Key, BigDecimal> resultTwo = rfsCalc.calculateRainfallByState(rainfall201505DataFile, "./src/main/out/RainfallByStateResult.txt");
        Instant endSecondCalc = Instant.now();

        System.out.println("Context Data Load Time :" + (endLoad.toEpochMilli() - startLoad.toEpochMilli()) + " ms");
        System.out.println("PopulationWetnessByMSA Calculation Time :" + (endFirstCalc.toEpochMilli() - startFirstCalc.toEpochMilli()) + " ms");
        System.out.println("RainfallByState Calculation Time :" + (endSecondCalc.toEpochMilli() - startSecondCalc.toEpochMilli()) + " ms");
    }
}