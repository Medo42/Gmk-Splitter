package org.lateralgm.file;

public class PostponeRunner {
    public static void runPostponedRefUpdates() {
        for (GmFileReader.PostponedRef i : GmFileReader.postpone)
        {
            i.invoke();
        }
        GmFileReader.postpone.clear();
    }
}
