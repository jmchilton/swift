package edu.mayo.mprc.swift.configuration.server.properties.validator;

import edu.mayo.mprc.swift.configuration.client.validation.local.IntegerValidator;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class IntegerValidatorTest {

	@Test
	public void validateValidIntegerTest() {
		final IntegerValidator integerValidator = new IntegerValidator();

		Assert.assertNull(integerValidator.validate("3"), "Validation of integer value 3 shoutld not had failed.");
	}

	@Test
	public void validateInvalidIntegerTest() {
		final IntegerValidator integerValidator = new IntegerValidator();

		Assert.assertNotNull(integerValidator.validate("3.45"), "Validation of integer value 3.45 should had failed.");
	}

	@Test
	public void validateValidIntegerWithLimitsTest() {
		final IntegerValidator integerValidator = new IntegerValidator(4, 6);

		Assert.assertNull(integerValidator.validate("6"), "Validation of integer value 6 should not had failed.");
	}

	@Test
	public void validateInvalidIntegerWithLimitsTest() {
		final IntegerValidator integerValidator = new IntegerValidator(7, 9);

		Assert.assertNotNull(integerValidator.validate("6"), "Validation of integer value 6 should had failed.");
	}
}
