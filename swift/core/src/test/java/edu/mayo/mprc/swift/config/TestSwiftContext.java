package edu.mayo.mprc.swift.config;

import edu.mayo.mprc.msmseval.MSMSEvalParamFile;
import edu.mayo.mprc.swift.WebUi;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public final class TestSwiftContext {

	@Test
	public void shouldParseParamFiles() {
		List<MSMSEvalParamFile> paramFiles = WebUi.parseSpectrumQaParamFiles("hello,/mnt/raid1/test.txt,test of spectrum qa.,C:\\test2.txt");
		Assert.assertEquals(paramFiles.get(0).getDescription(), "hello");
		Assert.assertEquals(paramFiles.get(0).getPath(), "/mnt/raid1/test.txt");
		Assert.assertEquals(paramFiles.get(1).getDescription(), "test of spectrum qa.");
		Assert.assertEquals(paramFiles.get(1).getPath(), "C:\\test2.txt");
	}
}
