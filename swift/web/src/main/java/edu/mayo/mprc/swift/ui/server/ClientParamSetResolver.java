package edu.mayo.mprc.swift.ui.server;

import edu.mayo.mprc.swift.params2.SearchEngineParameters;
import edu.mayo.mprc.swift.ui.client.rpc.ClientParamSet;
import edu.mayo.mprc.workspace.User;

/**
 * Takes {@link edu.mayo.mprc.swift.params2.SearchEngineParameters} and translates (resolves) them into an instance of
 * {@link edu.mayo.mprc.swift.ui.client.rpc.ClientParamSet}.
 * The resolution might require creating temporary parameter set and installing them into the user session.
 * The operation is very similar to the user picking an already existing parameter set and then editing it.
 */
public interface ClientParamSetResolver {
	ClientParamSet resolve(SearchEngineParameters parameters, User user);

	/**
	 * @return True if the list of client parameter sets changed as a result of the resolution.
	 */
	boolean isClientParamSetListChanged();
}
