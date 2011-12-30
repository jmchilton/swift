package edu.mayo.mprc.dbcurator.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.common.client.GWTServiceException;
import edu.mayo.mprc.database.Change;
import edu.mayo.mprc.database.Evolvable;
import edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStub;
import edu.mayo.mprc.dbcurator.client.curatorstubs.HeaderTransformStub;
import edu.mayo.mprc.dbcurator.model.Curation;
import edu.mayo.mprc.dbcurator.model.FastaSource;
import edu.mayo.mprc.dbcurator.model.HeaderTransform;
import edu.mayo.mprc.dbcurator.model.SourceDatabaseArchive;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;
import edu.mayo.mprc.utilities.FileUtilities;

public final class CommonDataRequesterImplTest {
	private static final String TEST_TRANSFORM_NAME = "id_transform";
	private static final String TEST_GROUP_STRING = "IPI:(\\w+)";
	private static final String TEST_SUB_PATTERN = "\\1";
	private static final String TEST_FASTA_SOURCE_NAME = "NCBI";
	private static final String TEST_FASTA_SOUCE_URL = "http://ncbi.gov/database/yeast";
	private static final CurationStub TEST_CURATION_STUB = new CurationStub();

	private static class InstrumentedCurationDao implements CurationDao {
		private boolean rollbackCalled = false;
		private boolean throwException = false;
		private List<HeaderTransform> headerTransformers = null;
		private List<FastaSource> fastaSources = null;
		private List<Curation> curations = null;
		
		@Override
		public void begin() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void commit() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void rollback() {
			rollbackCalled = true;			
		}

		@Override
		public Curation getCuration(int curationID) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addCuration(Curation curation) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void save(SourceDatabaseArchive archive) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteCuration(Curation curation, Change change) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public FastaSource getDataSourceByName(String urlString) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FastaSource getDataSourceByUrl(String urlString) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SourceDatabaseArchive findSourceDatabaseInExistence(String s,
				Date urlLastMod) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HeaderTransform getHeaderTransformByUrl(String url) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public HeaderTransform getHeaderTransformByName(String name) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Curation> getMatchingCurations(Curation templateCuration,
				Date earliestRunDate, Date latestRunDate) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Curation findCuration(String shortDbName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Curation getCurationByShortName(String uniqueName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Curation> getCurationsByShortname(String curationShortName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Curation> getCurationsByShortname(String shortname,
				boolean ignoreCase) {
			return curations;
		}

		@Override
		public List<FastaSource> getCommonSources() {
			return fastaSources;
		}

		@Override
		public void addHeaderTransform(HeaderTransform sprotTrans) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public List<HeaderTransform> getCommonHeaderTransforms() {
			if(throwException) {
				throw new MprcException();
			}
			return headerTransformers;
		}

		@Override
		public long countAll(Class<? extends Evolvable> clazz) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long rowCount(Class<?> clazz) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Curation addLegacyCuration(String legacyName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addFastaSource(FastaSource source) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private InstrumentedCurationDao curationDao;
	private CommonDataRequesterImpl requester;
	private HttpSession mockSession;
	private CurationHandlerI mockHandler;
	private File testSharedFile;
	
	@BeforeMethod
	public void initRequester() {
		curationDao = new InstrumentedCurationDao();
		mockSession = Mockito.mock(HttpSession.class);
		requester = new CommonDataRequesterImpl(curationDao, Suppliers.ofInstance(mockSession));
		mockHandler = Mockito.mock(CurationHandlerI.class);
		Mockito.when(mockSession.getAttribute("curationHandler")).thenReturn(mockHandler);
		testSharedFile = null;
	}
	
	@AfterMethod
	public void cleanSharedFile() {
		if(testSharedFile != null)  {
			FileUtilities.deleteNow(testSharedFile);
		}		
	}
	
	private void writeSharedFile(final String contents) throws IOException {
		testSharedFile = File.createTempFile("testCommonDataRequester", "");
		FileUtilities.writeStringToFile(testSharedFile, contents, true);
	}
	
	@Test
	public void testGetHeaderTransformersRollsBackOnException() {
		curationDao.throwException = true;
		boolean exceptionThrown = false;
		try {	
			requester.getHeaderTransformers();
		} catch(RuntimeException e) {
			exceptionThrown = true;
		}
		Assert.assertTrue(exceptionThrown);
		Assert.assertTrue(curationDao.rollbackCalled);
	}

	
	@Test
	public void testGetHeaderTransformersCreatesStubs() {
		final HeaderTransform transform = new HeaderTransform();
		transform.setName(TEST_TRANSFORM_NAME);
		transform.setGroupString(TEST_GROUP_STRING);
		transform.setSubstitutionPattern(TEST_SUB_PATTERN);
		curationDao.headerTransformers = Lists.newArrayList(transform);
		
		final List<HeaderTransformStub> transformStubs = requester.getHeaderTransformers();
		final HeaderTransformStub transformStub = Iterables.getOnlyElement(transformStubs);
		Assert.assertEquals(transformStub.description, TEST_TRANSFORM_NAME);
		Assert.assertEquals(transformStub.matchPattern, TEST_GROUP_STRING);
		Assert.assertEquals(transformStub.subPattern, TEST_SUB_PATTERN);
	}
	
	@Test
	public void testGetFTPDataSources() throws GWTServiceException {
		final FastaSource fastaSource = new FastaSource();
		fastaSource.setName(TEST_FASTA_SOURCE_NAME);
		fastaSource.setUrl(TEST_FASTA_SOUCE_URL);
		curationDao.fastaSources = Lists.newArrayList(fastaSource);
		
		final Map<String, String> ftpDataSourceMap = requester.getFTPDataSources();
		Assert.assertEquals(ftpDataSourceMap.get(TEST_FASTA_SOURCE_NAME), TEST_FASTA_SOUCE_URL);	
	}
	
	@Test
	public void testIsShortnameUniqueTrue() {
		curationDao.curations = Lists.newArrayList();
		Assert.assertTrue(requester.isShortnameUnique("unique"));
	}
	
	@Test
	public void testIsShortnameUniqueFalse() {
		curationDao.curations = Lists.newArrayList(new Curation());
		Assert.assertFalse(requester.isShortnameUnique("not unique"));
	}
	
	@Test
	public void testPerformUpdate() {
		final CurationStub inputCuration = new CurationStub();
		whenReturnTestStub(mockHandler.syncCuration(Mockito.same(inputCuration)));
		assertReturnsTestStub(requester.performUpdate(inputCuration));
	}
	
	@Test
	public void testLookForCuration() {
		whenReturnTestStub(mockHandler.getCachedCurationStub());
		assertReturnsTestStub(requester.lookForCuration());
	}
	
	@Test
	public void testGetCurationById() {
		Integer testId = 7123;
		whenReturnTestStub(mockHandler.getCurationByID(testId));
		assertReturnsTestStub(requester.getCurationByID(testId));
	}
	
	@Test
	public void testDates() {
		final Date from = new Date(), to = new Date();
		final ArrayList<CurationStub> matchingStubs = Lists.newArrayList(new CurationStub());
		Mockito.when(mockHandler.getMatchingCurations(Mockito.same(TEST_CURATION_STUB), Mockito.same(from), Mockito.same(to))).thenReturn(matchingStubs);
		Assert.assertEquals(matchingStubs, requester.getMatches(TEST_CURATION_STUB, from, to));
	}
		
	@Test
	public void testGetLinesOneLine() throws IOException, GWTServiceException {
		mockSessionAttribute("results");
		writeSharedFile("abc\nabcd\nabcde");
		final String[] lines = getLines(0, 1, "b");
		Assert.assertEquals(lines[0], "abc");
	}
	
	@Test
	public void testGetLinesTwoLines() throws IOException, GWTServiceException {
		mockSessionAttribute("results");
		writeSharedFile("abc\nacd\nabcde\nabcdef");
		final String[] lines = getLines(0, 2, "b");
		Assert.assertEquals(lines[0], "abc");
		Assert.assertEquals(lines[1], "abcde");
	}
	
	@Test
	public void testGetLinesSkipLine() throws IOException, GWTServiceException {
		mockSessionAttribute("results");
		writeSharedFile("abc\nabcd\nabcde");
		final String[] lines = getLines(1, 1, "b");
		Assert.assertEquals(lines[0], "abcd");
	}
	
	@Test
	public void testTestPatternPassing() {
		Assert.assertTrue(requester.testPattern("[\\d+]").length() == 0);
	}

	@Test
	public void testTestPatternFailing() {
		Assert.assertTrue(requester.testPattern("[\\d+").length() > 0);
	}

	private String[] getLines(final int startLine, final int numberOfLines, final String pattern) throws GWTServiceException {
		return requester.getLines(testSharedFile.getAbsolutePath(), startLine, numberOfLines, pattern);
	}
	
	private void whenReturnTestStub(final CurationStub callResult) {
		Mockito.when(callResult).thenReturn(TEST_CURATION_STUB);
	}
	
	private void assertReturnsTestStub(final CurationStub callResult) {
		Assert.assertSame(callResult, callResult);
	}
	
	private void mockSessionAttribute(final String attributeName) {
		final ThreadLocal<Object> reference = new ThreadLocal<Object>();
		Mockito.when(mockSession.getAttribute(attributeName)).thenAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) throws Throwable {
				return reference.get();
			}
		});
		Mockito.doNothing().when(mockSession).setAttribute(Mockito.eq(attributeName), Mockito.argThat(new BaseMatcher<Object>() {
			public boolean matches(Object item) {
				reference.set(item);
				return true;
			}

			public void describeTo(Description description) {
			}			
		}));		
	}
	
}
