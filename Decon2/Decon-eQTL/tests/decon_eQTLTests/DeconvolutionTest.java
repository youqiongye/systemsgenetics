package decon_eQTLTests;

import static org.junit.Assert.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import decon_eQTL.CommandLineOptions;
import decon_eQTL.Deconvolution;
import decon_eQTL.DeconvolutionResult;

public class DeconvolutionTest {
	String outputDir = "tests/resources/deconvolutionTestResults/";
	String counts;
	String genotypes;
	String geneSnpList;
	String expression;
	CommandLineOptions commandLineOptions;
	
	@Before
	public void init() {
		commandLineOptions = new CommandLineOptions();
		File countsFile = new File("tests/resources/cellcount_files/cellcounts.txt");
		File genotypesFile = new File("tests/resources/genotype_files/genotype_dosages.txt");
		File geneSnpListFile = new File("tests/resources/gene_snp_list_files/gene_snp_list.txt");
		File expressionFile = new File("tests/resources/expression_files/expression_levels.txt");
		counts = countsFile.getAbsolutePath();
		genotypes = genotypesFile.getAbsolutePath();
		geneSnpList = geneSnpListFile.getAbsolutePath();
		expression = expressionFile.getAbsolutePath();
	}
	
	@After
	public void tearDown() throws Exception {
		//deleteDir(new File(outputDir));
	}	
	
	/*
	 *  This is more like an integration test because it runs the whole program!
	 */
	/*@Test
	public void mainTest() throws Exception {
		String[] args = {"-o",outputDir+"deconvolutionTestResultsMain/","-c",counts,"-e",
						 expTable, "-g", dsgTable, "-sn", geneSnpList};
		Main.main(args);

		LineIterator deconResults = FileUtils.lineIterator(new File(outputDir+"deconvolutionTestResultsMain/deconvolutionResults.csv"), "UTF-8");
		LineIterator deconExpected = FileUtils.lineIterator(new File("tests/resources/expected/deconExpected.txt"), "UTF-8");
		//test if header is same
		assertEquals("File header the same",deconExpected.next(),deconResults.next());
		while (deconResults.hasNext() && deconExpected.hasNext()){
			ArrayList<String> deconResultsStringVector = new ArrayList<String>(Arrays.asList(deconResults.next().split("\t")));
			ArrayList<String> deconExpectedStringVector = new ArrayList<String>(Arrays.asList(deconExpected.next().split("\t")));
			assertEquals("Deconresult same as expected", deconExpectedStringVector, deconResultsStringVector);
			assertEquals("QTL name the same", deconExpectedStringVector.remove(0), deconResultsStringVector.remove(0));
		}
	}*/

	/*
	
	/*
	 * Give error when expression and genotype file have different names
	 */
	@Test
	public void readInputDataTest() throws Exception {
		File cellCountsSmall = new File("tests/resources/cellcount_files/cellcounts_small.txt");
		String[] args = {"-o",outputDir+"deconvolutionTestResults","-c",counts,cellCountsSmall.getAbsolutePath(),
						 "-e",expression, "-g", genotypes, 
						 "-sn", geneSnpList};
		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);

		deconvolution.readInputData();
	}
	
	/*
	
	/*
	 * Give error when expression and genotype file have different names
	 */
	@Test
	public void readInputDataWrongNamesTest() throws Exception {

		File expTableWrongNames = new File("tests/resources/expression_files/expression_levels_wrong_names.txt");
		String[] args = {"-o",outputDir+"deconvolutionTestResults","-c",counts,"-e",
						 expTableWrongNames.getAbsolutePath(), "-g", genotypes, 
						 "-sn", geneSnpList};
		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);
		try {
			deconvolution.readInputData();
			fail( "My method didn't throw when I expected it to" );
		} catch (RuntimeException expectedException) {
			assertThat(expectedException.getMessage(), CoreMatchers.containsString("Samplenames not the same in expression and genotype file, or not in the same order"));
		}
	}
	
	@Test
	public void runDeconPerGeneSnpPairTestRunTest() throws Exception {
		File geneSnpListFile = new File("tests/resources/gene_snp_list_files/gene_snp_list_long.txt");
		String[] args = {"-o",outputDir+"deconvolutionTestResultsTestRun","-c",counts,
						 "-e",expression, "-g",genotypes, "-sn", geneSnpListFile.getAbsolutePath(),
						 "-t"};

		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);
		deconvolution.readInputData();
		List<DeconvolutionResult> deconvolutionResults = deconvolution.runDeconPerGeneSnpPair();
		deconvolution.writeDeconvolutionResults(deconvolutionResults);
		Path path = Paths.get(outputDir+"deconvolutionTestResultsTestRun/deconvolutionResults.csv");
		long lineCount = Files.lines(path).count();
		assertEquals("100 example lines written", lineCount, 101);
	}

	@Test
	public void runDeconPerGeneSnpPairNotExistingGenotypeTest() throws Exception {
		File geneSnpList = new File("tests/resources/gene_snp_list_files/gene_snp_list_non_existing_genotype.txt");
		String[] args = {"-o",outputDir+"deconvolutionTestResults","-c",counts,
						 "-e",expression, "-g", genotypes, 
						 "-sn", geneSnpList.getAbsolutePath()};
		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);
		deconvolution.readInputData();
		try {
			deconvolution.runDeconPerGeneSnpPair();
			fail( "My method didn't throw when I expected it to" );
		} catch (RuntimeException expectedException) {
			assertEquals(expectedException.getMessage(),
						"Error: Genotype genotype_NOT_EXISTING included in gene/snp combinations to test, but not available in the expression file!");
		}
	}

	@Test
	public void runDeconPerGeneSnpPairNotExistingGeneTest() throws Exception {
		File geneSnpList = new File("tests/resources/gene_snp_list_files/gene_snp_list_non_existing_gene.txt");
		String[] args = {"-o",outputDir+"deconvolutionTestResults","-c",counts,
						 "-e",expression, "-g", genotypes, 
						 "-sn", geneSnpList.getAbsolutePath()};
		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);
		deconvolution.readInputData();
		try {
			deconvolution.runDeconPerGeneSnpPair();
			fail( "My method didn't throw when I expected it to" );
		} catch (RuntimeException expectedException) {
			assertThat(expectedException.getMessage(), CoreMatchers.containsString("included in gene/snp combinations to test, but not available in the expression file"));
		}
	}
	
	@Test
	public void runDeconPerGeneSnpPairGenotypeNotInGenotypeFileTest() throws Exception {
		File geneSnpList = new File("tests/resources/gene_snp_list_files/gene_snp_list_non_existing_genotype.txt");
		String[] args = {"-o",outputDir+"deconvolutionTestResults","-c",counts,
						 "-e",expression, "-g", genotypes,
						 "-sn", geneSnpList.getAbsolutePath()};
		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);
		deconvolution.readInputData();
		try {
			deconvolution.runDeconPerGeneSnpPair();
			fail( "My method didn't throw when I expected it to" );
		} catch (RuntimeException expectedException) {
			assertEquals(expectedException.getMessage(), 
						"Error: Genotype genotype_NOT_EXISTING included in gene/snp combinations to test, but not available in the expression file!");
		}
	}
		
	@Test
	public void runDeconPerGeneSnpPairGeneNotInExpressionFileTest() throws Exception {
		File geneSnpList = new File("tests/resources/gene_snp_list_files/gene_snp_list_non_existing_gene.txt");
		String[] args = {"-o",outputDir+"deconvolutionTestResults","-c",counts,
						 "-e",expression, "-g", genotypes,
						 "-sn", geneSnpList.getAbsolutePath()};
		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);
		deconvolution.readInputData();
		try {
			deconvolution.runDeconPerGeneSnpPair();
			fail( "My method didn't throw when I expected it to" );
		} catch (RuntimeException expectedException) {
			assertThat(expectedException.getMessage(), CoreMatchers.containsString("included in gene/snp combinations to test, but not available in the expression file"));
		}
	}

	@Test
	public void writeDeconvolutionResultWithSpearmanCorrelationTest() throws Exception {
		String[] args = {"-o",outputDir+"deconvolutionSpearmanResults","-c",counts,
						 "-e",expression, "-g", genotypes,
						 "-sn", geneSnpList, "-w"};
		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);
		deconvolution.readInputData();
		List<DeconvolutionResult> deconvolutionResults = deconvolution.runDeconPerGeneSnpPair();
		deconvolution.writeDeconvolutionResults(deconvolutionResults);

		LineIterator deconResults = FileUtils.lineIterator(new File(outputDir+"deconvolutionSpearmanResults/deconvolutionResults.csv"), "UTF-8");
		LineIterator deconExpected = FileUtils.lineIterator(new File("tests/resources/expected_results/deconSpearmanExpected.txt"), "UTF-8");
		//test if header is same
		assertEquals("File header the same",deconExpected.next(),deconResults.next());
		while (deconResults.hasNext() && deconExpected.hasNext()){
			ArrayList<String> deconResultsStringVector = new ArrayList<String>(Arrays.asList(deconResults.next().split("\t")));
			ArrayList<String> deconExpectedStringVector = new ArrayList<String>(Arrays.asList(deconExpected.next().split("\t")));
			assertEquals("Deconresult same as expected", deconExpectedStringVector, deconResultsStringVector);
			assertEquals("QTL name the same", deconExpectedStringVector.remove(0), deconResultsStringVector.remove(0));
		}
	}
	
	@Test
	public void writeDeconvolutionResultPredictedExpressionTest() throws Exception {
		String[] args = {"-o",outputDir+"deconvolutionPredictedExpression","-c",counts,
						 "-e",expression, "-g", genotypes,
						 "-sn", geneSnpList, "-oe"};
		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);
		deconvolution.readInputData();
		List<DeconvolutionResult> deconvolutionResults = deconvolution.runDeconPerGeneSnpPair();
		deconvolution.writeDeconvolutionResults(deconvolutionResults);

		LineIterator predictedResults = FileUtils.lineIterator(new File(outputDir+"deconvolutionPredictedExpression/predictedExpressionLevels.txt"), "UTF-8");
		LineIterator predictedExpected = FileUtils.lineIterator(new File("tests/resources/expected_results/expectedPredictedExpressionLevels.txt"), "UTF-8");
		//test if header is same
		assertEquals("File header the same",predictedExpected.next(),predictedResults.next());
		while (predictedResults.hasNext() && predictedExpected.hasNext()){
			ArrayList<String> deconResultsStringVector = new ArrayList<String>(Arrays.asList(predictedResults.next().split("\t")));
			ArrayList<String> deconExpectedStringVector = new ArrayList<String>(Arrays.asList(predictedExpected.next().split("\t")));
			assertEquals("Predicted expression same as expected", deconExpectedStringVector, deconResultsStringVector);
			assertEquals("QTL name the same", deconExpectedStringVector.remove(0), deconResultsStringVector.remove(0));
		}
	}
	
	@Test
	public void deconvolutionResultRoundDosageTest() throws Exception {
		String[] args = {"-o",outputDir+"deconvolutionRoundedDosage","-c",counts,
						 "-e",expression, "-g", genotypes,
						 "-sn", geneSnpList, "-r"};
		commandLineOptions.parseCommandLine(args);
		Deconvolution deconvolution = new Deconvolution(commandLineOptions);
		deconvolution.readInputData();
		List<DeconvolutionResult> deconvolutionResults = deconvolution.runDeconPerGeneSnpPair();
		deconvolution.writeDeconvolutionResults(deconvolutionResults);

		LineIterator deconResults = FileUtils.lineIterator(new File(outputDir+"deconvolutionRoundedDosage/deconvolutionResults.csv"), "UTF-8");
		LineIterator deconExpected = FileUtils.lineIterator(new File("tests/resources/expected_results/deconRoundDosageExpected.txt"), "UTF-8");
		//test if header is same
		assertEquals("File header the same",deconExpected.next(),deconResults.next());
		while (deconResults.hasNext() && deconExpected.hasNext()){
			ArrayList<String> deconResultsStringVector = new ArrayList<String>(Arrays.asList(deconResults.next().split("\t")));
			ArrayList<String> deconExpectedStringVector = new ArrayList<String>(Arrays.asList(deconExpected.next().split("\t")));
			assertEquals("Deconresult same as expected", deconExpectedStringVector, deconResultsStringVector);
			assertEquals("QTL name the same", deconExpectedStringVector.remove(0), deconResultsStringVector.remove(0));
		}
	}
	
	void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}
}