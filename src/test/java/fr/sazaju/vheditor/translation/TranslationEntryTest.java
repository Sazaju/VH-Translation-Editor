package fr.sazaju.vheditor.translation;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import fr.sazaju.vheditor.translation.TranslationEntry.TranslationListener;

public abstract class TranslationEntryTest {

	protected abstract TranslationEntry createTranslationEntry();

	protected abstract String getInitialReference();

	protected abstract String createNewTranslation(String currentTranslation);

	@Test
	public void testAbstractMethodsProvideProperValues() {
		Set<TranslationEntry> entries = new HashSet<TranslationEntry>();
		for (int i = 0; i < 10; i++) {
			entries.add(createTranslationEntry());
		}
		assertFalse("null instances are provided as entries",
				entries.contains(null));
		assertEquals(
				"the same entries are reused instead of creating new ones", 10,
				entries.size());

		try {
			getInitialReference();
		} catch (Exception e) {
			fail("Exception thrown while asking the reference translation");
		}
		assertNotNull("a translation can be empty, but not null",
				getInitialReference());

		String currentTranslation = getInitialReference();
		for (int i = 0; i < 10; i++) {
			String nextTranslation;
			try {
				nextTranslation = createNewTranslation(currentTranslation);
			} catch (Exception e) {
				fail("Exception thrown while asking a new translation");
				return;
			}
			assertNotNull("a translation can be empty, but not null",
					nextTranslation);
			String errorMessage = "the same translation (" + currentTranslation
					+ ") is returned when asking for a new one";
			assertFalse(errorMessage,
					nextTranslation.equals(currentTranslation));
		}
	}

	@Test
	public void testGetReferenceProperlyRetrievesReferenceTranslationBeforeModification() {
		TranslationEntry entry = createTranslationEntry();
		assertEquals(getInitialReference(), entry.getReferenceTranslation());
	}

	@Test
	public void testGetReferenceProperlyRetrievesReferenceTranslationAfterModification() {
		TranslationEntry entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		assertEquals(getInitialReference(), entry.getReferenceTranslation());
	}

	@Test
	public void testGetCurrentProperlyRetrievesReferenceTranslationBeforeModification() {
		TranslationEntry entry = createTranslationEntry();
		assertEquals(getInitialReference(), entry.getCurrentTranslation());
	}

	@Test
	public void testGetCurrentProperlyRetrievesUpdatedTranslationAfterModification() {
		TranslationEntry entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		assertEquals(translation, entry.getCurrentTranslation());
	}

	@Test
	public void testSetCurrentThrowsExceptionOnNullTranslation() {
		TranslationEntry entry = createTranslationEntry();
		try {
			entry.setCurrentTranslation(null);
			fail("No exception thrown");
		} catch (Exception e) {
		}
	}

	@Test
	public void testReferenceProperlyUpdatedAfterSaveTranslation() {
		TranslationEntry entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.saveTranslation();
		assertEquals(translation, entry.getReferenceTranslation());
	}

	@Test
	public void testGetCurrentProperlyUpdatedAfterResetTranslation() {
		TranslationEntry entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetTranslation();
		assertEquals(getInitialReference(), entry.getCurrentTranslation());
	}

	@Test
	public void testTranslationProperlyMaintainedAfterSaveAll() {
		TranslationEntry entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.saveAll();
		assertEquals(translation, entry.getCurrentTranslation());
	}

	@Test
	public void testReferenceProperlyUpdatedAfterSaveAll() {
		TranslationEntry entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.saveAll();
		assertEquals(entry.getCurrentTranslation(),
				entry.getReferenceTranslation());
	}

	@Ignore
	@Test
	public void testMetadataProperlyMaintainedAfterSaveAll() {
		// TODO
		fail("not implemented yet");
	}

	@Test
	public void testTranslationProperlyDiscardedAfterResetAll() {
		TranslationEntry entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetAll();
		assertFalse(translation.equals(entry.getCurrentTranslation()));
	}

	@Test
	public void testReferenceProperlyMaintainedAfterResetAll() {
		TranslationEntry entry = createTranslationEntry();
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetAll();
		assertEquals(getInitialReference(), entry.getReferenceTranslation());
	}

	@Ignore
	@Test
	public void testMetadataProperlyDiscardedAfterResetAll() {
		// TODO
		fail("not implemented yet");
	}

	@Test
	public void testListenerNotifiedAfterSetCurrentWhenRegistered() {
		TranslationEntry entry = createTranslationEntry();
		final String[] notified = { null };
		entry.addTranslationListener(new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}
		});
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		assertEquals(translation, notified[0]);
	}

	@Test
	public void testListenerNotNotifiedAfterSetCurrentWhenUnregistered() {
		TranslationEntry entry = createTranslationEntry();
		final String[] notified = { null };
		TranslationListener listener = new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}
		};
		entry.addTranslationListener(listener);
		entry.removeTranslationListener(listener);
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		assertNull(notified[0]);
	}

	@Test
	public void testListenerNotifiedAfterResetTranslationWhenRegistered() {
		TranslationEntry entry = createTranslationEntry();
		final String[] notified = { null };
		entry.addTranslationListener(new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}
		});
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetTranslation();
		assertEquals(entry.getReferenceTranslation(), notified[0]);
	}

	@Test
	public void testListenerNotNotifiedAfterResetTranslationWhenUnregistered() {
		TranslationEntry entry = createTranslationEntry();
		final String[] notified = { null };
		TranslationListener listener = new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}
		};
		entry.addTranslationListener(listener);
		entry.removeTranslationListener(listener);
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetTranslation();
		assertNull(notified[0]);
	}

	@Test
	public void testListenerNotifiedAfterResetAllWhenRegistered() {
		TranslationEntry entry = createTranslationEntry();
		final String[] notified = { null };
		entry.addTranslationListener(new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}
		});
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetAll();
		assertEquals(entry.getReferenceTranslation(), notified[0]);
	}

	@Test
	public void testListenerNotNotifiedAfterResetAllWhenUnregistered() {
		TranslationEntry entry = createTranslationEntry();
		final String[] notified = { null };
		TranslationListener listener = new TranslationListener() {

			@Override
			public void translationUpdated(String newTranslation) {
				notified[0] = newTranslation;
			}
		};
		entry.addTranslationListener(listener);
		entry.removeTranslationListener(listener);
		String translation = createNewTranslation(entry.getCurrentTranslation());
		entry.setCurrentTranslation(translation);
		entry.resetAll();
		assertNull(notified[0]);
	}

	@Ignore
	@Test
	public void testFieldListenerNotifiedAfterResetAllWhenRegistered() {
		// TODO
		fail("not implemented yet");
	}

	@Ignore
	@Test
	public void testFieldNotNotifiedAfterResetAllWhenUnregistered() {
		// TODO
		fail("not implemented yet");
	}
}