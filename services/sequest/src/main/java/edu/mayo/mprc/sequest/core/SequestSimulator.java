package edu.mayo.mprc.sequest.core;

import java.io.*;

/**
 * Simulator  for sequest27_master.exe
 * writes out the sequest.log, creates the .out files, then pauses for WAIT_TIME
 */
final class SequestSimulator {
	private static final long WAIT_TIME = 50 * 1000;
	private static final String[] SEQUEST_LOG = {
			"TurboSEQUEST - PVM Master v.27 (rev. 12), (c) 1998-2005",
			"Molecular Biotechnology, Univ. of Washington, J.Eng/S.Morgan/J.Yates",
			"Licensed to Thermo Electron Corp.",
			"",
			"NumHosts = 5, NumArch = 1",
			"",
			"Arch:LINUX64  CPU:1  Tid:40000  Name:geneva",
			"Arch:LINUX64  CPU:2  Tid:80000  Name:node001",
			"Arch:LINUX64  CPU:2  Tid:c0000  Name:node002",
			"Arch:LINUX64  CPU:2  Tid:100000  Name:node003",
			"Arch:LINUX64  CPU:2  Tid:180000  Name:node005",
			"",
			"Starting the SEQUEST task on 5 node(s)",
			"",
			"Could not start up the SEQUEST slave process on geneva",
			"Spawned the SEQUEST slave process [80041] on node001",
			"Spawned the SEQUEST slave process [80042] on node001",
			"Spawned the SEQUEST slave process [c002c] on node002",
			"Spawned the SEQUEST slave process [c002d] on node002",
			"Spawned the SEQUEST slave process [10002c] on node003 ",
			"Spawned the SEQUEST slave process [10002d] on node003",
			"Spawned the SEQUEST slave process [18002b] on node005 ",
			"Spawned the SEQUEST slave process [18002c] on node005 ",
			"",
			"Waiting for ready messages from 8 node(s)",
			"",
			"1.  received ready messsage from node002.mprc.mayo.edu(c002c)",
			"2.  received ready messsage from node001.mprc.mayo.edu(80042)",
			"3.  received ready messsage from node001.mprc.mayo.edu(80041)",
			"4.  received ready messsage from node002.mprc.mayo.edu(c002d)",
			"5.  received ready messsage from node003.mprc.mayo.edu(10002c)",
			"6.  received ready messsage from node003.mprc.mayo.edu(10002d)",
			"7.  received ready messsage from node005.mprc.mayo.edu(18002b)",
			"8.  received ready messsage from node005.mprc.mayo.edu(18002c)",
			" ",
			"Spawned 8 slave processes",
			"",
			"Search run on geneva using 8 node(s)",
			"Params file = /mnt/raid1/test/sharedDiskSpace/tmp/unittest_fast_all15680/params/sequest.params.replaced",
			"Peptide tol = 10.00 PPM, fragment tol = 1.0000, MONO/MONO",
			"ion series nABY ABCDVWXYZ: 0 1 1 0.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0",
			"Display top = 10/5, ion % = 0.0, CODE = 001040",
			"Protein database = /mnt/raid1/databases/dbcurator/ShortTest/ShortTest_params.fasta.hdr, index /mnt/raid1/databases/dbcurator/ShortTest/ShortTest_params.fasta.hdr",
			"(M* +15.9949) (C# +57.0215)",
			"",
			"Processing 74 dta's on 8 node(s)",
	};

	private SequestSimulator() {
	}

	public static void main(String[] args) {
		// write the line above out to a log file
		File out = new File("sequest.log");
		BufferedWriter bf = null;
		try {
			BufferedOutputStream s = new BufferedOutputStream(new FileOutputStream(out));
			Writer w = new OutputStreamWriter(s);
			bf = new BufferedWriter(w);
			for (String aSEQUEST_LOG : SEQUEST_LOG) {
				try {
					bf.write(aSEQUEST_LOG + "\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				bf.close();
				bf = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (bf != null) {
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}


	}
}
