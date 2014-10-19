package fr.sazaju.vheditor.translation.parsing;

import java.util.Arrays;

import org.junit.Test;

public class EndFlagTest {

	@Test
	public void testSetGetContent() {
		EndFlag end = new EndFlag();
		end.setContent("# END STRING");
		for (String newline : Arrays.asList("\n", "\r", "\n\r", "\r\n")) {
			end.setContent("# END STRING" + newline);
		}
	}

}
