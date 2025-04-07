package org.uvccamera.flutter;

import java.io.File;

/**
 * Handler to be notified when the take-picture result is available.
 */
@FunctionalInterface
/* package-private */ interface UvcCameraTakePictureResultHandler {

    /**
     * Called when the take-picture result is available
     *
     * @param outputFile the output file to which the picture is saved
     *                   or null if the picture could not be taken
     * @param error      the error that occurred while taking the picture
     */
    void onResult(File outputFile, Exception error);

}
