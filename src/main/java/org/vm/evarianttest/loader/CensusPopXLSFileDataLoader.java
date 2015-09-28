package org.vm.evarianttest.loader;

import org.apache.poi.ss.usermodel.*;
import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.census.USAStatAreaPopulationRecord;
import org.vm.evarianttest.entity.census.USAState;
import org.vm.evarianttest.entity.census.USAStatisticalArea;
import org.vm.evarianttest.entity.census.USAStatisticalAreaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.Year;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a Data Loader for Census file that is of format as given in http://www.census.gov/population/www/cen2010/cph-t/cph-t-5.html.
 *
 * It accepts an Excel file, extracts the Statistical Areas as defined by the input typesToLoad and computes the projected 2015 population based on the 10 year rate, using a formula
 * projected 2015 population = 2010 population + (ten year percentage change/10) * 5 * 2010 population.
 *
 * @author vivekm
 * @since 1.0
 */
public class CensusPopXLSFileDataLoader implements DataLoader{

    public static final int CENSUS_USA_STARTING_ROW = 7;
    public static final int CENSUS_TITLE_COL = 0;
    public static final int CENSUS_2010_POP_COL = 2;
    public static final int CENSUS_POP_CHANGE_PERCENT_COL = 4;

    public static final Year CENSUS_POP_PREVIOUS_BASE_YEAR = Year.parse("2000");
    public static final Year CENSUS_POP_CURRENT_BASE_YEAR = Year.parse("2010");
    public static final Year CENSUS_POP_CURRENT_YEAR = Year.parse("2015");

    private Logger log = Logger.getLogger(this.getClass().getName());

    private Map<Key, Entity> statAreaMap = null;
    private URI censusFile = null;
    private List<USAStatisticalAreaType> typesToLoad = new ArrayList<>();
    private Map<String, Map<Key, Entity>> result = new LinkedHashMap<>();

    public CensusPopXLSFileDataLoader(URI censusFile, List<USAStatisticalAreaType> typesToLoad){
        this.censusFile = censusFile;
        this.typesToLoad.addAll(typesToLoad);
    }

    @Override
    public void load() throws DataLoaderException{
        statAreaMap = new LinkedHashMap<>();
        USAStatisticalAreaType currentType = null;
        boolean isPuertoRicoArea = false;
        int counter = 0;

        try(InputStream in = new FileInputStream(new File(censusFile))) {
            Workbook wb = WorkbookFactory.create(in);
            Sheet firstSheet = wb.getSheetAt(0);

            Iterator<Row> rowIter = firstSheet.rowIterator();

            rowReader:
            while(rowIter.hasNext()){
                boolean isMetaData = false;
                boolean isSkipRow = true;

                Row row = rowIter.next();
                if(row.getRowNum() < CENSUS_USA_STARTING_ROW)
                    continue rowReader;

                Cell cell = row.getCell(CENSUS_TITLE_COL);
                String title = cell.toString().trim();
                if(!title.isEmpty()){
                    if(USAState.PUERTO_RICO.getName().equalsIgnoreCase(title)){
                        isPuertoRicoArea = true;
                        isMetaData = true;
                        continue rowReader;
                    }

                    if(USAStatisticalAreaType.isValid(title)){
                        currentType = USAStatisticalAreaType.findByName(title);
                        isMetaData = true;
                        continue rowReader;
                    }

                    //Apply the input filter
                    if(typesToLoad.contains(currentType)) {
                        isSkipRow = false;
                    }
                }

                if(!isMetaData && !isSkipRow){
                    USAStatAreaPopulationRecord rec = buildPopulationRec(++counter, title, isPuertoRicoArea, currentType, row.getCell(CENSUS_2010_POP_COL).getNumericCellValue(), row.getCell(CENSUS_POP_CHANGE_PERCENT_COL).getNumericCellValue());
                    if(log.isLoggable(Level.FINE)) log.fine("Loaded - " + rec.toString());
                    statAreaMap.put(rec.getArea().key(), rec);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Error occurred while loading Census XLS File", e);
        }
        result.put(Constants.STAT_AREA_MAP_NAME, statAreaMap);
        System.out.println("Total MSA records loaded : " + statAreaMap.size());
    }

    @Override
    public Map<String, Map<Key, Entity>> getDataMaps() throws DataLoaderException {
        return this.result;
    }

    public static USAStatAreaPopulationRecord buildPopulationRec(final int id, final String title, final boolean isPuertoRicoArea, USAStatisticalAreaType type, final double base2010Population, final double tenYearRatePercent){
        USAStatAreaPopulationRecord rec = new USAStatAreaPopulationRecord();

        rec.setArea(new USAStatisticalArea(id, title, type, isPuertoRicoArea));

        rec.setBasePopulation(Math.round(base2010Population));
        rec.setBaseYear(CENSUS_POP_CURRENT_BASE_YEAR);

        rec.setAverageYearlyPopChangeRate(tenYearRatePercent / ((CENSUS_POP_CURRENT_BASE_YEAR.getValue() - CENSUS_POP_PREVIOUS_BASE_YEAR.getValue())*100));

        long currentPopulation = projectPopulation(rec.getBasePopulation(), CENSUS_POP_CURRENT_BASE_YEAR, rec.getAverageYearlyPopChangeRate(), CENSUS_POP_CURRENT_YEAR);
        rec.setCurrentPopulation(currentPopulation);
        rec.setCurrentPopYear(CENSUS_POP_CURRENT_YEAR);

        return rec;
    }

    public static long projectPopulation(long inputPop, Year inputYear, double averageYearlyPopChangeRate, Year yearToProject) {
        int numberOfYears = Math.abs(yearToProject.getValue() - inputYear.getValue());
        double change = inputPop * averageYearlyPopChangeRate * numberOfYears;
        return inputPop + Math.round(change);
    }
}