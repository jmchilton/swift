package edu.mayo.mprc.swift.report.test;

import edu.mayo.mprc.swift.report.JsonWriter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.Date;

public final class ReportTest {
	@Test(enabled = true, groups = {"fast", "unit"})
	public void testJavascriptEscape() {
		Assert.assertEquals("hello", JsonWriter.escapeSingleQuoteJavascript("hello"));
		Assert.assertEquals("\\\\", JsonWriter.escapeSingleQuoteJavascript("\\"));
		Assert.assertEquals("\\'", JsonWriter.escapeSingleQuoteJavascript("'"));
		Assert.assertEquals("it\\'s me", JsonWriter.escapeSingleQuoteJavascript("it's me"));
		Assert.assertEquals("1\\\\2", JsonWriter.escapeSingleQuoteJavascript("1\\2"));
	}

	@Test(enabled = true, groups = {"fast", "unit"})
	public void testDateFormatting() {
		Date now = new Date();
		Assert.assertTrue(JsonWriter.formatDateCompact(now).contains("Today"));

		Calendar c = Calendar.getInstance();
		c.setTime(now);
		c.add(Calendar.DAY_OF_WEEK, -1);
		Assert.assertTrue(JsonWriter.formatDateCompact(c.getTime()).contains("Yesterday"));
	}

	@Test
	public void testTodayDateFormatting() {
		Calendar now = Calendar.getInstance();
		now.set(2009 - 1900, 0, 29, 7, 23, 30);
		Calendar date = Calendar.getInstance();
		date.set(2009 - 1900, 0, 28, 22, 30, 15);
		Assert.assertTrue(JsonWriter.formatDateCompact(date, now).contains("Yesterday"));
	}

}