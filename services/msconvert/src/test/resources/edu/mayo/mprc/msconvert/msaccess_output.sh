#!/bin/bash

PROJECT_DIR="$( cd "$( dirname "$0" )" && pwd )"

DIR=/mnt/mprc/software/public/bumbershoot/pwiz-wine-3_0_4019/
MSACCESS=${DIR}/run_msaccess.sh

QE1=${DIR}/test/qe1_2012oct10_10_postk4_blank2.raw
QE2=${DIR}/test/kj_qe2_2012sep26_12c_phe_inf_us5.RAW
ELITE=${DIR}/test/Lieske_CW_090712_3.raw
ORBI=${DIR}/test/o63_12aug03_05_uc_blank4.RAW

for I in $QE1 $QE2 $ELITE $ORBI; do
        echo Extracting metadata for: $I
        echo --------------------------------------
        $MSACCESS -x metadata $I -o $PROJECT_DIR
        echo Error code: $?
done
