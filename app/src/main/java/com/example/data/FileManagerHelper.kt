package com.example.data

import android.content.Context
import java.io.File

data class WorkspaceFile(
    val name: String,
    val isDirectory: Boolean,
    val size: Long,
    val lastModified: Long,
    val relativePath: String, // relative to workspace root (e.g. "subdir/hello.txt")
    val absoluteFile: File
)

class FileManagerHelper(context: Context) {
    val workspaceRoot: File = File(context.filesDir, "workspace").apply {
        if (!exists()) {
            mkdirs()
        }
        
        // Ensure README.md exists
        val readme = resolve("README.md")
        if (!readme.exists()) {
            readme.writeText(
                "# Welcome to Omni Toolbox!\n\n" +
                "This is your persistent sandboxed workspace accessible via both the **File Manager** and the **Terminal Emulator**.\n\n" +
                "### Quick Tips:\n" +
                "- Use the File Manager to create, rename, and delete folders.\n" +
                "- Switch to the Terminal to run commands like `ls`, `mkdir test`, `touch log.txt`, or `cat README.md`.\n" +
                "- Any changes made here are instantly reflected in both interfaces!\n"
            )
        }

        // Ensure documents/ and todo.txt exist
        val docs = resolve("documents")
        if (!docs.exists()) {
            docs.mkdirs()
        }
        val todo = resolve("documents/todo.txt")
        if (!todo.exists()) {
            todo.writeText(
                "- [ ] Review Omni Toolbox features\n" +
                "- [ ] Create custom notes in Terminal\n" +
                "- [ ] Sync interactive Calendar events\n"
            )
        }

        // Ensure misc/ exists and is seeded with diagnostics, profiles, custom configs, etc.
        val misc = resolve("misc")
        if (!misc.exists()) {
            misc.mkdirs()
            resolve("misc/system_diagnostics.log").writeText(
                "=== OMNI DIAGNOSTICS LOG ===\n" +
                "Timestamp: 2026-06-02T10:00:00Z\n" +
                "Active CPU Threads: 8 / 8 [ONLINE]\n" +
                "RAM Utility: 1.42 GB / 3.84 GB [HEALTHY]\n" +
                "Database Integrity: SQLite Core SQLite3 Validated\n" +
                "Sandboxed Files Cache Node: 0x4F9B [CONNECTED]\n"
            )
            resolve("misc/custom_alias.sh").writeText(
                "alias ll='ls -la'\n" +
                "alias ports='ip address'\n" +
                "alias checksys='neofetch'\n"
            )
            resolve("misc/user_profile.json").writeText(
                "{\n" +
                "  \"operator\": \"Omni User\",\n" +
                "  \"clearance\": \"L5_ADMIN\",\n" +
                "  \"encryption\": \"AES256_CBC\",\n" +
                "  \"node_ip\": \"192.168.1.144\",\n" +
                "  \"terminal_shell\": \"/bin/omnishell\"\n" +
                "}\n"
            )
            resolve("misc/todo_archive.txt").writeText(
                "ARCHIVED TASKS:\n" +
                "- [x] Design Initial Application Framework\n" +
                "- [x] Install Jetpack Compose material libraries\n" +
                "- [x] Set up local database persistence\n"
            )
            resolve("misc/hardware_spec.xml").writeText(
                "<specs>\n" +
                "  <cpu cores=\"8\" arch=\"aarch64\" mhz=\"2400\" />\n" +
                "  <gpu driver=\"vulkan-v1.3\" client=\"jetpack-compose-renderer\" />\n" +
                "  <memory heap=\"512MB\" dynamic=\"true\" />\n" +
                "</specs>\n"
            )
        }

        // Clean up any old simulated sdcard/ and sdcard_ext/ in the sandbox
        try {
            val oldSd = resolve("sdcard")
            if (oldSd.exists() && oldSd.isDirectory) {
                oldSd.deleteRecursively()
            }
            val oldSde = resolve("sdcard_ext")
            if (oldSde.exists() && oldSde.isDirectory) {
                oldSde.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Ensure non-sandbox /sdcard exists and is seeded with media metadata if possible
        try {
            val sd = File("/sdcard")
            if (!sd.exists()) {
                sd.mkdirs()
            }
            if (sd.exists()) {
                val dcim = File(sd, "DCIM")
                if (!dcim.exists()) dcim.mkdirs()
                val camMeta = File(dcim, "camera_test.jpg_meta.txt")
                if (!camMeta.exists()) {
                    camMeta.writeText(
                        "Camera Snapshot Meta:\nResolution: 4032x3024\nISO: 100\nFocal Length: 4.25mm\nDate: 2026-06-02"
                    )
                }
                val downloads = File(sd, "Downloads")
                if (!downloads.exists()) downloads.mkdirs()
                val setupPdf = File(downloads, "setup_guide.pdf_meta.txt")
                if (!setupPdf.exists()) {
                    setupPdf.writeText(
                        "Download Attachment Meta:\nTitle: Omni Setup Manual\nBytes: 154201\nSource: local_sandbox"
                    )
                }
                val music = File(sd, "Music")
                if (!music.exists()) music.mkdirs()
                val synthWave = File(music, "synth_wave.mp3_meta.txt")
                if (!synthWave.exists()) {
                    synthWave.writeText(
                        "Sample Audio Meta:\nTrack: Neon Horizon\nArtist: Sandbox Wave\nDuration: 3:45"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Ensure non-sandbox /sdcard_ext exists and is seeded with backup metadata if possible
        try {
            val sde = File("/sdcard_ext")
            if (!sde.exists()) {
                sde.mkdirs()
            }
            if (sde.exists()) {
                val backups = File(sde, "Backups")
                if (!backups.exists()) backups.mkdirs()
                val sysState = File(backups, "system_state_2026.bak")
                if (!sysState.exists()) {
                    sysState.writeText(
                        "=== VIRTUAL BACKUP INDEX ===\n" +
                        "Version: v1.1.0\n" +
                        "Checksum: 0x9AF83B\n" +
                        "Status: READONLY_ARCHIVE"
                    )
                }
                val archives = File(sde, "Archives")
                if (!archives.exists()) archives.mkdirs()
                val projRetro = File(archives, "project_retro_source.zip_meta.txt")
                if (!projRetro.exists()) {
                    projRetro.writeText(
                        "Source Archive Metadata:\nRepository: omni-toolbox\nFormat: ZIP Compressed\nDate: 2026-05-15"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Create symbolic links to actual /sdcard and /sdcard_ext in workspace/misc
        try {
            val miscDir = resolve("misc")
            if (miscDir.exists()) {
                val sdcardLink = File(miscDir, "sdcard")
                val sdcardTarget = File("/sdcard")
                if (!sdcardLink.exists()) {
                    java.nio.file.Files.createSymbolicLink(
                        sdcardLink.toPath(),
                        sdcardTarget.toPath()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val miscDir = resolve("misc")
            if (miscDir.exists()) {
                val sdcardExtLink = File(miscDir, "sdcard_ext")
                val sdcardExtTarget = File("/sdcard_ext")
                if (!sdcardExtLink.exists()) {
                    java.nio.file.Files.createSymbolicLink(
                        sdcardExtLink.toPath(),
                        sdcardExtTarget.toPath()
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Returns a list of files/directories in the physical path corresponding to a logical relative path.
     */
    fun listFiles(relativeDir: String): Result<List<WorkspaceFile>> {
        return try {
            val targetDir = resolveRelativePath(relativeDir)
            if (!targetDir.exists()) {
                if (targetDir.absolutePath == "/sdcard" || targetDir.absolutePath == "/sdcard_ext") {
                    targetDir.mkdirs()
                }
                if (!targetDir.exists()) {
                    return Result.failure(Exception("Path does not exist: $relativeDir"))
                }
            }
            if (!targetDir.isDirectory) {
                return Result.failure(Exception("Path is not a directory: $relativeDir"))
            }

            val list = targetDir.listFiles() ?: emptyArray()
            val result = list.map { file ->
                val pathStr = file.absolutePath
                val rel = when {
                    pathStr == "/sdcard_ext" -> "sdcard_ext"
                    pathStr.startsWith("/sdcard_ext/") -> {
                        val sub = pathStr.removePrefix("/sdcard_ext/")
                        "sdcard_ext/$sub"
                    }
                    pathStr == "/sdcard" -> "sdcard"
                    pathStr.startsWith("/sdcard/") -> {
                        val sub = pathStr.removePrefix("/sdcard/")
                        "sdcard/$sub"
                    }
                    else -> {
                        try {
                            file.relativeTo(workspaceRoot).path
                        } catch (e: Exception) {
                            file.name
                        }
                    }
                }
                WorkspaceFile(
                    name = file.name,
                    isDirectory = file.isDirectory,
                    size = if (file.isDirectory) 0 else file.length(),
                    lastModified = file.lastModified(),
                    relativePath = rel,
                    absoluteFile = file
                )
            }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resolves a logical path relative to workspace root. Prevents directory traversal attacks.
     */
    fun resolveRelativePath(relative: String): File {
        val cleaned = relative.trim().removePrefix("/").removeSuffix("/")
        if (cleaned.isEmpty() || cleaned == "." || cleaned == "root") {
            return workspaceRoot
        }

        // Intercept external roots
        if (cleaned == "sdcard") {
            return File("/sdcard")
        }
        if (cleaned.startsWith("sdcard/")) {
            val sub = cleaned.substringAfter("sdcard/").trim().removePrefix("/").removeSuffix("/")
            return if (sub.isEmpty()) File("/sdcard") else File("/sdcard", sub)
        }
        if (cleaned == "sdcard_ext") {
            return File("/sdcard_ext")
        }
        if (cleaned.startsWith("sdcard_ext/")) {
            val sub = cleaned.substringAfter("sdcard_ext/").trim().removePrefix("/").removeSuffix("/")
            return if (sub.isEmpty()) File("/sdcard_ext") else File("/sdcard_ext", sub)
        }

        val file = File(workspaceRoot, cleaned)
        val canonical = try { file.canonicalPath } catch (e: Exception) { file.absolutePath }
        
        // Allow if it's within workspace root, OR within /sdcard, OR within /sdcard_ext
        if (canonical.startsWith(workspaceRoot.canonicalPath) || 
            canonical.startsWith("/sdcard") || 
            canonical.startsWith("/sdcard_ext")) {
            return file
        }
        return workspaceRoot
    }

    fun makeDirectory(parentRelative: String, name: String): Result<File> {
        return try {
            val parentFile = resolveRelativePath(parentRelative)
            val newDir = File(parentFile, name)
            if (newDir.exists()) {
                return Result.failure(Exception("Folder '$name' already exists"))
            }
            if (newDir.mkdirs()) {
                Result.success(newDir)
            } else {
                Result.failure(Exception("Failed to create folder '$name'"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun makeFile(parentRelative: String, name: String, initialContent: String = ""): Result<File> {
        return try {
            val parentFile = resolveRelativePath(parentRelative)
            val newFile = File(parentFile, name)
            if (newFile.exists()) {
                return Result.failure(Exception("File '$name' already exists"))
            }
            newFile.writeText(initialContent)
            Result.success(newFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun deleteFileOrDirectory(relative: String): Result<Boolean> {
        return try {
            val file = resolveRelativePath(relative)
            if (file == workspaceRoot) {
                return Result.failure(Exception("Cannot delete workspace root"))
            }
            if (file.absolutePath == "/sdcard" || file.absolutePath == "/sdcard_ext") {
                return Result.failure(Exception("Cannot delete external root"))
            }
            if (!file.exists()) {
                return Result.failure(Exception("Target path does not exist"))
            }
            val deleted = file.deleteRecursively()
            if (deleted) Result.success(true) else Result.failure(Exception("Failed to delete '$relative'"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun rename(currentRelative: String, newName: String): Result<File> {
        return try {
            val file = resolveRelativePath(currentRelative)
            if (file == workspaceRoot) {
                return Result.failure(Exception("Cannot rename workspace root"))
            }
            if (file.absolutePath == "/sdcard" || file.absolutePath == "/sdcard_ext") {
                return Result.failure(Exception("Cannot rename external root"))
            }
            if (!file.exists()) {
                return Result.failure(Exception("Target does not exist"))
            }
            val destination = File(file.parentFile, newName)
            if (destination.exists()) {
                return Result.failure(Exception("A file or folder with that name already exists at destination"))
            }
            if (file.renameTo(destination)) {
                Result.success(destination)
            } else {
                Result.failure(Exception("Failed to rename '$currentRelative' to '$newName'"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun readFileContent(relative: String): Result<String> {
        return try {
            val file = resolveRelativePath(relative)
            if (!file.exists()) {
                return Result.failure(Exception("File does not exist"))
            }
            if (file.isDirectory) {
                return Result.failure(Exception("Path is a directory, cannot read as text"))
            }
            Result.success(file.readText())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeFileContent(relative: String, content: String): Result<Boolean> {
        return try {
            val file = resolveRelativePath(relative)
            if (file.isDirectory) {
                return Result.failure(Exception("Path is a directory, cannot write text to it"))
            }
            file.writeText(content)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
