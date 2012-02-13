package edu.mayo.mprc.daemon;

import com.google.common.collect.Lists;
import edu.mayo.mprc.daemon.exception.DaemonException;
import edu.mayo.mprc.daemon.files.FileTokenFactory;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.progress.ProgressInfo;
import edu.mayo.mprc.utilities.progress.ProgressListener;
import edu.mayo.mprc.utilities.progress.ProgressReporter;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public final class WorkCacheTest {
	private File cacheFolder;
	private boolean cacheIsStale;

	@Test
	public void shouldCacheWork() {
		TestConnection connection = new TestConnection();

		ProgressReporter reporter = mock(ProgressReporter.class);
		cacheFolder = FileUtilities.createTempFolder();

		TestWorkCache workCache = new TestWorkCache();
		workCache.setCacheFolder(cacheFolder);
		workCache.setDaemon(connection);


		workCache.processRequest(new TestWorkPacket("task1", "request1", null), reporter);
		connection.enqueue(0);
		connection.start(0);

		workCache.processRequest(new TestWorkPacket("task2", "request2", null), reporter);
		connection.enqueue(1);
		connection.start(1);
		connection.progress(1);
		connection.success(1);

		workCache.processRequest(new TestWorkPacket("task1", "request3", null), reporter);
		connection.progress(0);
		connection.success(0);

		workCache.processRequest(new TestWorkPacket("error", "request4", null), reporter);
		connection.enqueue(2);
		connection.start(2);
		connection.progress(2);
		connection.failure(2);

		workCache.processRequest(new TestWorkPacket("task1", "request5", null), reporter);

		cacheIsStale = true;
		workCache.processRequest(new TestWorkPacket("task1", "request6", null), reporter);
		connection.enqueue(3);
		connection.start(3);
		connection.progress(3);
		connection.success(3);

		ArgumentCaptor<TestProgressInfo> argument = ArgumentCaptor.forClass(TestProgressInfo.class);

		verify(reporter, times(6)).reportStart();
		verify(reporter, times(10)).reportProgress(argument.capture());
		verify(reporter, times(5)).reportSuccess();
		verify(reporter, times(1)).reportFailure(Matchers.<Throwable>any());

		final List<TestProgressInfo> allValues = argument.getAllValues();
		int count = 0;
		Assert.assertEquals(allValues.get(count++).getRequest(), "request2");
		Assert.assertEquals(allValues.get(count++).getRequest(), "cache:request2");
		Assert.assertEquals(allValues.get(count++).getRequest(), "request1");
		Assert.assertEquals(allValues.get(count++).getRequest(), "request1"); // Request 3 reported together with 1
		Assert.assertEquals(allValues.get(count++).getRequest(), "cache:request1");
		Assert.assertEquals(allValues.get(count++).getRequest(), "cache:request1"); // Cache request 3 reported together with 1
		Assert.assertEquals(allValues.get(count++).getRequest(), "request4");
		Assert.assertEquals(allValues.get(count++).getRequest(), "cache:request5");
		Assert.assertEquals(allValues.get(count++).getRequest(), "request6");  // Stale cache caused recalculation of request 6
		Assert.assertEquals(allValues.get(count++).getRequest(), "cache:request6");

		Assert.assertEquals(cacheFolder.listFiles().length, 3, "There should be two result folders (two hashes fold into one) and wip folder");
		Assert.assertFalse(workCache.isWorkInProgress(), "There should be no work in progress anymore");

		FileUtilities.cleanupTempFile(cacheFolder);
	}

	public class TestProgressInfo implements ProgressInfo {
		private static final long serialVersionUID = -6401192874783083247L;

		private String request;

		public TestProgressInfo(String request) {
			this.request = request;
		}

		public String getRequest() {
			return request;
		}
	}

	public class TestConnection implements DaemonConnection {
		private ArrayList<ProgressListener> listeners = new ArrayList<ProgressListener>();
		private ArrayList<TestWorkPacket> workPackets = new ArrayList<TestWorkPacket>();

		@Override
		public FileTokenFactory getFileTokenFactory() {
			return null;
		}

		@Override
		public String getConnectionName() {
			return "test";
		}

		@Override
		public void sendWork(WorkPacket workPacket, ProgressListener listener) {
			final TestWorkPacket testPacket = (TestWorkPacket) workPacket;
			listeners.add(listener);
			workPackets.add(testPacket);
		}

		public void enqueue(int index) {
			listeners.get(index).requestEnqueued("localhost");
		}

		public void start(int index) {
			listeners.get(index).requestProcessingStarted();
		}

		public void progress(int index) {
			listeners.get(index).userProgressInformation(new TestProgressInfo(workPackets.get(index).getRequest()));
		}

		public void success(int index) {
			TestWorkPacket packet = workPackets.get(index);
			FileUtilities.ensureFileExists(new File(packet.getFolder(), "file1.txt"));
			FileUtilities.ensureFileExists(new File(packet.getFolder(), "file2.txt"));
			listeners.get(index).requestProcessingFinished();
		}

		public void failure(int index) {
			listeners.get(index).requestTerminated(new DaemonException("Task failed"));
		}

		@Override
		public DaemonRequest receiveDaemonRequest(long timeout) {
			return null;
		}

		@Override
		public void close() {
		}
	}

	public class TestWorkCache extends WorkCache<TestWorkPacket> {
	}

	private class TestWorkPacket extends WorkPacketBase implements CachableWorkPacket {
		private static final long serialVersionUID = -4723515359638194382L;

		private String request;
		private File folder;

		private TestWorkPacket(String taskId, String request, File folder) {
			super(taskId, false);
			this.request = request;
			this.folder = folder;
		}

		public String getRequest() {
			return request;
		}

		public File getFolder() {
			return folder;
		}

		@Override
		public boolean isPublishResultFiles() {
			return false;
		}

		@Override
		public File getOutputFile() {
			return null;
		}

		@Override
		public String getStringDescriptionOfTask() {
			return getTaskId();
		}

		@Override
		public WorkPacket translateToWorkInProgressPacket(File wipFolder) {
			return new TestWorkPacket(getTaskId(), getRequest(), wipFolder);
		}

		@Override
		public List<String> getOutputFiles() {
			return Lists.newArrayList("file1.txt", "file2.txt");
		}

		@Override
		public boolean cacheIsStale(File subFolder, List<String> outputFiles) {
			return cacheIsStale;
		}

		@Override
		public void reportCachedResult(ProgressReporter reporter, File targetFolder, List<String> outputFiles) {
			reporter.reportProgress(new TestProgressInfo("cache:" + getRequest()));
		}

	}
}
