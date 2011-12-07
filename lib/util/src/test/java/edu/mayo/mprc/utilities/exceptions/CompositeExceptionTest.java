package edu.mayo.mprc.utilities.exceptions;

import edu.mayo.mprc.MprcException;
import org.testng.Assert;
import org.testng.annotations.Test;

public final class CompositeExceptionTest {

	@Test
	public void shouldProvideCleanMessage() {
		MprcException a1 = new MprcException("Disk out of space");
		MprcException a2 = new MprcException("Could not save", a1);

		MprcException b1 = new MprcException("Missing file");
		CompositeException compositeException = new CompositeException("Something failed");
		compositeException.addCause(a2);
		compositeException.addCause(b1);

		Assert.assertEquals(compositeException.getMessage(), "Something failed:\n1) Could not save - Disk out of space\n2) Missing file");
	}
}
