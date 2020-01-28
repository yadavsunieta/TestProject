package com.myexchange.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

/**
 * 
 * @author suniy
 *
 */
public class StockDataAnalysis {

	public static final String COMMA = ",";

	public static enum FILE_HEADERS {
		DATE, CLOSE, VOLUME, OPEN, HIGH, LOW
	};

	@Parameter(names = { "-d", "-dir" }, description = "Source data directory", required = true)
	public String sourceDir = null;

	@Parameter(names = { "-f", "-fileName" }, description = "File name", required = true)
	public String fileName = null;

	public static void main(String[] args) {
		StockDataAnalysis app = new StockDataAnalysis();
		JCommander.newBuilder().addObject(app).build().parse(args);
		List<List<String>> inputData = readCSVAsStrings(app.sourceDir, app.fileName);
		List<List<String>> outputData = perfromAnalysis(inputData);
		writeCSV(app.sourceDir, "stock historical trade Analysis.csv", outputData);
	}

	/**
	 * Reads a CSV file from the provided path and maps the data as a two
	 * dimensional list of strings.
	 * 
	 * @param dir
	 *            - path of dir where the input file is.
	 * @param fileName
	 *            - name of file
	 * @return
	 */
	public static List<List<String>> readCSVAsStrings(String dir, String fileName) {

		List<List<String>> output = new ArrayList<List<String>>();

		if (!StringUtils.endsWithIgnoreCase(dir, "/") && !StringUtils.endsWithIgnoreCase(dir, "\\")) {
			dir = dir + "/";
		}
		String line = "";
		System.out.println("Reading input file : "+dir+fileName);
	
		try {
			File file = new File(dir + fileName);
			if (!file.exists()) {
				file = new File(StockDataAnalysis.class.getClassLoader().getResource(dir + fileName).toURI());
			}
			try (BufferedReader br = new BufferedReader(new FileReader(file))) {
				while ((line = br.readLine()) != null) {
					output.add(Arrays.asList(line.split(COMMA)));
				}
			} catch (FileNotFoundException e) {
				System.out.print("No file with name [" + fileName + "] found under dir [" + dir + "]");
			} catch (IOException e) {
				System.out.print("Error occured while reading file [" + fileName + "]");
			}
		} catch (URISyntaxException uriSyntaxException) {
			System.out.print("Invalid file location [" + dir + fileName + "] ");
		}
		System.out.println(output.size()+" lines read from file : "+dir+fileName);
		return output;
	}

	/**
	 * Performs below analysis on input data and returns the calculated outputs
	 * filter record when stock closed price higher than open price filter
	 * record when stock closed price is less then open price 
	 * calculate monthly average volume traded for last 1 year
	 * 
	 * @param inputStockData
	 * @return
	 */
	public static List<List<String>> perfromAnalysis(List<List<String>> inputStockData) {
		List<List<String>> outputData = new ArrayList<>();
		outputData.add(inputStockData.get(0));
		List<String> record=null;
		BigDecimal open;
		BigDecimal close;
		Long volume;
		LocalDate date;
		Map<String,List<Long>> monthlyVolumes = new HashMap<String, List<Long>>();
		//Skip header row in input data
		for(int i=1;i<inputStockData.size();i++){
			record = inputStockData.get(i);
			//Keep if close>open or open>close
			open = new BigDecimal(StringUtils.substringAfter(record.get(FILE_HEADERS.OPEN.ordinal()).trim(), "$"));
			close = new BigDecimal(StringUtils.substringAfter(record.get(FILE_HEADERS.CLOSE.ordinal()).trim(), "$"));
			date = LocalDate.parse(record.get(FILE_HEADERS.DATE.ordinal()).trim(),DateTimeFormatter.ofPattern("MM/dd/yyyy"));
			volume = Long.parseLong(record.get(FILE_HEADERS.VOLUME.ordinal()).trim());
			//Keep where open and close are different
			if(open.compareTo(close)!=0){
				outputData.add(record);
			}
			
			//Keep track of volumes by month_year
			if(null==monthlyVolumes.get(date.getMonthValue()+"_"+date.getYear())){
				List<Long> volumByMonth = new ArrayList<>();
				volumByMonth.add(volume);
				monthlyVolumes.put(date.getMonthValue()+"_"+date.getYear(), volumByMonth);
			}else{
				monthlyVolumes.get(date.getMonthValue()+"_"+date.getYear()).add(volume);
			}
			
		}
		
		//Header for monthly volumes
		outputData.add(Arrays.asList("Month","AvgVolume"));
		List<String> avgMonthlyVolume;
		Long totalMonthlyVolume;
		for(Entry<String, List<Long>> entry : monthlyVolumes.entrySet()){
			avgMonthlyVolume = new ArrayList<>();
			avgMonthlyVolume.add(entry.getKey());
			totalMonthlyVolume=0l;
			for(Long vol: entry.getValue()){
				totalMonthlyVolume=totalMonthlyVolume+vol;
			}
			avgMonthlyVolume.add((totalMonthlyVolume/entry.getValue().size())+"");
			
			outputData.add(avgMonthlyVolume);
		}
		
		
		return outputData;
	}

	public static void writeCSV(String dir, String fileName, List<List<String>> outputData) {

		if (!StringUtils.endsWithIgnoreCase(dir, "/") && !StringUtils.endsWithIgnoreCase(dir, "\\")) {
			dir = dir + "/";
		}
		
		StringBuilder outputStringBuilder = new StringBuilder();
		for(List<String> row : outputData){
			for(String column: row){
				outputStringBuilder.append(column).append(COMMA);
			}
			outputStringBuilder.append("\n");
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(dir + fileName))) {
			bw.write(outputStringBuilder.toString());
		} catch (FileNotFoundException e) {
			System.out.print("No file with name ["+fileName+"] found under dir ["+dir+"]");
		} catch (IOException e) {
			System.out.print("Error occured while reading file ["+fileName+"]");
		}
		System.out.println(outputData.size()+" lines written to file : "+dir+fileName);
	}

}
