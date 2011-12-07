package edu.mayo.mprc.scaffold.report;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.io.TsvStreamReader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Reads Scaffold peptide report files.
 */
final class ScaffoldOutputReader implements Closeable {
	private static final char DELIMITER = '\t';
	private TsvStreamReader reader;

	public ScaffoldOutputReader(File scaffoldOutputFile) {
		try {
			this.reader = new TsvStreamReader(scaffoldOutputFile);
		} catch (Exception e) {
			throw new MprcException("Error reading scaffold output file: " + scaffoldOutputFile.getAbsolutePath(), e);
		}
	}

	/**
	 * Returns a string containing Scaffold report, rows sorted by given comparator.
	 *
	 * @param includeHeaders     Include Scaffold column headers.
	 * @param columns            Column indices to be included.
	 * @param groupByColumnIndex Resulting column index to be used for grouping. Blank lines separate the groups.
	 * @param comparator         Comparator used to sort the rows.
	 * @param minColumns         Minimal amount of columns a row has to have to be considered.
	 * @return
	 * @throws IOException
	 */
	public String getRowSortedDataTableContent(boolean includeHeaders, int[] columns, int groupByColumnIndex, Comparator<List<String>> comparator, int minColumns) throws IOException {
		StringBuilder sb = new StringBuilder();

		List<String> row = null;

		TreeSet<List<String>> rows = new TreeSet<List<String>>(comparator);

		//Get header row which is the first row of the data table.
		List<String> headerRow = nextRow(minColumns, columns);

		while ((row = nextRow(minColumns, columns)) != null) {
			rows.add(row);
		}

		if (includeHeaders) {
			boolean first = true;
			for (String str : headerRow) {
				if (!first) {
					sb.append(DELIMITER);
				}
				sb.append(str);
				first = false;
			}

			sb.append("\n");
		}

		boolean group = groupByColumnIndex != -1;

		if (rows.size() > 0) {
			String currentGroupByColumnValue = group ? rows.first().get(groupByColumnIndex) : null;

			for (Iterator<List<String>> rowIterator = rows.iterator(); rowIterator.hasNext();) {

				List<String> sortedRow = rowIterator.next();

				if (group && !currentGroupByColumnValue.equals(sortedRow.get(groupByColumnIndex))) {
					sb.append("\n");
					currentGroupByColumnValue = sortedRow.get(groupByColumnIndex);
				}

				for (Iterator<String> iterator = sortedRow.iterator(); iterator.hasNext();) {
					String str = iterator.next();

					sb.append(str);

					if (iterator.hasNext()) {
						sb.append(DELIMITER);
					}
				}

				if (rowIterator.hasNext()) {
					sb.append("\n");
				}
			}
		}

		return sb.toString();
	}

	public String getRowSortedDataTableContent(boolean includeHeaders, int[] columns, int groupByColumnIndex, Comparator<List<String>> comparator) throws IOException {
		return getRowSortedDataTableContent(includeHeaders, columns, groupByColumnIndex, comparator, columns.length);
	}

	private List<String> nextRow(int minColumns, int[] columns) throws IOException {
		ArrayList<String> rowBuffer = new ArrayList<String>(Math.max(minColumns, columns.length));
		while (reader.nextValues(columns, rowBuffer)) {
			if (rowBuffer.size() >= minColumns) {
				return rowBuffer;
			}
		}
		return null;
	}

	public void close() throws IOException {
		reader.close();
	}
}
