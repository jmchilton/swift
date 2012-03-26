package edu.mayo.mprc.daemon;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.exception.DaemonException;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class DaemonExceptionTest {

	@Test
	public void shouldRemoveWithDifferentExceptions() {
		final DaemonException base = new DaemonException("Base");
		final MprcException wrap1 = new MprcException("Wrap", base);
		final MprcException wrap2 = new MprcException("Wrap", wrap1);
		final DaemonException wrap3 = new DaemonException("Wrap3", wrap2);
		final MprcException wrap4 = new MprcException(wrap3);
		Assert.assertEquals(MprcException.getDetailedMessage(wrap3), "Wrap3 - Wrap - Base");
	}
}
