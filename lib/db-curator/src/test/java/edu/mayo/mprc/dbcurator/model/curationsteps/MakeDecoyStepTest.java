package edu.mayo.mprc.dbcurator.model.curationsteps;

import org.testng.Assert;
import org.testng.annotations.Test;

public final class MakeDecoyStepTest {

	@Test
	public void shouldModifyHeaders() {
		Assert.assertEquals(MakeDecoyStep.modifyHeader(">PROT_HUMAN Human protein", "Reversed"),
				">Reversed_PROT_HUMAN (Reversed) Human protein");
		Assert.assertEquals(MakeDecoyStep.modifyHeader(">PROT_HUMAN  Human protein", "Reversed"),
				">Reversed_PROT_HUMAN  (Reversed) Human protein");
		Assert.assertEquals(MakeDecoyStep.modifyHeader("> PROT_HUMAN  Human protein", "Reversed"),
				">Reversed_PROT_HUMAN  (Reversed) Human protein");
		Assert.assertEquals(MakeDecoyStep.modifyHeader("> PROT_HUMAN", "Random"),
				">Random_PROT_HUMAN");
	}

}
