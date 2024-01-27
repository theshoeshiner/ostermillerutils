package com.Ostermiller.util;

import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class BigMeasurementTest extends TestCase {

	private static final Logger LOGGER = LoggerFactory.getLogger(BigMeasurementTest.class);

	/*public void testTrailingZeros() {
		BigMeasurement test;
	
		test = new BigMeasurement("145000.e-5");
		LOGGER.info("{}", test);
	
		test = new BigMeasurement("145000.0e-5");
		LOGGER.info("{}", test);
	
		test = new BigMeasurement("145.00e-5");
		LOGGER.info("{}", test);
	
	}*/

	/*
	 * Array of pre-calculated inputs and outputs
	 * value, value sigfig, value precision, value*prior, v*p expected precision, value+prior, v+p expected sigfig
	 */
	Object[][] testValues = new Object[][] {
			{ "0034.5693107", 	9, 7 },
			{ "+0000.0000", 	8, 4, 	"0.0000000" ,	7,	"34.5693",	6},
			{ ".2232000", 		7, 7 ,	"0.000000",		6,	"0.2232",	4},
			{ "-2.48320", 		6, 5 ,	"-0.554250",	6,	"-2.26000",	6},
			{ "145.00e-5", 		5, 7 ,	"-0.0036006",	7,	"-2.48175",	6},
			{ "-465.2", 		4, 1 ,	"-0.6745",		4,	"-465.2",	4},
			{ "000", 			3, 0 ,	"0.00",			2,	"-465",		3},
			{ "+11000", 		2, -3 ,	"0.0",			1,	"11000",	2},
			{ "20", 			1, -1 ,	"200000",		-5,	"11000",	2},
			{ "1.2e3", 			2, -2 ,	"20000",		-4,	"1200",		2},
			{ "0.0e3", 			2, -2 ,	"0.0" ,			1,	"1200",		2},
			{ "0.00", 			3, 2 ,	"0.0" ,			1,	"0",		1},
			{ "0.00", 			3, 2 ,	"0.00",			2,	"0.00",		3},
			{ "000", 			3, 0,	"0.00",			2,	"0",		1},
			{ "+00.00", 		4, 2, 	"0.00",			2,	"0",		1},
			{ "-0.0000", 		5, 4, 	"0.000",		3,	"0.00",		3},
			{ "-0.0", 			2, 1,	"0.0",			1,	"0.0",		2},
	};
	
	/*Object[][] zeroTestValues = new Object[][] {
		{ "0.00", 		3, 2},
		{ "000", 		3, 0,	"0.00",	"0"},
		{ "+00.00", 	4, 2, 	"0.00",	"0"},
		{ "-0.0000", 	5, 4, 	"0.000","0.00"},
		{ "-0.0", 		2, 1,	"0.0",	"0.0"},
	};*/

	/**
	 * Tests that number of significant figures is correct for test values
	 */
	public void testParseSignificantFigures() {
		for (int i = 0; i < testValues.length; i++) {
			BigMeasurement val = new BigMeasurement((String) testValues[i][0]);
			assertEquals(val+" sigfigs",testValues[i][1], val.getSignificantFigures());
		}
	}

	/**
	 * Tests that precision is correct for test values
	 */
	public void testParsePrecision() {
		for (int i = 0; i < testValues.length; i++) {
			BigMeasurement val = new BigMeasurement((String) testValues[i][0]);
			LOGGER.info("precision of {} was {}", val, val.getPrecision());
			assertEquals(val+" precision",testValues[i][2], val.getPrecision());
		}
	}

	/**
	 * Tests that precision is adjusted when adding testing values
	 */
	public void testDecreasePrecision() {
		for (int i = 1; i < testValues.length; i++) {
			
			String s1 = (String) testValues[i - 1][0];
			String s2 = (String) testValues[i][0];
			String resValid = (String) testValues[i][5];
			int expectedSigFig = (int) testValues[i][6];
			
			LOGGER.info("resValid: {}",resValid);
			LOGGER.info("testDecreasePrecision {} + {} = {}",new Object[] {s1,s2,resValid});
			
			BigMeasurement val1 = new BigMeasurement(s1);
			BigMeasurement val2 = new BigMeasurement(s2);
			
			int precision = Math.min((int)testValues[i-1][2], (int)testValues[i][2]);
			
			BigMeasurement res1 = val1.add(val2);
			
			LOGGER.info("{} + {} = ...",s1,s2);
			LOGGER.info("res1 exact: {}",res1.getValidValue().toPlainString());
			
			
			
			assertEquals(val1+" + "+val2+" = ",resValid, res1.getValidValue().toPlainString());
			assertEquals(val1+" + "+val2+" precision",precision, res1.getPrecision());
			assertEquals(val1+" + "+val2+" sigfig",expectedSigFig, res1.getSignificantFigures());
		}
	}
	
	/**
	 * Tests that significant figures is adjusted when multiplying test values
	 */
	public void testDecreaseSignificantFigures() {
		for (int i = 1; i < testValues.length; i++) {
			
			String s1 = (String) testValues[i - 1][0];
			String s2 = (String) testValues[i][0];
			String expectedValid = (String) testValues[i][3];
			int expectedPrecision = (int)testValues[i][4];
			
			BigMeasurement val1 = new BigMeasurement(s1);
			BigMeasurement val2 = new BigMeasurement(s2);
			
			LOGGER.info("{} x {} = ...",s1,s2);
			
			int sigfig = Math.min((int)testValues[i-1][1], (int)testValues[i][1]);
			
			BigMeasurement res1 = val1.multiply(val2);
			//BigMeasurement res2 = val2.multiply(val1);
			
			//assertEquals(res1, res2);
			
			LOGGER.info("{} x {} = ...",s1,s2);
			LOGGER.info("exact: {}",res1.getValidValue().toPlainString());
			//LOGGER.info("res2 exact: {}",res2.getValidValue().toPlainString());
	
			assertEquals(val1+" * "+val2+" = ",expectedValid, res1.getValidValue().toPlainString());
			assertEquals(val1+" * "+val2+" sigfigs ",sigfig, res1.getSignificantFigures());
			assertEquals(val1+" * "+val2+" precision ",expectedPrecision, res1.getPrecision());
			//assertEquals(sigfig, res2.getSignificantFigures());
		}
	}
	
	/**
	 * Tests that (a+b) is the same as (b+a)
	 */
	public void testCommutativeAddition() {
		
		for (int i = 1; i < testValues.length; i++) {
			
			BigMeasurement bm1 = new BigMeasurement((String) testValues[i - 1][0]);
			BigMeasurement bm2 = new BigMeasurement((String) testValues[i][0]);
			
			BigMeasurement res1 = bm1.add(bm2);
			BigMeasurement res2 = bm2.add(bm1);
			
			assertEquals(bm1+" + "+bm2 +" = "+bm2+" + "+bm1,res1, res2);
			
		}

	}

	/**
	 * Tests that (a*b) is the same as (b*a)
	 */
	public void testCommutativeMultiplication() {
		
		BiFunction<BigMeasurement, BigMeasurement, BigMeasurement> op = (bm1,bm2)->{
			return bm1.multiply(bm2);
		};

		for (int i = 1; i < testValues.length; i++) {
			
			BigMeasurement bm1 = new BigMeasurement((String) testValues[i - 1][0]);
			BigMeasurement bm2 = new BigMeasurement((String) testValues[i][0]);
			
			BigMeasurement res1 = bm1.multiply(bm2);
			BigMeasurement res2 = bm2.multiply(bm1);
			
			assertEquals(bm1+" x "+bm2+" = "+bm2+" * "+bm1,res1, res2);
		}

	}

	/**
	 * Tests that (a-b) is the same as 0-(b-a)
	 */
	public void testSubtractiveInverse() {

		for (int i = 1; i < testValues.length; i++) {
			BigMeasurement val1 = new BigMeasurement((String) testValues[i - 1][0]);
			BigMeasurement val2 = new BigMeasurement((String) testValues[i][0]);

			BigMeasurement sub1 = val1.subtract(val2);
			BigMeasurement sub2 = val2.subtract(val1);
			BigMeasurement neg1 = sub1.negate();
			BigMeasurement neg2 = sub2.negate();

			assertEquals(sub1, neg2);
			assertEquals(sub2, neg1);

		}

	}

	/*public void testZeros() {
	
		LOGGER.info("testZeros");
	
		for (int i = 0; i < zeroTestValues.length; i++) {
			String s = (String) zeroTestValues[i][0];
			LOGGER.info("string: {}", s);
			BigMeasurement val = new BigMeasurement(s);
			LOGGER.info("BigMeasurement: {}", val);
			LOGGER.info("BigMeasurement sigifg: {}", val.getSignificantFigures());
			LOGGER.info("BigMeasurement precision: {}", val.getPrecision());
	
		}
	
	}*/
	
	
	/*
	public void testAdd() {
		
		BigMeasurement val1;
		BigMeasurement val2;
		BigMeasurement val3;
		BigMeasurement val4;
		
		
		val1 = new BigMeasurement(".2232000");
	//		LOGGER.info("sf1: {} man: {}",sf1,sf1.getMantissa());
		//LOGGER.info("sf1: {}",sf1);
		val2 = new BigMeasurement("465.2");
		
		val3 = new BigMeasurement("21200.");
		
		val4 = new BigMeasurement("20");
		
		BigMeasurement sum = addAll(val1,val2,val3,val4);
		
		//BigMeasurement sum = addAll(val1,val2,val3,val4);
	//		LOGGER.info("Sum: {}",sum);
		
		LOGGER.info("val1: {} lsd: {}",val1,val1.getLSD());
		LOGGER.info("val2: {} lsd: {}",val2,val2.getLSD());
		LOGGER.info("val3: {} lsd: {}",val3,val3.getLSD());
		
		LOGGER.info("val1: {} lsd: {}",val1,val1.getPrecision());
		LOGGER.info("val2: {} lsd: {}",val2,val2.getPrecision());
		LOGGER.info("val3: {} lsd: {}",val3,val3.getPrecision());
		LOGGER.info("val4: {} lsd: {}",val4,val4.getPrecision());
		
		
		BigMeasurement val2p4 = val2.add(val4);
		
		BigMeasurement val1p2 = val1.add(val2);
		
		BigMeasurement val2p3 = val3.add(val2);
		
	}
	
	public void testMultiply() {
		
		
		BigMeasurement val1;
		BigMeasurement val2;
		BigMeasurement result;
		
		
	
	
		val1 = new BigMeasurement(".0032000");
		val2 = new BigMeasurement("471.25");
		//LOGGER.info("sf1: {} man: {}",val1,val1.getExponent());
		//LOGGER.info("sf2: {} man: {}",val2,val2.getExponent());
		
		
		result = val1.multiply(val2);
		LOGGER.info("{} x {} = {}",val1,val2,result);
		
		
		val1 = new BigMeasurement("1.1801");
		val2 = result;
		result = val1.multiply(val2);
		LOGGER.info("{} x {} = {}",val1,val2,result);
		
		
		val1 = result;
		val2 = new BigMeasurement("14.15");
		result = val1.multiply(val2);
		LOGGER.info("{} x {} = {}",val1,val2,result);
		
		val1 = result;
		val2 = new BigMeasurement("32.1");
		result = val1.multiply(val2);
		LOGGER.info("{} x {} = {}",val1,val2,result);
		
	}
	
	public BigMeasurement addAll(BigMeasurement... mes) {
		
		StringBuilder msg = new StringBuilder();
		BigMeasurement sum = mes[0];
		msg.append(sum.toStringExact());
		for(int i=1;i<mes.length;i++) {
			sum = sum.add(mes[i]);
			msg.append(" + "+mes[i].toStringExact());
		}
		msg.append(" = "+sum.toString()+" ("+sum.toStringExact()+")");
		LOGGER.info(msg.toString());
	
		return sum;
	}*/
}
