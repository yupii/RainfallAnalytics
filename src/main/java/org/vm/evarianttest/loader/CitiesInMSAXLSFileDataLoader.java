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
 * This is not implemented, but a potential prospect to improve the data link quality, by linking the List2.xls file to Statistical Area file. List2.xls has the list of all principal cities
 * under a given Statistical Area.
 *
 * But for now this is not being used.
 */
@Deprecated
public class CitiesInMSAXLSFileDataLoader implements DataLoader {
    private Logger log = Logger.getLogger(this.getClass().getName());

    private URI countyMSALinkFile;
    private Map<Key, Entity> statAreaMap;
    private int counter;
    private Map<String, Map<Key, Entity>> result = new LinkedHashMap<>();

    public static final int CENSUS_CITY_FILE_STARTING_ROW = 3;
    public static final int CENSUS_CITY_FILE_STAT_AREA_COL = 3;
    public static final int CENSUS_CITY_FILE_COUNTY_NAME_COL = 7;
    public static final int CENSUS_CITY_FILE_STATE_NAME_COL = 8;

    public CitiesInMSAXLSFileDataLoader(URI countyMSALinkFile, Map<String, Map<Key, Entity>> maps) {
        this.countyMSALinkFile = countyMSALinkFile;
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
                if(row.getRowNum() < CENSUS_CITY_FILE_STARTING_ROW)
                    continue rowReader;

                Optional<Cell> cellValue = Optional.ofNullable(row.getCell(CENSUS_CITY_FILE_STAT_AREA_COL));
                cellValue.ifPresent(new Consumer<Cell>() {
                    @Override
                    public void accept(Cell cell) {
                        if(cell.getCellType() == Cell.CELL_TYPE_STRING){
                            String statAreaName = cell != null? Util.cleanSAName(cell.getStringCellValue()).toUpperCase(): "";
                            USAStatAreaPopulationRecord rec = (USAStatAreaPopulationRecord) statAreaMap.get(new Key(statAreaName));

                            if(rec != null){
                                String countyName = Util.cleanString(row.getCell(CENSUS_CITY_FILE_COUNTY_NAME_COL).getStringCellValue());
                                String stateName = Util.cleanString(row.getCell(CENSUS_CITY_FILE_STATE_NAME_COL).getStringCellValue());
                                USAState state = USAState.findByName(stateName);
                                if(rec.getArea().getStates().values().contains(state)) {
                                    counter++;
                                    log.log(Level.FINE, " Rec ---> " + countyName + " - " + stateName + " added.");
                                    USACounty county = new USACounty(countyName, state);
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
        }
        result.put(Constants.STAT_AREA_MAP_NAME, statAreaMap);
        System.out.println("Total Number of Counties updated : " + counter);
    }

    @Override
    public Map<String, Map<Key, Entity>> getDataMaps() throws DataLoaderException {
        return result;
    }
}