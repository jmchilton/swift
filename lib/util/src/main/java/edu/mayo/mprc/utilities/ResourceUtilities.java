package edu.mayo.mprc.utilities;

import com.google.common.base.Joiner;
import edu.mayo.mprc.MprcException;

import java.io.*;
import java.net.*;
import java.util.Date;

/**
 * Utility functions for obtaining streams/readers for resources in various locations.
 * <p/>
 * <p/>
 * In particular this deals with classpath URIs which have the form:
 * <pre>classpath:path/relative/to/classpath/resource.foo</pre>
 * <p/>
 * <p/>
 * We could have registered a custom protocol handler for classpath URLs but, apparently,
 * that's a royal pain in the ass:
 * <p/>
 * http://www.unicon.net/node/776
 */
public final class ResourceUtilities {
	private ResourceUtilities() {

	}

	/**
	 * Given a URI, if it's a classpath: URI, grab that resource, otherwise
	 * convert it to a URL and open a connection to it.
	 *
	 * @param u  URI (possibly classpath: scheme) to resource desired.
	 * @param cl ClassLoader to fetch from.
	 * @return an InputStream to that resource.
	 */
	public static InputStream getStream(URI u, ClassLoader cl) {
		if (u.getScheme().equals("classpath")) {
			InputStream stream = cl.getResourceAsStream(u.getSchemeSpecificPart());
			if (stream == null) {
				throw new MprcException("Can't find " + u.getSchemeSpecificPart()
						+ " in classpath: " + getClassPathString(cl));
			}
			return stream;
		} else {
			try {
				URL uu = u.toURL();
				URLConnection c = uu.openConnection();
				c.connect();
				return c.getInputStream();
			} catch (IOException e) {
				throw new MprcException("Can't open connection to " + u.toString(), e);
			}
		}
	}

	/**
	 * @param cl ClassLoader to use for determining the path.
	 * @return Class path string, separate
	 */
	public static String getClassPathString(ClassLoader cl) {
		StringBuilder cp = new StringBuilder();
		try {
			if (cl instanceof URLClassLoader) {
				URLClassLoader ucl = (URLClassLoader) cl;
				Joiner.on(" ").join(ucl.getURLs());
				for (URL url : ucl.getURLs()) {
					cp.append(url);
				}
			}
		} catch (ClassCastException ignore) {
			// SWALLOWED: ignore = we are just building classpath to output
		}
		return cp.toString();
	}

	public static Date getLastModified(URI u, ClassLoader cl) {
		URL url;
		if (u.getScheme().equals("classpath")) {
			url = cl.getResource(u.getSchemeSpecificPart());
		} else {
			try {
				url = u.toURL();
			} catch (MalformedURLException e) {
				throw new MprcException("Invalid resource URI " + u.toString(), e);
			}
		}

		try {
			URLConnection c = url.openConnection();
			return new Date(c.getLastModified());
		} catch (IOException e) {
			throw new MprcException("Can't open connection to " + url.toString(), e);
		}
	}

	/**
	 * Downloads file from given URL.
	 *
	 * @param fileURL
	 * @param outputFile
	 * @throws IOException
	 */
	public static void downloadFile(URL fileURL, File outputFile) throws IOException {

		URLConnection urlConnection = null;
		InputStream is = null;
		OutputStream os = null;

		try {
			byte[] bytes = new byte[256];
			int numberOfReadBytes = 0;

			urlConnection = fileURL.openConnection();
			urlConnection.connect();

			is = fileURL.openStream();
			os = new FileOutputStream(outputFile);

			while ((numberOfReadBytes = is.read(bytes)) != -1) {
				os.write(bytes, 0, numberOfReadBytes);
			}
		} finally {
			FileUtilities.closeQuietly(is);
			FileUtilities.closeQuietly(os);
		}
	}

	public static InputStream getStream(URI u, Class c) {
		return getStream(u, c.getClassLoader());
	}

	public static Reader getReader(URI u, Object o) {
		return getReader(u, o.getClass());
	}

	public static Reader getReader(URI u, Class c) {
		return getReader(u, c.getClassLoader());
	}

	public static Reader getReader(URI u, ClassLoader cl) {
		return new InputStreamReader(getStream(u, cl));
	}

	public static InputStream getStream(String u, Class c) {
		return getStream(toURI(u), c.getClassLoader());
	}

	public static InputStream getStream(String u, Object o) {
		return getStream(toURI(u), o.getClass());
	}

	public static Reader getReader(String u, Object o) {
		return getReader(toURI(u), o.getClass());
	}

	public static Reader getReader(String u, Class c) {
		return getReader(toURI(u), c.getClassLoader());
	}

	public static Reader getReader(String u, ClassLoader cl) {
		return new InputStreamReader(getStream(toURI(u), cl));
	}

	public static URI toURI(String s) {
		try {
			String trimmed = s.trim();
			// Make sure we do not misinterpret windows C:\paths as URIs.
			if (trimmed.contains(":") && trimmed.indexOf(':') != 1) {
				return new URI(s);
			} else {
				return new File(s).toURI();
			}
		} catch (URISyntaxException e) {
			throw new MprcException("Can't create URI from " + s, e);
		}
	}

	public static URI append(URI to, String pathPart) {
		// it really seems like URI.resolve() should do this but it just doesn't.
		String path = to.getPath();
		try {
			path = to.getSchemeSpecificPart();
			if (!path.endsWith("/")) {
				path += "/";
			}
			path += pathPart;
			return new URI(to.getScheme(), path, to.getFragment());
		} catch (URISyntaxException e) {
			throw new MprcException(e);
		}
		//}
	}
}
