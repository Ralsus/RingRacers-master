package org.kartkrew.ringracers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copies game assets from APK to external storage on first run.
 */
public class AssetCopier {
    private static final String TAG = "RingRacers-Assets";
    private static final int BUFFER_SIZE = 8192;
    
    private final Context context;
    private final String destPath;
    
    public AssetCopier(Context context) {
        this.context = context;
        File extDir = context.getExternalFilesDir(null);
        this.destPath = extDir != null ? extDir.getAbsolutePath() : "/sdcard/ringracers";
    }
    
    public String getGamePath() {
        return destPath;
    }
    
    /**
     * Copy all game assets if not already present.
     * Call this on first launch.
     */
    public void copyAssetsIfNeeded() {
        File destDir = new File(destPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        // Check if already copied (marker file)
        File marker = new File(destPath, ".assets_copied");
        if (marker.exists()) {
            Log.i(TAG, "Assets already copied");
            return;
        }
        
        Log.i(TAG, "Copying game assets to " + destPath);
        
        try {
            copyAssetFolder("gamedata", destPath);
            
            // Create marker
            marker.createNewFile();
            Log.i(TAG, "Assets copied successfully");
        } catch (IOException e) {
            Log.e(TAG, "Failed to copy assets: " + e.getMessage());
        }
    }
    
    /**
     * Copy entire folder from assets.
     */
    private void copyAssetFolder(String srcFolder, String destFolder) throws IOException {
        AssetManager assetManager = context.getAssets();
        String[] files = assetManager.list(srcFolder);
        
        if (files == null || files.length == 0) {
            // It's a file, not a folder
            copyAssetFile(srcFolder, destFolder);
            return;
        }
        
        // Create destination folder
        File destDir = new File(destFolder);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        for (String filename : files) {
            String srcPath = srcFolder.isEmpty() ? filename : srcFolder + "/" + filename;
            String dstPath = destFolder + "/" + filename;
            
            // Check if it's a directory
            String[] subFiles = assetManager.list(srcPath);
            if (subFiles != null && subFiles.length > 0) {
                copyAssetFolder(srcPath, dstPath);
            } else {
                copyAssetFile(srcPath, dstPath);
            }
        }
    }
    
    /**
     * Copy single file from assets.
     */
    private void copyAssetFile(String srcPath, String destPath) throws IOException {
        File destFile = new File(destPath);
        
        // Skip if already exists
        if (destFile.exists()) {
            return;
        }
        
        Log.d(TAG, "Copying: " + srcPath);
        
        AssetManager assetManager = context.getAssets();
        
        try (InputStream in = assetManager.open(srcPath);
             OutputStream out = new FileOutputStream(destFile)) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
    
    /**
     * Check if specific game file exists.
     */
    public boolean hasGameFile(String filename) {
        File file = new File(destPath, filename);
        return file.exists();
    }
    
    /**
     * Get list of .pk3 files in game directory.
     */
    public String[] getGameFiles() {
        File dir = new File(destPath);
        if (!dir.exists()) {
            return new String[0];
        }
        
        return dir.list((d, name) -> name.endsWith(".pk3"));
    }
}
