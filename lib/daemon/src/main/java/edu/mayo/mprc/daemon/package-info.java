/**
 * Everything you need to write your own daemon.
 * <p>
 * A daemon consists of multiple runners that listen to their queue and provide services.
 * <p>
 * Each runner (e.g. {@link #SimpleRunner} is a thread consuming a {@link DaemonConnection},
 * that executes {@link Worker} whenever a new request represented by a {@link WorkPacket} arrives.
 */
package edu.mayo.mprc.daemon;