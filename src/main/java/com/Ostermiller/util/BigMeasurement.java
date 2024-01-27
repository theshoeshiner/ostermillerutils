package com.Ostermiller.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import org.apache.commons.numbers.core.NativeOperators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * Figure out how to correctly represent precision and sigfigs when adding/multiplying zero values.
 * e.g. "0e3" = "0000" but BigDecimal will trim leading zeros and output it as "0". Maybe we need to force
 * exponential notation for outputting valid values and show it as "0e3"?
 *
 */
public class BigMeasurement extends Number implements Comparable<BigMeasurement>, NativeOperators<BigMeasurement>, Serializable {
	
	private static final long serialVersionUID = 2685548793501031015L;

	private static final Logger LOGGER = LoggerFactory.getLogger(BigMeasurement.class);

	private Integer significantFigures;
	
	//The tens place of the right most significant digit
	private Integer precision;
	
	private BigDecimal exactValue;
	
	private BigDecimal validValue;
	
	public BigMeasurement(String string) {
		SignificantFigures significantFiguresInfo = new SignificantFigures(string);
		this.significantFigures = significantFiguresInfo.getDigits().length();
		LOGGER.debug("BigMeasurement(): {}",string);
		LOGGER.trace("digits: {}",significantFiguresInfo.getDigits());
		LOGGER.trace("string: {}",significantFiguresInfo.toString());
		LOGGER.trace("mantissa: {}",significantFiguresInfo.getMantissa());
		LOGGER.trace("sigfigs: {}",significantFiguresInfo.getNumberSignificantFigures());
		
		//precision = 0 - LSD
		this.precision = getPrecision(significantFiguresInfo);
		this.exactValue = new BigDecimal(significantFiguresInfo.toString());
		this.validValue = exactValue;
		
		LOGGER.debug("exactValue: {}",exactValue.toPlainString());
	}
	
	
	
	private BigMeasurement(Integer significantFigures, Integer precision, BigDecimal exactValue,
			BigDecimal validValue) {
		super();
		this.significantFigures = significantFigures;
		this.precision = precision;
		this.exactValue = exactValue;
		this.validValue = validValue;
	}



	public int getSignificantFigures() {
		return significantFigures;
	}
	
	

	public BigDecimal getExactValue() {
		return exactValue;
	}

	public BigDecimal getValidValue() {
		return validValue;
	}

	public void setSignificantFigures(Integer significantFigures) {
		LOGGER.info("setSignificantFigures: {}",significantFigures);
		if(!Objects.equals(significantFigures, this.significantFigures)) {
			LOGGER.info("change sigfigs from {} to {}",this.significantFigures,significantFigures);
			this.significantFigures = significantFigures;
			if(this.significantFigures != null) {
				LOGGER.info("create newSigFig from: {}",exactValue);
				SignificantFigures newSigFig = new SignificantFigures(exactValue.toPlainString());
				newSigFig.setNumberSignificantFigures(significantFigures);
				LOGGER.info("after setting sigfigs: {}",newSigFig.toString());
				this.validValue = new BigDecimal(newSigFig.toString());
				int newPrecision = getPrecision(newSigFig);
				LOGGER.info("Change precision from {} to {}",this.precision,newPrecision);
				this.precision = newPrecision;
			}
			else {
				//undefined sigfigs means infinite
				this.validValue = exactValue;
			}
		}
		
	}
	
	
	
	public int getPrecision() {
		return precision;
	}

	/**
	 * Sets the precision. This can inherently change the number of significant figures.
	 * @param precision
	 */
	public void setPrecision(Integer precision) {
		LOGGER.debug("setPrecision: {}",precision);
		if(!Objects.equals(precision, this.precision)) {
		
			LOGGER.debug("change precision from {} to {}",this.precision,precision);
			this.precision = precision;
			
			if(this.precision != null) {
			
				validValue = validValue.setScale(precision, RoundingMode.HALF_EVEN);
				
				LOGGER.debug("new valid value after setting precision {} = {}",precision,validValue.toPlainString());

				SignificantFigures sf = new SignificantFigures(validValue.toPlainString());
				
				LOGGER.debug("change sigfig from {} to {}",this.significantFigures,sf.getNumberSignificantFigures());
				
				this.significantFigures = sf.getNumberSignificantFigures();
				
				//LOGGER.debug("new valid value after setting sigifg {} = {}",this.significantFigures,validValue.toPlainString());
			}
			else {
				//TODO is this correct when removing precision?
				this.validValue = exactValue;
			}
		}
		//LOGGER.debug("{}",newValue.toPlainString());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(validValue.toPlainString());
		return builder.toString();
	}
	
	public String toStringExact() {
		StringBuilder builder = new StringBuilder();
		builder.append(exactValue.toPlainString());
		return builder.toString();
	}

	@Override
	public BigMeasurement add(BigMeasurement other) {
		LOGGER.info("add: {} + {}",this.exactValue,other.exactValue);
		LOGGER.info("prec: {} , {}",this.precision,other.precision);
		LOGGER.info("sig: {} , {}",this.significantFigures,other.significantFigures);
		
		int newPrecision = this.precision > other.precision ? other.precision : this.precision;
		BigDecimal newValue = this.exactValue.add(other.exactValue);
		LOGGER.debug("{} + {} = {}",this.exactValue.toPlainString(),other.exactValue.toPlainString(),newValue.toPlainString());
		LOGGER.debug("newPrecision: {}",newPrecision);

		BigMeasurement newMes = new BigMeasurement(newValue.toPlainString());
		newMes.setPrecision(newPrecision);
		LOGGER.debug("{} rounded = {}",newMes.toStringExact(),newMes.toString());
		return newMes;
	}
	
	public BigMeasurement add(Number other) {
		if(other instanceof BigMeasurement) return this.add((BigMeasurement)other);
		LOGGER.debug("add: {}",other);
		BigDecimal newValue = this.exactValue.add(new BigDecimal(other.toString()));
		BigMeasurement newMes = new BigMeasurement(newValue.toPlainString());
		//newMes.setSignificantFigures(significantFigures);
		newMes.setPrecision(precision);
		return newMes;
	}
	
	public BigMeasurement multiply(Number other) {
		LOGGER.debug("multiply: {}",other);
		if(other instanceof BigMeasurement) return this.multiply((BigMeasurement)other);
		BigDecimal newValue = this.exactValue.multiply(new BigDecimal(other.toString()));
		BigMeasurement newMes = new BigMeasurement(newValue.toPlainString());
		newMes.setSignificantFigures(significantFigures);
		//newMes.setPrecision(precision);
		return newMes;
	}

	@Override
	public BigMeasurement zero() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigMeasurement negate() {
		BigMeasurement neg = new BigMeasurement(significantFigures,precision,exactValue.negate(),validValue.negate());
		return neg;
	}

	@Override
	public BigMeasurement multiply(BigMeasurement other) {
		LOGGER.debug("multiply {} x {}",this.exactValue,other.exactValue);
		Integer resSig = null;
		if(this.significantFigures == null) {
			if(other.significantFigures != null) resSig = other.significantFigures;
		}
		else if(other.significantFigures == null) resSig = this.significantFigures;
		else {
			if(other.significantFigures < this.significantFigures) resSig = other.significantFigures;
			else resSig = this.significantFigures;
		}
		
		//BigInteger result = new BigInteger(this.digits.toString()).multiply(new BigInteger(other.digits.toString()));
		
		BigDecimal newValue = this.exactValue.multiply(other.exactValue);
		//LOGGER.debug("newValue: {}",newValue);
		LOGGER.debug("create new big mes from: {}",newValue.toPlainString());
		
		
		//the exponent is one less than mantissa start point
		//LOGGER.debug("mans: {} , {}",this.mantissa,a.mantissa);
		//
		//LOGGER.debug("lens: {} , {}",this.digits.length(),a.digits.length());
		//LOGGER.debug("exps: {} , {}",this.mantissa-(this.digits.length()-1),a.mantissa-(a.digits.length()-1));
		//Integer exp = this.exponent-(this.digits.length()-1) + other.exponent-(other.digits.length()-1);
		//Integer exp = this.getMSD() + a.getMSD();
		//LOGGER.debug("exp: {}",exp);
		//LOGGER.debug("sigfig: {}",resSig);
		//BigMeasurement resMes = new BigMeasurement(result,exp);
		BigMeasurement resMes = new BigMeasurement(newValue.toPlainString());
		LOGGER.debug("bm: {}",resMes);
		LOGGER.debug("set result sig fig: {}",resSig);
		resMes.setSignificantFigures(resSig);
		LOGGER.debug("bm: {}",resMes);
		//resMes.setNumberSignificantFigures(resSig);
		
		return resMes;
	}

	@Override
	public BigMeasurement one() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigMeasurement reciprocal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigMeasurement subtract(BigMeasurement other) {
		LOGGER.info("add: {} + {}",this.exactValue,other.exactValue);
		LOGGER.info("prec: {} , {}",this.precision,other.precision);
		LOGGER.info("sig: {} , {}",this.significantFigures,other.significantFigures);
		
		int newPrecision = this.precision > other.precision ? other.precision : this.precision;
		BigDecimal newValue = this.exactValue.subtract(other.exactValue);
		LOGGER.debug("{} + {} = {}",this.exactValue.toPlainString(),other.exactValue.toPlainString(),newValue.toPlainString());
		LOGGER.debug("newPrecision: {}",newPrecision);

		BigMeasurement newMes = new BigMeasurement(newValue.toPlainString());
		newMes.setPrecision(newPrecision);
		LOGGER.debug("{} rounded = {}",newMes.toStringExact(),newMes.toString());
		
		
		//newValue = newValue.setScale(newPrecision, RoundingMode.HALF_EVEN);
		
		
		return newMes;
	}

	@Override
	public BigMeasurement divide(BigMeasurement a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigMeasurement multiply(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BigMeasurement pow(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(BigMeasurement o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int intValue() {
		LOGGER.info("intValue");
		return validValue.intValue();
	}

	@Override
	public long longValue() {
		LOGGER.info("longValue");
		return validValue.longValue();
	}

	@Override
	public float floatValue() {
		LOGGER.info("floatValue");
		return validValue.floatValue();
	}

	@Override
	public double doubleValue() {
		LOGGER.info("doubleValue");
		return validValue.doubleValue();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exactValue == null) ? 0 : exactValue.hashCode());
		result = prime * result + ((precision == null) ? 0 : precision.hashCode());
		result = prime * result + ((significantFigures == null) ? 0 : significantFigures.hashCode());
		result = prime * result + ((validValue == null) ? 0 : validValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BigMeasurement other = (BigMeasurement) obj;
		if (exactValue == null) {
			if (other.exactValue != null)
				return false;
		} else if (!exactValue.equals(other.exactValue))
			return false;
		if (precision == null) {
			if (other.precision != null)
				return false;
		} else if (!precision.equals(other.precision))
			return false;
		if (significantFigures == null) {
			if (other.significantFigures != null)
				return false;
		} else if (!significantFigures.equals(other.significantFigures))
			return false;
		if (validValue == null) {
			if (other.validValue != null)
				return false;
		} else if (!validValue.equals(other.validValue))
			return false;
		return true;
	}
	
	private static int getPrecision(SignificantFigures significantFiguresInfo) {
		return significantFiguresInfo.getDigits().length() - significantFiguresInfo.getMantissa() - 1;
	}
	
	
}
