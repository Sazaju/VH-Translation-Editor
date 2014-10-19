package fr.sazaju.vheditor.translation.parsing;

import java.util.Arrays;

import org.junit.Test;

public class TranslationFlagTest {

	@Test
	public void testSetGetContent() {
		TranslationFlag trans = new TranslationFlag();
		for (String newline : Arrays.asList("\n", "\r", "\n\r", "\r\n")) {
			trans.setContent("# TRANSLATION " + newline);
		}
	}

}
