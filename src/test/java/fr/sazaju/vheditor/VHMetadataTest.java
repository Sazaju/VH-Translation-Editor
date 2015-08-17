package fr.sazaju.vheditor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import fr.vergne.translation.TranslationMetadata;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationMetadataTest;

public class VHMetadataTest extends TranslationMetadataTest {

	private final File testFolder = new File("src/test/resources");

	@Override
	protected TranslationMetadata createTranslationMetadata() {
		try {
			File templateFile = new File(testFolder, "project/Map1.txt");
			File mapFile = File.createTempFile("Map", ".txt");
			FileUtils.copyFile(templateFile, mapFile);
			VHMap map = new VHMap(mapFile);
			return map.getEntry(0).getMetadata();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Collection<Field<?>> getNonEditableFields() {
		return Arrays.<Field<?>> asList(VHMap.CHAR_LIMIT_FACE,
				VHMap.CHAR_LIMIT_NO_FACE, VHMap.CONTEXT);
	}

	@Override
	protected Collection<Field<?>> getEditableFields() {
		return Arrays.<Field<?>> asList(VHMap.MARKED_AS_UNTRANSLATED);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T getInitialStoredValue(Field<T> field) {
		if (field == VHMap.MARKED_AS_UNTRANSLATED) {
			return (T) (Boolean) false;
		} else if (field == VHMap.CHAR_LIMIT_FACE) {
			return (T) (Integer) 35;
		} else if (field == VHMap.CHAR_LIMIT_NO_FACE) {
			return (T) (Integer) 49;
		} else if (field == VHMap.CONTEXT) {
			return (T) "Dialogue/Message/FaceUnknown";
		} else {
			throw new RuntimeException("Unmanaged field: " + field);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createNewEditableFieldValue(Field<T> field, T currentValue) {
		if (field == VHMap.MARKED_AS_UNTRANSLATED) {
			return (T) (Boolean) !((Boolean) currentValue);
		} else {
			throw new RuntimeException("Unmanaged field: " + field);
		}
	}

}
