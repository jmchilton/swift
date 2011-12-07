@echo off
echo TurboSEQUEST - PVM Master v.27 (rev. 12), (c) 1998-2005
echo Molecular Biotechnology, Univ. of Washington, J.Eng/S.Morgan/J.Yates
echo Licensed to Thermo Electron Corp.
echo 
echo NumHosts = 5, NumArch = 1
echo 
echo Arch:LINUX64  CPU:1  Tid:40000  Name:geneva
echo Arch:LINUX64  CPU:2  Tid:80000  Name:node001
echo Arch:LINUX64  CPU:2  Tid:c0000  Name:node002
echo Arch:LINUX64  CPU:2  Tid:100000  Name:node003
echo Arch:LINUX64  CPU:2  Tid:180000  Name:node005
echo 
echo Starting the SEQUEST task on 5 node(s)
echo 
echo Could not start up the SEQUEST slave process on geneva
echo Spawned the SEQUEST slave process [80041] on node001
echo Spawned the SEQUEST slave process [80042] on node001
echo Spawned the SEQUEST slave process [c002c] on node002
echo Spawned the SEQUEST slave process [c002d] on node002
echo Spawned the SEQUEST slave process [10002c] on node003 
echo Spawned the SEQUEST slave process [10002d] on node003
echo Spawned the SEQUEST slave process [18002b] on node005 
echo Spawned the SEQUEST slave process [18002c] on node005 
echo 
echo Waiting for ready messages from 8 node(s)
echo 
echo 1.  received ready messsage from node002.mprc.mayo.edu(c002c)
echo 2.  received ready messsage from node001.mprc.mayo.edu(80042)
echo 3.  received ready messsage from node001.mprc.mayo.edu(80041)
echo 4.  received ready messsage from node002.mprc.mayo.edu(c002d)
echo 5.  received ready messsage from node003.mprc.mayo.edu(10002c)
echo 6.  received ready messsage from node003.mprc.mayo.edu(10002d)
echo 7.  received ready messsage from node005.mprc.mayo.edu(18002b)
echo 8.  received ready messsage from node005.mprc.mayo.edu(18002c)
echo  
echo Spawned 8 slave processes
echo 
echo Search run on geneva using 8 node(s)
echo Params file = /mnt/raid1/test/sharedDiskSpace/tmp/unittest_fast_all15680/params/sequest.params.replaced
echo Peptide tol = 10.00 PPM, fragment tol = 1.0000, MONO/MONO
echo ion series nABY ABCDVWXYZ: 0 1 1 0.0 1.0 0.0 0.0 0.0 0.0 0.0 1.0 0.0
echo Display top = 10/5, ion % = 0.0, CODE = 001040
echo Protein database = /mnt/raid1/databases/dbcurator/ShortTest/ShortTest_params.fasta.hdr, index /mnt/raid1/databases/dbcurator/ShortTest/ShortTest_params.fasta.hdr, (M* +15.9949) (C# +57.0215)
echo 
echo Processing 74 dta's on 8 node(s)
for /L %%I in (1,1,20)  ; do sleep 1
