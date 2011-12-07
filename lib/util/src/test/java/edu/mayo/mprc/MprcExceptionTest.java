package edu.mayo.mprc;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class MprcExceptionTest {

	@Test
	public void shouldListInCorrectOrder() {
		MprcException base = new MprcException("Base");
		MprcException wrap1 = new MprcException("Wrap1", base);
		MprcException wrap2 = new MprcException("Wrap2", wrap1);
		MprcException wrap3 = new MprcException("Wrap3", wrap2);
		Assert.assertEquals(MprcException.getDetailedMessage(wrap3), "Wrap3 - Wrap2 - Wrap1 - Base");
	}

	@Test
	public void shouldRemoveRepeatedMessages() {
		MprcException base = new MprcException("Base");
		MprcException wrap1 = new MprcException("Wrap", base);
		MprcException wrap2 = new MprcException("Wrap", wrap1);
		MprcException wrap3 = new MprcException("Wrap3", wrap2);
		Assert.assertEquals(MprcException.getDetailedMessage(wrap3), "Wrap3 - Wrap - Base");
	}

	@Test
	public void shouldHandleNulls() {
		Assert.assertEquals(MprcException.getDetailedMessage(null), "");
	}

	@Test
	public void shouldHandleSingleException() {
		Assert.assertEquals(MprcException.getDetailedMessage(new MprcException("Hi")), "Hi");
	}

	@Test
	public void shouldHandleEmptyException() {
		Assert.assertEquals(MprcException.getDetailedMessage(new MprcException()), "");
	}

	@Test
	public void shouldRemoveEmptyWrappers() {
		MprcException base = new MprcException("Base");
		MprcException wrap1 = new MprcException(base);
		MprcException wrap2 = new MprcException(wrap1);
		MprcException wrap3 = new MprcException("Wrap3", wrap2);

		Assert.assertEquals(MprcException.getDetailedMessage(wrap3), "Wrap3 - Base");
	}

	@Test
	public void shouldHandleNullPointer() {
		try {
			String s = null;
			s.charAt(0);
		} catch (NullPointerException e) {
			Assert.assertEquals(MprcException.getDetailedMessage(e), "Null pointer exception");
		}
	}
}
