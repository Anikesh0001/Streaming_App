package com.livestreamplayer.app.data

import com.livestreamplayer.app.model.Channel
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object M3UParser {

    fun parse(input: InputStream): List<Channel> {
        val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))
        val channels = mutableListOf<Channel>()

        val firstLine = reader.readLine()?.trim() ?: return emptyList()
        if (!firstLine.startsWith("#EXTM3U")) return emptyList()

        var currentName = ""
        var currentLogo = ""
        var currentGroup = "Uncategorized"
        var currentTvgId = ""
        var currentTvgName = ""
        var currentLanguage = ""
        var currentCountry = ""
        var currentUserAgent = ""
        var currentReferrer = ""
        var hasExtInf = false

        reader.forEachLine { rawLine ->
            val line = rawLine.trim()
            when {
                line.startsWith("#EXTINF:") -> {
                    hasExtInf = true
                    currentName = parseDisplayName(line)
                    currentLogo = parseAttribute(line, "tvg-logo")
                    currentGroup = parseAttribute(line, "group-title").ifEmpty { "Uncategorized" }
                    currentTvgId = parseAttribute(line, "tvg-id")
                    currentTvgName = parseAttribute(line, "tvg-name")
                    currentLanguage = parseAttribute(line, "tvg-language")
                    currentCountry = parseAttribute(line, "tvg-country")
                }
                line.startsWith("#EXTVLCOPT:http-user-agent=") -> {
                    currentUserAgent = line.substringAfter("=")
                }
                line.startsWith("#EXTVLCOPT:http-referrer=") -> {
                    currentReferrer = line.substringAfter("=")
                }
                line.startsWith("#KODIPROP:") -> { /* skip kodi properties */ }
                line.startsWith("#") -> { /* skip other directives */ }
                line.isNotBlank() && hasExtInf -> {
                    channels.add(
                        Channel(
                            name = currentName.ifEmpty { "Channel ${channels.size + 1}" },
                            url = line,
                            logoUrl = currentLogo,
                            group = currentGroup,
                            language = currentLanguage,
                            country = currentCountry,
                            tvgId = currentTvgId,
                            tvgName = currentTvgName,
                            userAgent = currentUserAgent,
                            referrer = currentReferrer
                        )
                    )
                    // Reset
                    currentName = ""
                    currentLogo = ""
                    currentGroup = "Uncategorized"
                    currentTvgId = ""
                    currentTvgName = ""
                    currentLanguage = ""
                    currentCountry = ""
                    currentUserAgent = ""
                    currentReferrer = ""
                    hasExtInf = false
                }
            }
        }
        return channels
    }

    fun parse(content: String): List<Channel> = parse(content.byteInputStream())

    private fun parseAttribute(line: String, attr: String): String {
        val pattern = Regex("""$attr="([^"]*?)"""")
        return pattern.find(line)?.groupValues?.getOrNull(1) ?: ""
    }

    private fun parseDisplayName(line: String): String {
        val commaIndex = line.lastIndexOf(',')
        return if (commaIndex >= 0) line.substring(commaIndex + 1).trim() else ""
    }
}
