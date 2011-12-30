package edu.mayo.mprc.dbcurator.server;

import com.google.common.base.Supplier;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import edu.mayo.mprc.common.client.GWTServiceException;
import edu.mayo.mprc.dbcurator.client.curatorstubs.CurationStub;
import edu.mayo.mprc.dbcurator.client.curatorstubs.HeaderTransformStub;
import edu.mayo.mprc.dbcurator.client.services.CommonDataRequester;
import edu.mayo.mprc.dbcurator.model.persistence.CurationDao;

import java.util.*;

import javax.servlet.http.HttpSession;

/**
 * @author Eric Winter
 */
public final class CommonDataRequesterImpl extends RemoteServiceServlet implements CommonDataRequester {
	private static final long serialVersionUID = 20071220L;

	private CommonDataRequesterDelegateImpl delegate;

	public CommonDataRequesterImpl() {
		CurationDao curationDao = CurationWebContext.getCurationDAO();
		Supplier<HttpSession> sessionSupplier = new Supplier<HttpSession>() {
			public HttpSession get() {
				return getThreadLocalRequest().getSession();
			}
		};
		delegate = new CommonDataRequesterDelegateImpl(curationDao, sessionSupplier);
	}

	CommonDataRequesterImpl(CurationDao curationDao, Supplier<HttpSession> sessionSupplier) {
		delegate = new CommonDataRequesterDelegateImpl(curationDao, sessionSupplier);
	}
	
	public List<HeaderTransformStub> getHeaderTransformers() {
		return delegate.getHeaderTransformers();
	}

	public Map<String, String> getFTPDataSources()  {
		return delegate.getFTPDataSources();
	}

	public Boolean isShortnameUnique(String toCheck)  {
		return delegate.isShortnameUnique(toCheck);
	}

	public CurationStub performUpdate(CurationStub toUpdate)
			 {
		return delegate.performUpdate(toUpdate);
	}

	public CurationStub lookForCuration()  {
		return delegate.lookForCuration();
	}

	public CurationStub getCurationByID(Integer id)  {
		return delegate.getCurationByID(id);
	}

	public CurationStub copyCurationStub(CurationStub toCopy) {
		return delegate.copyCurationStub(toCopy);
	}

	public List<CurationStub> getMatches(CurationStub toMatch) {
		return delegate.getMatches(toMatch);
	}

	public List<CurationStub> getMatches(CurationStub toMatch,
			Date earliestRun, Date latestRun)  {
		return delegate.getMatches(toMatch, earliestRun, latestRun);
	}

	public CurationStub runCuration(CurationStub toRun) {
		return delegate.runCuration(toRun);
	}

	public String[] getLines(String sharedPath, int startLineInclusive,
			int numberOfLines, String pattern) throws GWTServiceException  {
		return delegate.getLines(sharedPath, startLineInclusive, numberOfLines,
				pattern);
	}

	public void setCancelMessage(boolean cancelMessage) throws GWTServiceException {
		delegate.setCancelMessage(cancelMessage);
	}

	public String testPattern(String pattern)  {
		return delegate.testPattern(pattern);
	}

	public String[] getResults() throws GWTServiceException  {
		return delegate.getResults();
	}

}