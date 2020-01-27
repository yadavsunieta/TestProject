package com.myexchange.data;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class StockDataAnalysisTest {
	
	@Test
	public void testFileRead() throws Exception{
	
		List<List<String>> output = StockDataAnalysis.readCSVAsStrings("/samples","HistoricalQuotes.csv");
		Assert.assertNotNull(output);
		Assert.assertEquals(253, output.size());
	}

}
