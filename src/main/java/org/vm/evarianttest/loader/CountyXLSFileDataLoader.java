package org.vm.evarianttest.loader;

import org.apache.poi.ss.usermodel.*;
import org.vm.evarianttest.entity.Entity;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.census.USAStatAreaPopulationRecord;
import org.vm.evarianttest.entity.census.USACounty;
import org.vm.evarianttest.entity.census.USAState;
import org.vm.evarianttest.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements a Data Loader for Census - County - SA List1.xls file that is of format as given in http://www.census.gov/population/metro/data/def.html.
 * This helps in improving the linking between WBANs and Statistical Areas, by tieing them using County.
 *
 * @author vivekm
 * @since 1.0
 */
public class CountyXLSFileDataLoader implements DataLoader {
    private Logger log = Logger.getLogger(this.getClass().getName());

    /**
     * Common County suffix terms that we will be removing. This is done for match quality improvement and standardization.
     */
    private enum COMMON_COUNTY_TYPES{
        COUNTY("COUNTY"),
        MUNICIPIO("MUNICIPIO"),
        BOROUGH("BOROUGH"),
        MUNICIPALITY("MUNICIPALITY");

        String name;
        COMMON_COUNTY_TYPES(String name){
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    private URI countyMSALinkFile;
    private Map<Key, Entity> statAreaMap;
    private int counter;
    private Map<String, Map<Key, Entity>> maps;

    public static final int CENSUS_COUNTY_FILE_STARTING_ROW = 3;
    public static final int CENSUS_COUNTY_FILE_STAT_AREA_COL = 3;
    public static final int CENSUS_COUNTY_FILE_COUNTY_NAME_COL = 7;
    public static final int CENSUS_COUNTY_FILE_STATE_NAME_COL = 8;

    /**
     * Constructor
     *
     * @param countyMSALinkFile - URI for List2.xls
     * @param maps - Maps of Context data
     */
    public CountyXLSFileDataLoader(URI countyMSALinkFile, Map<String, Map<Key, Entity>> maps) {
        this.countyMSALinkFile = countyMSALinkFile;
        this.maps = maps;
        this.statAreaMap = maps.get(Constants.STAT_AREA_MAP_NAME);
    }

    @Override
    public void load() throws DataLoaderException {

        try(InputStream in = new FileInputStream(new File(countyMSALinkFile))) {
            Workbook wb = WorkbookFactory.create(in);
            Sheet firstSheet = wb.getSheetAt(0);

            Iterator<Row> rowIter = firstSheet.rowIterator();
            rowReader:
            while(rowIter.hasNext()){

                Row row = rowIter.next();
                if(row.getRowNum() < CENSUS_COUNTY_FILE_STARTING_ROW)
                    continue rowReader;

                Optional<Cell> cellValue = Optional.ofNullable(row.getCell(CENSUS_COUNTY_FILE_STAT_AREA_COL));
                cellValue.ifPresent(new Consumer<Cell>() {
                    @Override
                    public void accept(Cell cell) {
                        if(cell.getCellType() == Cell.CELL_TYPE_STRING){
                            String statAreaName = cell != null? Util.cleanSAName(cell.getStringCellValue()).toUpperCase(): "";
                            USAStatAreaPopulationRecord rec = (USAStatAreaPopulationRecord) statAreaMap.get(new Key(statAreaName));

                            if(rec != null){
                                //Strip common county terms for match quality
                                String countyName = removeCommonCountySuffix(Util.cleanString(row.getCell(CENSUS_COUNTY_FILE_COUNTY_NAME_COL).getStringCellValue()));
                                String stateName = Util.cleanString(row.getCell(CENSUS_COUNTY_FILE_STATE_NAME_COL).getStringCellValue());
                                USAState state = USAState.findByName(stateName);
                                if(rec.getArea().getStates().values().contains(state)) {
                                    counter++;
                                    log.log(Level.FINE, " Rec ---> " + countyName + " - " + stateName + " added.");
                                    USACounty county = new USACounty(countyName, state);
                                    county.addPartOfArea(rec.getArea());
                                    rec.getArea().addCounty(county);
                                }
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.log(Level.SEVERE, "Error occurred while loading County-SA XLS File", e);
            throw new DataLoaderException("Error occurred while loading County-SA XLS File", e);
        }
        System.out.println("Total Number of Counties updated : " + counter);
    }

    @Override
    public Map<String, Map<Key, Entity>> getDataMaps() throws DataLoaderException {
        return maps;
    }

    /**
     * This method removes the county suffixes.
     *
     * @param input - County name like Travis County
     * @return - County name like Travis.
     */
    private String removeCommonCountySuffix(String input) {
        for (COMMON_COUNTY_TYPES type : COMMON_COUNTY_TYPES.values()) {
            input = input.replaceAll(type.getName(),"");
        }
        return input.trim();
    }
}