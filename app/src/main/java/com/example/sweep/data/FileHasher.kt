package com.example.sweep.data

import android.content.Context
import android.net.Uri
import java.io.IOException
import java.security.MessageDigest

/**
 * Computes content hashes for files addressed by content
 *
 * The file is streamed through the digest in small chunks, so even a
 * multi-gigabyte video never has to hold the entire file in memory.
 */
class FileHasher(private val context: Context) {
    /**
    * Returns the SHA-256 of the file's content as a hex string, or null
    * if the file can't be read (deleted mid-scan, permission lost, etc.).
    * This is blocking I/O, so call it from Dispatchers.IO.
    */
    // TODO: Hashing reads every byte of every candidate. For large videos, hash
    //  only the first 64 KB first and do a full hash only when those match.
    fun sha256(uri: Uri): String? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val digest = MessageDigest.getInstance("SHA-256")
            input.use { stream ->
                val buffer = ByteArray(BUFFER_SIZE_BYTES)
                var bytesRead = stream.read(buffer)
                while (bytesRead != -1) {
                    digest.update(buffer, 0, bytesRead)
                    bytesRead = stream.read(buffer)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: IOException) {
            null
        } catch (e: SecurityException) {
            null
        }
    }

    private companion object {
        const val BUFFER_SIZE_BYTES = 64 * 1024
    }
}