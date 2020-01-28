package com.myexchange.data;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
/**
 * Some basic tests to validate successful read and parsing of data.
 * @author suniy
 *
 */
public class StockDataAnalysisTest {
	
	@Test
	public void testFileRead() throws Exception{
	
		List<List<String>> output = StockDataAnalysis.readCSVAsStrings("samples","HistoricalQuotes.csv");
		Assert.assertNotNull(output);
		Assert.assertEquals(253, output.size());
	}

	@Test
	public void testPerformAnalysis() throws Exception{
	
		List<List<String>> sourceData = StockDataAnalysis.readCSVAsStrings("samples","HistoricalQuotes.csv");
		Assert.assertNotNull(sourceData);
		Assert.assertEquals(253, sourceData.size());
		List<List<String>> outputData = StockDataAnalysis.perfromAnalysis(sourceData); 
		Assert.assertEquals(266, outputData.size());
	}

}
