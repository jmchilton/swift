package edu.mayo.mprc.sequest.core;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.tar.TarWriter;
import edu.mayo.mprc.utilities.FileUtilities;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

final class Dta2TarWriter {
	private static final Logger LOGGER = Logger.getLogger(Dta2TarWriter.class);
	private static final String OUT_EXT = ".out";

	public Dta2TarWriter() {
	}


	/*
  * find the matching .out files based on same prefix
  * then insert the dta's and out's into the tar in order
  * dta, out, dta, out...
  * @param dtaFileNames - the dta file names
  * @param outputDir - tar file will be placed here
  *
  */
	public void writeDtaFilesToTar(List<String> dtaFileNames, File outputDir, TarWriter tarWriter) {
		List<File> allFiles = new ArrayList<File>();
		for (String fileName1 : dtaFileNames) {
			File dtaFile = new File(fileName1);
			String fileName = dtaFile.getAbsolutePath();
			if (!new File(fileName).isFile()) {
				throw new MprcException("not a file : " + fileName);
			}
			String prefix = FileUtilities.stripExtension(fileName);
			String outFileName = prefix + OUT_EXT;
			if (!new File(outFileName).isFile()) {
				// move all the dtas and out files to a backup folder so can use for troubleshooting
				// find the temporary folder
				String path = dtaFile.getParent();

				String lastFolder = FileUtilities.getLastFolderName(path);
				String topPath = path;
				File someFile = new File(path);
				if (someFile.getParent() != null) {
					topPath = someFile.getParent();
				}
				File p = new File(path);
				File dest = new File(new File(topPath), "outs");
				dest = new File(dest, lastFolder);
				LOGGER.debug("moving .out files to " + dest.getAbsolutePath());
				FileUtilities.rename(p, dest);
				// also move files in outputDir to the 'outs' folder
				for (File file : outputDir.listFiles()) {
					FileUtilities.rename(file, new File(dest, file.getName()));
				}
				throw new MprcException("tar failed, as sequest out file does not exist :" + outFileName);
			}
			File out = new File(outFileName);
			allFiles.add(dtaFile);
			allFiles.add(out);

		}
		// now tar these files
		if (allFiles.size() > 0) {
			tarWriter.addFiles(allFiles);
		}

	}


}
