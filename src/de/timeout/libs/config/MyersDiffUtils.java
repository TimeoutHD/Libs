package de.timeout.libs.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import com.github.difflib.DiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;

final class MyersDiffUtils {
	
	private MyersDiffUtils() {
		/* EMPTY. IT IS NOT NECESSARY */
	}

	public static String diff3(List<String> original, String dump) {
		// get List
		List<String> dumpList = Arrays.asList(dump.split("\n"));
		
		try {
			// generating diff information
			Patch<String> diff = DiffUtils.diff(original, dumpList);
			
			// run though changes
			for(AbstractDelta<String> delta : new ArrayList<>(diff.getDeltas())) {
				// copy targetList and sourceList
				List<String> targetCopy = new ArrayList<>(delta.getTarget().getLines());
				List<String> sourceCopy = new ArrayList<>(delta.getSource().getLines());
				// check if delta is change delta
				if(delta.getType() == DeltaType.DELETE) {
					// remove comment changes
					removeCommentChanges(sourceCopy);
					// apply
					if(!sourceCopy.isEmpty()) {
						delta.getSource().setLines(sourceCopy);
					} else diff.getDeltas().remove(delta);
				} else if(delta.getType() == DeltaType.CHANGE) {
					// update changes
					applyCommentInsertion(sourceCopy, targetCopy);
					// remove comment changes
					delta.getTarget().setLines(targetCopy);
				}
			}
			// apply patch and return
			return String.join("\n", DiffUtils.patch(original, diff).toArray(new String[0]));
		} catch (DiffException | PatchFailedException e) {
			Bukkit.getLogger().log(Level.SEVERE, "Cannot compute changes. Load old values. Please report this error", e);
		}
		// return dump after error
		return dump;
	}
	
	/**
	 * Removes all Deltas which delete comments in File
	 * @param list the list of changes (sources)
	 */
	private static void removeCommentChanges(List<String> list) {
		// remove all comments
		list.removeIf(filter -> filter.trim().isEmpty() || filter.trim().startsWith("#"));
		// run through lines
		for(int i = 0; i < list.size(); i++) {
			// get Line
			String line = list.get(i);
			// if comment is on this line
			int commentStart = line.indexOf('#');
			if(commentStart != -1) {
				// replace comment change
				list.set(i, line.substring(0, commentStart -1));
			}
		}
	}
	
	/**
	 * Merges all comments in Change-Delta into list target
	 * @param source the line before change
	 * @param target the changes itself
	 */
	private static void applyCommentInsertion(List<String> source, List<String> target) {
		// iterate through source
		for(int i = 0; i < source.size(); i++) {
			String line = source.get(i);
			// if this is a comment line
			if(line.trim().isEmpty() || line.trim().startsWith("#")) {
				// add value to target list
				target.add(i, line);
			} else {
				// check if value has a comment inside
				int commentBegin = line.indexOf('#');
				if(commentBegin != -1) {
					// add comment to target list
					target.set(i, target.get(i) + line.substring(commentBegin));
				}
			}
		}
	}
}
