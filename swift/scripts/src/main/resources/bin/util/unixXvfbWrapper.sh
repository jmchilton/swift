#!/bin/bash
source ~/.bashrc

echo Host name: `hostname`

umask 002

create_xvfb () {
	USERNAME=`whoami`

    # Get latest running Xvfb
    XVFB_PID=`pgrep -n -u $USERNAME ^Xvfb\$`
    if [ ! -z $XVFB_PID ] ; then
        echo Found running Xvfb pid $XVFB_PID
        XVFB_DISPLAY=`ps -p $XVFB_PID -o args= | awk '{ print $2 }'`
        echo Connecting to display $XVFB_DISPLAY
        export DISPLAY=$XVFB_DISPLAY
        xvfb_success=1
    else
        DISPLAYNO=1
        while [ -z $xvfb_success ]; do
            echo Xvfb :${DISPLAYNO} -screen 0 1024x1024x8

            Xvfb :${DISPLAYNO} -screen 0 1024x1024x8 >& /dev/null &
            XVFB_PID=$!
            echo $XVFB_PID
            sleep 1
            if ps --pid $XVFB_PID; then
                echo "Started XVFB on display $DISPLAYNO process $XVFB_PID"
                xvfb_success=1
            else
                echo "Failed to use display "${DISPLAYNO}
                DISPLAYNO=$(($DISPLAYNO + 1))
                XVFB_PID=""
            fi
            done
        export DISPLAY=:${DISPLAYNO}
    fi
    export XVFB_PID
}

if [ $# -lt 1 ]
then
	echo "Usage: unixXvfbWrapper <command>"
	echo "Where <command> is the command to be executed with Xvfb set up"
else
    echo Using Xvfb: `which Xvfb`

    if [ $? != 0 ]
    then
        echo "Xvfb command was not found. Install Xvfb and try again."
        exit 1
    fi

    create_xvfb

    echo "Executing command: $@"
    "$@"

    echo Done. Xvfb is left running at $XVFB_PID
fi
