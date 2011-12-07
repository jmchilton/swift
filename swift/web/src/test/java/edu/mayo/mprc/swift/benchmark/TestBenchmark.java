package edu.mayo.mprc.swift.benchmark;

import edu.mayo.mprc.swift.dbmapping.TaskData;
import edu.mayo.mprc.swift.dbmapping.TaskStateData;
import edu.mayo.mprc.workflow.persistence.TaskState;
import junit.framework.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class TestBenchmark {

	private static final TaskData MASCOT_SEARCH = new TaskData(
			"Mascot search",
			new Date(12345678),
			new Date(12345678 + 1000),
			new Date(12345678 + 3500), // Runs for 2.5 seconds
			null,
			new TaskStateData(TaskState.COMPLETED_SUCCESFULLY.getText()),
			"Mascot search of <file>test.txt</file>");

	private static final TaskData SEQUEST_SEARCH = new TaskData(
			"Sequest search",
			new Date(12345678),
			new Date(12345678 + 2000),
			new Date(12345678 + 3000), // Runs for 1 second
			null,
			new TaskStateData(TaskState.COMPLETED_SUCCESFULLY.getText()),
			"Sequest search of <file>test.txt</file>");

	private static final TaskData SEQUEST_FAIL_SEARCH = new TaskData(
			"Sequest search",
			new Date(12345678),
			new Date(12345678 + 2000),
			null,
			null,
			new TaskStateData(TaskState.RUN_FAILED.getText()),
			"Sequest search of <file>test.txt</file>");

	private List<TaskData> tasks = new ArrayList<TaskData>();

	@BeforeMethod
	public void setup() {
		tasks.clear();
	}

	@Test
	public void shouldOutputEmptyTable() throws IOException {
		checkResult(tasks, "");
	}

	@Test
	public void shouldOutputSingleTask() throws IOException {
		tasks.add(MASCOT_SEARCH);
		checkResult(tasks, "Mascot search\n2.5\n");
	}

	@Test
	public void shouldIgnoreNonComplete() throws IOException {
		tasks.add(MASCOT_SEARCH);
		tasks.add(SEQUEST_FAIL_SEARCH);
		checkResult(tasks, "Mascot search\n2.5\n");
	}

	@Test
	public void shouldOutputTwoTasks() throws IOException {
		tasks.add(MASCOT_SEARCH);
		tasks.add(SEQUEST_FAIL_SEARCH);
		tasks.add(SEQUEST_SEARCH);
		checkResult(tasks, "Mascot search,Sequest search\n2.5,1.0\n");
	}


	private void checkResult(List<TaskData> tasks, String expected) throws IOException {
		final Stream outputStream = new Stream();
		Benchmark.printTaskTable(outputStream, tasks);
		Assert.assertEquals(outputStream.getOutput(), expected);
	}

	private class Stream extends ServletOutputStream {
		private StringBuilder output = new StringBuilder(1000);

		public String getOutput() {
			return output.toString();
		}

		@Override
		public void print(String s) throws IOException {
			output.append(s);
		}

		@Override
		public void println(String s) throws IOException {
			output.append(s).append("\n");
		}

		@Override
		public void println() throws IOException {
			output.append("\n");
		}

		@Override
		public void write(int b) throws IOException {
			output.append((char) b);
		}
	}
}
