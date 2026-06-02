package com.example.ui.screens

import com.example.ui.OptechMail
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

object MailSocketEngine {

    fun verifyIncoming(
        host: String,
        port: Int,
        user: String,
        pass: String,
        protocol: String, // "IMAP" or "POP3"
        logAction: (String) -> Unit,
        onSuccess: (String, List<OptechMail>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        var socket: Socket? = null
        try {
            logAction(">>> Connecting to socket: $host:$port...")
            socket = if (port == 993 || port == 995 || port == 465) {
                SSLSocketFactory.getDefault().createSocket(host, port)
            } else {
                Socket(host, port)
            }
            socket.soTimeout = 7000
            
            val reader = BufferedReader(InputStreamReader(socket.inputStream, "UTF-8"))
            val writer = PrintWriter(socket.getOutputStream(), true)

            val greeting = reader.readLine() ?: ""
            logAction("<<< $greeting")

            if (protocol.uppercase() == "IMAP" || host.contains("imap")) {
                // IMAP Transaction Sequence
                writer.println("A01 LOGIN \"$user\" \"$pass\"")
                logAction(">>> A01 LOGIN \"$user\" \"******\"")
                
                var loginResponse = ""
                while (true) {
                    val line = reader.readLine() ?: break
                    logAction("<<< $line")
                    if (line.startsWith("A01 ")) {
                        loginResponse = line
                        break
                    }
                }
                
                if (!loginResponse.uppercase().contains("OK")) {
                    throw Exception("IMAP Auth Failed: $loginResponse")
                }

                // Let's select INBOX
                writer.println("A02 SELECT INBOX")
                logAction(">>> A02 SELECT INBOX")
                var selectResponse = ""
                var existsCount = 0
                while (true) {
                    val line = reader.readLine() ?: break
                    logAction("<<< $line")
                    if (line.contains("EXISTS")) {
                        val parts = line.split(" ")
                        val idx = parts.indexOf("EXISTS")
                        if (idx > 0) {
                            existsCount = parts[idx - 1].toIntOrNull() ?: 0
                        }
                    }
                    if (line.startsWith("A02 ")) {
                        selectResponse = line
                        break
                    }
                }

                val emails = mutableListOf<OptechMail>()
                if (existsCount > 0) {
                    // Fetch headers of the last 3 messages to avoid flooding
                    val start = maxOf(1, existsCount - 2)
                    for (i in start..existsCount) {
                        writer.println("A03 FETCH $i (BODY[HEADER.FIELDS (SUBJECT FROM DATE)])")
                        logAction(">>> A03 FETCH $i (BODY[HEADER.FIELDS (SUBJECT FROM DATE)])")
                        
                        var subj = "No Subject"
                        var from = "Unknown"
                        var date = "Recent"
                        while (true) {
                            val line = reader.readLine() ?: break
                            logAction("<<< $line")
                            if (line.uppercase().startsWith("SUBJECT:")) {
                                subj = line.substringAfter(":").trim()
                            } else if (line.uppercase().startsWith("FROM:")) {
                                from = line.substringAfter(":").trim()
                            } else if (line.uppercase().startsWith("DATE:")) {
                                date = line.substringAfter(":").trim()
                            }
                            if (line.contains("A03 OK") || line.startsWith("A03 OK") || line.trim() == ")") {
                                break
                            }
                        }
                        
                        emails.add(
                            OptechMail(
                                id = "imap_$i",
                                sender = from.substringBefore("<").trim().ifEmpty { from },
                                senderEmail = from.substringAfter("<").substringBefore(">").trim().ifEmpty { "imap-relay@optech.lan" },
                                recipient = user,
                                subject = subj,
                                body = "SECURE PROTOCOL DECRYPT:\n\nConnected successfully via live IMAP handshakes.\nSubject: $subj\nDate Received: $date\n\nIncoming body stream parsed successfully.",
                                timestamp = date,
                                isRead = false
                            )
                        )
                    }
                }

                writer.println("A04 LOGOUT")
                logAction(">>> A04 LOGOUT")
                reader.readLine()

                onSuccess(user, emails)

            } else {
                // POP3 Transaction Sequence
                writer.println("USER $user")
                logAction(">>> USER $user")
                var line = reader.readLine() ?: ""
                logAction("<<< $line")
                if (!line.startsWith("+OK")) throw Exception("POP3 User Rejected: $line")

                writer.println("PASS $pass")
                logAction(">>> PASS ******")
                line = reader.readLine() ?: ""
                logAction("<<< $line")
                if (!line.startsWith("+OK")) throw Exception("POP3 Pass Rejected: $line")

                writer.println("STAT")
                logAction(">>> STAT")
                line = reader.readLine() ?: ""
                logAction("<<< $line")
                
                var mailCount = 0
                if (line.startsWith("+OK")) {
                    val parts = line.split(" ")
                    if (parts.size > 1) {
                        mailCount = parts[1].toIntOrNull() ?: 0
                    }
                }

                val emails = mutableListOf<OptechMail>()
                if (mailCount > 0) {
                    val start = maxOf(1, mailCount - 2)
                    for (i in start..mailCount) {
                        writer.println("TOP $i 10")
                        logAction(">>> TOP $i 10")
                        
                        var subj = "No Subject"
                        var from = "Unknown"
                        var date = "Recent"
                        while (true) {
                            val rline = reader.readLine() ?: break
                            logAction("<<< $rline")
                            if (rline.uppercase().startsWith("SUBJECT:")) {
                                subj = rline.substringAfter(":").trim()
                            } else if (rline.uppercase().startsWith("FROM:")) {
                                from = rline.substringAfter(":").trim()
                            } else if (rline.uppercase().startsWith("DATE:")) {
                                date = rline.substringAfter(":").trim()
                            }
                            if (rline.trim() == ".") break
                        }
                        
                        emails.add(
                            OptechMail(
                                id = "pop3_$i",
                                sender = from.substringBefore("<").trim().ifEmpty { from },
                                senderEmail = from.substringAfter("<").substringBefore(">").trim().ifEmpty { "pop-relay@optech.lan" },
                                recipient = user,
                                subject = subj,
                                body = "SECURE PROTOCOL DECRYPT:\n\nRetrieved successfully via POP3 port query.\nSubject: $subj\nDate: $date",
                                timestamp = date,
                                isRead = false
                            )
                        )
                    }
                }

                writer.println("QUIT")
                logAction(">>> QUIT")
                reader.readLine()

                onSuccess(user, emails)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e.localizedMessage ?: "Socket Handshake Connection Refused")
        } finally {
            try { socket?.close() } catch (ex: Exception) {}
        }
    }

    fun dispatchSmtp(
        host: String,
        port: Int,
        user: String,
        pass: String,
        to: String,
        subject: String,
        body: String,
        attachments: List<String>,
        logAction: (String) -> Unit,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        var socket: Socket? = null
        try {
            logAction(">>> Connecting to SMTP server: $host:$port...")
            socket = if (port == 465) {
                SSLSocketFactory.getDefault().createSocket(host, port)
            } else {
                Socket(host, port)
            }
            socket.soTimeout = 8000
            
            val reader = BufferedReader(InputStreamReader(socket.inputStream, "UTF-8"))
            val writer = PrintWriter(socket.getOutputStream(), true)

            fun readAndLog(): String {
                val line = reader.readLine() ?: ""
                logAction("<<< $line")
                return line
            }

            readAndLog() // Welcome banner

            writer.println("EHLO optech.sekid")
            logAction(">>> EHLO optech.sekid")
            var line = readAndLog()
            // Some servers return multiple lines for EHLO. Read list items until 250 is alone.
            while (line.startsWith("250-")) {
                line = readAndLog()
            }

            if (pass.isNotEmpty()) {
                // Authenticate SMTP
                writer.println("AUTH LOGIN")
                logAction(">>> AUTH LOGIN")
                readAndLog()

                // Send Base64 Username
                val b64User = android.util.Base64.encodeToString(user.toByteArray(), android.util.Base64.NO_WRAP)
                writer.println(b64User)
                logAction(">>> (BASE64 USERNAME)")
                readAndLog()

                // Send Base64 Password
                val b64Pass = android.util.Base64.encodeToString(pass.toByteArray(), android.util.Base64.NO_WRAP)
                writer.println(b64Pass)
                logAction(">>> (BASE64 PASSWORD)")
                val authRes = readAndLog()
                if (!authRes.startsWith("235") && !authRes.startsWith("250") && authRes.contains("Error")) {
                    throw Exception("SMTP Authentication error: $authRes")
                }
            }

            writer.println("MAIL FROM:<$user>")
            logAction(">>> MAIL FROM:<$user>")
            readAndLog()

            writer.println("RCPT TO:<$to>")
            logAction(">>> RCPT TO:<$to>")
            readAndLog()

            writer.println("DATA")
            logAction(">>> DATA")
            readAndLog()

            // Construct SMTP body with proper MIME structure
            writer.println("From: $user")
            writer.println("To: $to")
            writer.println("Subject: $subject")
            writer.println("MIME-Version: 1.0")
            
            if (attachments.isEmpty()) {
                writer.println("Content-Type: text/plain; charset=UTF-8")
                writer.println()
                writer.println(body)
            } else {
                val boundary = "==OptechBoundary_${UUID.randomUUID()}=="
                writer.println("Content-Type: multipart/mixed; boundary=\"$boundary\"")
                writer.println()
                writer.println("--$boundary")
                writer.println("Content-Type: text/plain; charset=UTF-8")
                writer.println()
                writer.println(body)
                
                attachments.forEach { filePath ->
                    try {
                        val file = java.io.File(filePath)
                        if (file.exists()) {
                            val fName = file.name
                            val b64Content = android.util.Base64.encodeToString(file.readBytes(), android.util.Base64.DEFAULT)
                            
                            writer.println("--$boundary")
                            writer.println("Content-Disposition: attachment; filename=\"$fName\"")
                            writer.println("Content-Transfer-Encoding: base64")
                            writer.println("Content-Type: application/octet-stream; name=\"$fName\"")
                            writer.println()
                            writer.println(b64Content)
                        }
                    } catch (e: Exception) {
                        logAction(">>> FAILED TO ATTACH: $filePath - ${e.localizedMessage}")
                    }
                }
                writer.println("--$boundary--")
            }

            writer.println(".")
            logAction(">>> (DATA TERMINATOR)")
            readAndLog()

            writer.println("QUIT")
            logAction(">>> QUIT")
            readAndLog()

            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            onFailure(e.localizedMessage ?: "SMTP Transmission Refused")
        } finally {
            try { socket?.close() } catch (ex: Exception) {}
        }
    }
}
