package fr.sazaju.vheditor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import fr.sazaju.vheditor.VHMap.VHEntry;
import fr.sazaju.vheditor.VHMap.VHMetadata;
import fr.vergne.translation.TranslationEntryTest;
import fr.vergne.translation.TranslationMetadata.Field;

public class VHEntryTest extends TranslationEntryTest<VHMetadata> {

	private final File testFolder = new File("src/test/resources");

	@Override
	protected VHEntry createTranslationEntry() {
		try {
			File templateFile = new File(testFolder, "project/Map1.txt");
			File mapFile = File.createTempFile("Map", ".txt");
			FileUtils.copyFile(templateFile, mapFile);
			VHMap map = new VHMap(mapFile);
			return map.getEntry(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected String getInitialStoredTranslation() {
		return "Entry 1";
	}

	@Override
	protected String createNewTranslation(String currentTranslation) {
		return currentTranslation + ".";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		if (field == VHMap.MARKED_AS_UNTRANSLATED) {
			return (T) (Boolean) !((Boolean) currentValue);
		} else {
			throw new RuntimeException("The field " + field
					+ " is not supposed to be editable");
		}
	}

}
