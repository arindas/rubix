package rubix.structure;

import org.junit.Test;
import static org.junit.Assert.*;

public class MiscTest {
	
	@Test 
	public void enumHashing() {
		assertEquals(Direction
			.getDefaultDef().size(), Cube.N_FACES);
	}
}
