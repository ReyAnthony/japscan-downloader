package fr.jordanmarques.japscandownloader.extractor

import fr.jordanmarques.japscandownloader.JAPSCAN_URL
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File
import javax.imageio.ImageIO

@Component
class ChapterExtractor @Autowired constructor(
        private var imageExtractor: ImageExtractor
) {
    private val log = LoggerFactory.getLogger(MangaExtractor::class.java)
    private val currentDirectory = System.getProperty("user.dir")

    fun extract(japscanUrl: String = JAPSCAN_URL, manga: String, chapter: String, prefix: String = "") {
        log.info("Start downloading chapter $prefix$chapter")

        val chapterUrl = "$japscanUrl/$manga/$prefix$chapter"
        val document = Jsoup.connect(chapterUrl).get()
                ?: throw RuntimeException("No Chapter found for url : $chapterUrl")

        createChapterDirectory(manga, chapter, prefix)

        for (i in 1..numberOfScansInChapter(document)) {
            val scanDoc = Jsoup.connect("$chapterUrl/$i.html").get()
                    ?: throw RuntimeException("No Scan found for url : $chapterUrl/$i.html")

            val savePath = "$currentDirectory/$manga/$prefix$chapter/$i.png"
            imageExtractor.extract(scanDoc)?.let {
                ImageIO.write(it, "png", File(savePath))
                log.info(savePath)
            }

        }

    }

    private fun createChapterDirectory(manga: String, chapter: String, prefix: String) {
        File("$currentDirectory/$manga/$prefix$chapter").mkdirs()
    }

    private fun numberOfScansInChapter(document: Document): Int {
        return document.select("option").size
    }

}