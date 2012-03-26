package edu.mayo.mprc.dbcurator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class TestDateFormatting {

	@Test
	public void shouldParseAndFormatDate() {
		final DateTimeFormatter formatter = DateTimeFormat.forPattern("MM/dd/yyyy");
		final DateTime date = formatter.parseDateTime("02/29/2012");
		final String formatted = formatter.print(date);
		Assert.assertEquals(formatted, "02/29/2012", "Date should match");
	}

}
