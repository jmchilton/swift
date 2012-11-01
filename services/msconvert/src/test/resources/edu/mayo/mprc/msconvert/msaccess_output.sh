#!/bin/bash

PROJECT_DIR="$( cd "$( dirname "$0" )" && pwd )"

DIR=/mnt/mprc/software/public/bumbershoot/pwiz-wine-3_0_4019/
MSACCESS=${DIR}/run_msaccess.sh

QE1=${DIR}/test/qe1_2012oct10_10_postk4_blank2.raw
QE2=${DIR}}/test/kj_qe2_2012sep26_12c_phe_inf_us5.RAW
ELITE=${DIR}/test/Lieske_CW_090712_3.raw
ORBI=${DIR}/test/o63_12aug03_05_uc_blank4.RAW

$MSACCESS -x metadata $QE1 -o $PROJECT_DIR
$MSACCESS -x metadata $QE2 -o $PROJECT_DIR
$MSACCESS -x metadata $ELITE -o $PROJECT_DIR
$MSACCESS -x metadata $ORBI -o $PROJECT_DIR


