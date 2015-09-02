package fr.sazaju.vheditor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.sazaju.vheditor.VHMap.VHEntry;
import fr.vergne.translation.TranslationMetadata.Field;
import fr.vergne.translation.TranslationProject;
import fr.vergne.translation.TranslationProjectTest;

public class VHProjectTest extends TranslationProjectTest<VHEntry, File, VHMap> {

	@Override
	protected TranslationProject<VHEntry, File, VHMap> createTranslationProject() {
		File tempDirectory;
		try {
			tempDirectory = File.createTempFile("vhTest", "");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		tempDirectory.delete();
		tempDirectory.mkdir();

		File templateDirectory = new File("VH/branches/working");

		try {
			FileUtils.copyDirectory(templateDirectory, tempDirectory);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new VHProject(tempDirectory);
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

	@Test
	public void testAllVHFilesAreRetrieved() {
		File vhDirectory = new File("VH/branches/working");
		VHProject project = new VHProject(vhDirectory);
		int simpleMapCounter = 0;
		List<String> specialMaps = new LinkedList<>();
		specialMaps.add("RPG_RT_ATTRIBUTEDATA.txt");
		specialMaps.add("RPG_RT_COMMONEVENTDATA.txt");
		specialMaps.add("RPG_RT_CONDITIONDATA.txt");
		specialMaps.add("RPG_RT_HERODATA.txt");
		specialMaps.add("RPG_RT_ITEMDATA.txt");
		specialMaps.add("RPG_RT_MONSTERDATA.txt");
		specialMaps.add("RPG_RT_MONSTERPARTIES.txt");
		specialMaps.add("RPG_RT_SKILLDATA.txt");
		specialMaps.add("RPG_RT_STRINGDATA.txt");
		for (File file : project) {
			if (file.getName().matches("^Map[0-9]+\\.txt$")) {
				simpleMapCounter++;
			} else if (specialMaps.contains(file.getName())) {
				specialMaps.remove(file.getName());
			} else {
				fail("Consider a file which is not a map: " + file);
			}
		}
		assertTrue("only " + simpleMapCounter + " simple maps retrieved",
				simpleMapCounter > 600);
		assertTrue("some special maps are not retrieved: " + specialMaps,
				specialMaps.isEmpty());
	}
}
