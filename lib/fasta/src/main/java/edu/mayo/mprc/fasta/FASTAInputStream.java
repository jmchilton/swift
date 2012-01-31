package edu.mayo.mprc.fasta;

import com.google.common.base.Charsets;
import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.utilities.FileUtilities;
import edu.mayo.mprc.utilities.GZipUtilities;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * An input stream to handle FASTA data files.  It will read in the files and allow access to each header and sequence in
 * the file.
 *
 * @author Eric J. Winter Date: Apr 10, 2007 Time: 9:00:59 AM
 */
public final class FASTAInputStream implements DBInputStream {
    private static final char FASTA_HEADER = '>';

    /**
     * The file that we are using as input
     */
    private File fastaFile;

    /**
     * A reader to access the above file
     */
    private BufferedReader reader;

    /**
     * the header of the sequence at the current location
     */
    private String currentHeader;

    /**
     * the sequence that is at the current position in the file
     */
    private String currentSequence;

    /**
     * the header that comes next and will become the currentHeader when next is called
     */
    private String nextHeader;

    private static final Logger LOGGER = Logger.getLogger(FASTAInputStream.class);

    /**
     * create a new stream with the given file
     *
     * @param file the file that you want to create an input stream of
     *             ¬
     */
    public FASTAInputStream(File file) {
        try {
            this.fastaFile = file;
            reopenReader();
        } catch (Exception e) {
            throw new MprcException("Could not open FASTA file [" + file.getAbsolutePath() + "]", e);
        }
    }

    private void reopenReader() throws IOException {
        if (GZipUtilities.isGZipped(fastaFile)) {
            this.reader = new BufferedReader(new InputStreamReader(GZipUtilities.getCompressedFileAsStream(fastaFile), Charsets.ISO_8859_1));
        } else {
            this.reader = FileUtilities.getReader(fastaFile);
        }
    }

    /**
     * goes to the first sequence in the sequence database file so that the next call to getHeader() will return the
     * first header in the file.
     */
    public void beforeFirst() {
        try {
            FileUtilities.closeQuietly(this.reader);
            reopenReader();
            this.nextHeader = this.reader.readLine();
        } catch (Exception e) {
            throw new MprcException("Cannot reopen fasta database " + this.fastaFile.getAbsolutePath(), e);
        }
    }

    /**
     * Advances to the next sequence in the database file and returns true unless there is no more sequences in the file
     * and then false is returned.
     *
     * @return false if we are already at the end of the file else true
     */
    public boolean gotoNextSequence() {
        //we should have been left after reading a header because that is how we detect
        //the end of the next sequence
        if (nextHeader == null) {
            return false;
        }
        this.currentHeader = nextHeader;

        //read in lines until we reach the next header
        StringBuilder sequenceBuilder = new StringBuilder();
        String nextLine = null;
        try {
            nextLine = this.reader.readLine();
        } catch (IOException e) {
            LOGGER.warn(e);
        }
        //if the next line is not a header or an end of line then append it to the sequence
        while (isSequence(nextLine)) {
            sequenceBuilder.append(nextLine);
            try {
                //read in the next line
                nextLine = this.reader.readLine();
            } catch (IOException e) {
                LOGGER.warn(e);
            }
        }

        while (nextLine != null && !isHeader(nextLine)) {
            try {
                nextLine = this.reader.readLine();
            } catch (IOException e) {
                LOGGER.warn(e);
            }
        }
        this.nextHeader = nextLine;

        //set the current sequence to the concatenation of all strings
        this.currentSequence = sequenceBuilder.toString();

        //return true since we will have a next header
        return true;
    }

    /**
     * Determines if a String is part of a sequence (lack of '>' character while containing some characters
     *
     * @param potentialSequence the string that is suspected to be a header
     * @return true if the string is a header or if it is null (meaning end of file)
     */
    private static boolean isSequence(String potentialSequence) {
        return !(potentialSequence == null || potentialSequence.length() == 0 || potentialSequence.charAt(0) == FASTA_HEADER);
    }

    private static boolean isHeader(String potentialHeader) {
        return !(potentialHeader == null || potentialHeader.length() == 0) && potentialHeader.charAt(0) == FASTA_HEADER;
    }

    /**
     * gets the header of the current sequence in the file.
     *
     * @return the current sequence's header
     */
    public String getHeader() {
        return this.currentHeader;
    }

    /**
     * gets the sequence portion of the curent sequence in the file
     *
     * @return the current sequence
     */
    public String getSequence() {
        return this.currentSequence;
    }

    /**
     * performs any cleaning up that may be necessary.  Always call when you are done.
     */
    public void close() {
        FileUtilities.closeQuietly(this.reader);
    }

    /**
     * goes through each header in a fasta file and checks to make sure it is a valid fasta header.  If any problems
     * are encountered or a header does not check out then false is returned
     *
     * @param toCheck the file you want to see is a valid FASTA file
     * @return true if the file is a valid fasta file else false
     */
    public static boolean isFASTAFileValid(File toCheck) {
        DBInputStream in = null;
        try {
            in = new FASTAInputStream(toCheck);
            int sequenceCount = 0;
            in.beforeFirst();
            while (in.gotoNextSequence()) {
                if (isHeader(in.getHeader())) {
                    sequenceCount++;
                } else {
                    return false;
                }
            }
            return (sequenceCount != 0);
        } catch (Exception e) {
            // SWALLOWED: We just return false as in "not valid"
            LOGGER.warn(e);
            return false;
        } finally {
            FileUtilities.closeQuietly(in);
        }
    }
}
