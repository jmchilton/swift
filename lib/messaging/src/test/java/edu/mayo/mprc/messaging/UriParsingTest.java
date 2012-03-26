package edu.mayo.mprc.messaging;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;

public final class UriParsingTest {

	@Test
	public void shouldExtractBrowserUri() throws URISyntaxException {
		final URI uri = new URI("jms.vm://user:passwd@localhost?simplequeue=hello");
		final URI brokerUri = ServiceFactory.extractJmsBrokerUri(uri);
		Assert.assertEquals(brokerUri, new URI("vm://user:passwd@localhost"));

		final URI uri2 = new URI("jms.vm://user:passwd@localhost?test=test&simplequeue=hello");
		final URI brokerUri2 = ServiceFactory.extractJmsBrokerUri(uri2);
		Assert.assertEquals(brokerUri2, new URI("vm://user:passwd@localhost?test=test"));
	}

	@Test
	public void shouldExtractQueueName() throws URISyntaxException {
		final URI uri = new URI("jms.vm://user:passwd@localhost?simplequeue=hello");
		final String name = ServiceFactory.extractJmsQueueName(uri);
		Assert.assertEquals(name, "hello");

		final URI uri2 = new URI("jms.vm://user:passwd@localhost?test=test&simplequeue=hi2");
		final String name2 = ServiceFactory.extractJmsQueueName(uri2);
		Assert.assertEquals(name2, "hi2");
	}

	@Test
	public void shouldParseUserCredentialUris() throws URISyntaxException {
		final URI uri = new URI("vm://user:passwd@localhost");
		final UserInfo info = ServiceFactory.extractJmsUserinfo(uri);
		Assert.assertEquals(info.getUserName(), "user");
		Assert.assertEquals(info.getPassword(), "passwd");
	}

	@Test
	public void shouldNotParseWrappedUserCredentialUris() throws URISyntaxException {
		final URI uri = new URI("failover://(vm://user:passwd@localhost)?a=b");
		final UserInfo info = ServiceFactory.extractJmsUserinfo(uri);
		Assert.assertEquals(info.getUserName(), null);
		Assert.assertEquals(info.getPassword(), null);
	}

	@Test
	public void shouldParseNoUserCredentialUris() throws URISyntaxException {
		final URI uri = new URI("vm://localhost");
		final UserInfo info = ServiceFactory.extractJmsUserinfo(uri);
		Assert.assertEquals(info.getUserName(), null);
		Assert.assertEquals(info.getPassword(), null);
	}


}
