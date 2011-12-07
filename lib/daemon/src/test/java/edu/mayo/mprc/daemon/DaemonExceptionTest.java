package edu.mayo.mprc.daemon;

import edu.mayo.mprc.MprcException;
import edu.mayo.mprc.daemon.exception.DaemonException;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class DaemonExceptionTest {

	@Test
	public void shouldRemoveWithDifferentExceptions() {
		DaemonException base = new DaemonException("Base");
		MprcException wrap1 = new MprcException("Wrap", base);
		MprcException wrap2 = new MprcException("Wrap", wrap1);
		DaemonException wrap3 = new DaemonException("Wrap3", wrap2);
		MprcException wrap4 = new MprcException(wrap3);
		Assert.assertEquals(MprcException.getDetailedMessage(wrap3), "Wrap3 - Wrap - Base");
	}
}
