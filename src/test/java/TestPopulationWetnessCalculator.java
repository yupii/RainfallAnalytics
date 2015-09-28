import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vm.evarianttest.entity.Key;
import org.vm.evarianttest.entity.census.USAStatAreaPopulationRecord;
import org.vm.evarianttest.entity.census.USAState;
import org.vm.evarianttest.entity.census.USAStatisticalAreaType;
import org.vm.evarianttest.loader.Constants;
import org.vm.evarianttest.loader.DataLoaderException;
import org.vm.evarianttest.precipitationcalc.RainFallStatsCalculator;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Vivek M on 9/27/2015.
 */
public class TestPopulationWetnessCalculator {
    private static URI testData = Paths.get("./src/test/resources/test_good_wban_bad_wban_one_each.txt").toUri();
    private static URI censusFile = Paths.get("./src/main/resources/CPH-T-5.xls").toAbsolutePath().toUri();
    private static URI countyMSALinkFile = Paths.get("./src/main/resources/MSA-County-Census-List1.xls").toUri();
    private static URI wbanMasterFile = Paths.get("./src/main/resources/wbanmasterlist.psv").toUri();
    private static URI stations201505File = Paths.get("./src/main/resources/201505station.txt").toUri();

    private static RainFallStatsCalculator calc;

    @BeforeClass
    public static void setup() throws DataLoaderException {
        List<USAStatisticalAreaType> typeToLoad = new ArrayList<>();
        typeToLoad.add(USAStatisticalAreaType.METROPOLITAN_STATISTICAL_AREA);

        calc = new RainFallStatsCalculator(censusFile, countyMSALinkFile, wbanMasterFile, stations201505File, typeToLoad);
        calc.load();
    }

    @Test
    public void testAustinAreaRainfallWithGoodAndBadWBANs(){
        Map<Key, BigDecimal> result = this.calc.calculatePopulationWetnessByMSA(testData, "./src/test/out/testResult1.txt");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.keySet().size() == 1);
        Key firstKey = null;
        for(Key key : result.keySet()){
            firstKey = key;
            break;
        }
        //Projected Austin-RR population in 2015 = 2,036,627 using Excel calculation
        USAStatAreaPopulationRecord rec = (USAStatAreaPopulationRecord)calc.getResult().get(Constants.STAT_AREA_MAP_NAME).get(firstKey);
        BigDecimal expectedWetness = new BigDecimal(0.8d*2036627);
        Assert.assertTrue(result.get(firstKey).doubleValue() == expectedWetness.doubleValue());
    }

    @Test
    public void testRainfallTexasState(){
        Map<Key, BigDecimal> result = this.calc.calculateRainfallByState(testData, "./src/test/out/testResult2.txt");
        Assert.assertNotNull(result);
        Assert.assertTrue(result.keySet().size() == 1);
        Key firstKey = null;
        for(Key key : result.keySet()){
            firstKey = key;
            break;
        }
        Assert.assertEquals(USAState.TEXAS.key(), firstKey);
        Assert.assertTrue(result.get(firstKey).doubleValue() == 1.0d);
    }
}
