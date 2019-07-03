import com.opencsv.CSVWriter
import com.opencsv.bean.CsvToBeanBuilder
import org.openqa.selenium.By
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File
import java.io.FileWriter
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

val CSV_FILE_PATH = "C:\\Users\\harsh\\Desktop\\IdeaProjects\\YouPic-Automessenger\\photographers.csv"

val writer by lazy {
    CSVWriter(
        outputfile, ';',
        CSVWriter.NO_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
        CSVWriter.DEFAULT_LINE_END
    )
}

val file = File(CSV_FILE_PATH)
val outputfile = FileWriter(file)

val reader by lazy {
    Files.newBufferedReader(Paths.get(CSV_FILE_PATH))
}

val csvToBean by lazy {
    CsvToBeanBuilder<Photographer>(reader)
        .withType(Photographer::class.java)
        .withIgnoreLeadingWhiteSpace(true)
        .build()
}

val alreadyScannedPhotographers = mutableListOf<Photographer>()

fun main() {
//    createCsv()
    alreadyScannedPhotographers.clear()
    alreadyScannedPhotographers.addAll(csvToBean.parse())
    openWebsiteAndLogIn()
}

fun openWebsiteAndLogIn() {
    System.setProperty("webdriver.chrome.driver", "C:\\chromedriver.exe")

    val options = ChromeOptions()
    options.addArguments("--disable-extensiorns")
    options.addArguments("--incognito")
    options.addArguments("enable-automation")
    options.addArguments("--no-sandbox")
    options.addArguments("--window-size=1920,1080")
    options.addArguments("--disable-gpu")
    options.addArguments(
        "--disable-extensions"
    )
    options.addArguments("--dns-prefetch-disable")
    options.setPageLoadStrategy(PageLoadStrategy.NORMAL)
    val driver = ChromeDriver(options)

    signIn(driver)

    Thread.sleep(1500)

    driver.get("https://youpic.com/photographers")

//        setWedding(driver)
    setLocation(driver, "Paris")
    search(driver)

    scroll(driver, 400)
    Thread.sleep(1000)

    val photographers = getPhotographers(driver)

    println("Size of array : ${photographers.size}")

    photographers
        .sortedWith(compareBy { it.name })
        .forEachIndexed { index, it ->

            //            if (index > 200) {

            //Write photographers to csv
            val photographer = arrayOf(it.name, it.link)
            writePhotographer(photographer)

            driver.get(it.link)
            Thread.sleep(1500)

            //Click Follow
            try {
                val element = driver.findElement(By.cssSelector(".layout-item > a:nth-child(1)"))
                if (element.text == "Following") {

                } else {
                    element.click()
                    //Click Message
                    Thread.sleep(400)
                    driver.findElement(By.cssSelector(".layout-item > a:nth-child(2)")).click()

                    Thread.sleep(800)

                    try {
                        driver.findElement(By.cssSelector(".input-lg")).sendKeys(
                            """
Hi ${it.name.split(" ").first()}, hope you're doing good!

I am a student at MIT BootCamps and being a hobbyist photographer myself; I was doing a survey to study the time spent by photographers in culling and organizing their photos.
I was wondering if you would be able to devote a few minutes of your time to answer some questions, your experience and expertise will surely help us a lot in understanding and potentially improving every photographer's workflow!

All the questions that we need for the study are in the short google form linked below :

https://forms.gle/EijfGvmcW1tBtV6v8

The form is totally anonymous and won't take more than 5 minutes of your time, I’d appreciate if you could take out some time to fill it out.

Thanks!
        """.trimIndent()
                        )
                    } catch (e: WebDriverException) {
                    }

                    Thread.sleep(500)

                    try {
                        driver.findElement(By.cssSelector(".btn-primary-outline")).click()
                    } catch (e: Exception) {

                    }
                    Thread.sleep(500)
                }
            } catch (e: NoSuchElementException) {

            }
//            }
        }
    writer.close()
    driver.close()
}

//private fun createCsv() {
//    writer = CSVWriter(
//        outputfile, ';',
//        CSVWriter.NO_QUOTE_CHARACTER,
//        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
//        CSVWriter.DEFAULT_LINE_END
//    )
//    val header = arrayOf("name", "link")
//    writer.writeNext(header)
////    writer.close()
//}

fun writePhotographer(photographer: Array<String>) {
//    writer = CSVWriter(
//        outputfile, ';',
//        CSVWriter.NO_QUOTE_CHARACTER,
//        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
//        CSVWriter.DEFAULT_LINE_END
//    )

    writer.writeNext(photographer)
//    writer.close()
}

private fun signIn(driver: ChromeDriver) {
    val baseUrl = "https://youpic.com/signin"
    driver.get(baseUrl)

    Thread.sleep(800)

    val userName = "manasbagula@gmail.com"
    val password = "youpic@123"

    driver.findElement(By.name("user")).sendKeys(userName)
    driver.findElement(By.name("password")).sendKeys(password)

    driver.findElement(By.cssSelector("button[type=\"submit\"]")).click()
}

private val photographersWithUrl = hashSetOf<Photographer>()

private fun getPhotographers(driver: ChromeDriver): HashSet<Photographer> {
    val photographers = driver.findElements(By.cssSelector(".link-underline"))

    photographers.forEach { webElement ->
        try {
            val photographer = Photographer(webElement.text, webElement.getAttribute("href"))
            photographersWithUrl.add(photographer)
            //Replace this with the name of last photographer in your seaech results
            if (photographer.name == "Clara Achour") return photographersWithUrl
        } catch (e: Exception) {
            e.printStackTrace()
            print("Size of array ${photographersWithUrl.size}")
        }
    }

    if (photographers.size < 15 || photographersWithUrl.size > 600) return photographersWithUrl

    //Recursive call to scroll more
    scroll(driver, 350)
    try {
        getPhotographers(driver)
    } catch (e: StaleElementReferenceException) {
        e.printStackTrace()
        return photographersWithUrl
    }
    return photographersWithUrl
}

private fun scroll(driver: ChromeDriver, amount: Int) {
    driver.executeScript("window.scrollBy(0,$amount);")
//    Thread.sleep(200)
}

fun search(driver: ChromeDriver) {
    driver.findElement(By.linkText("Search")).click()
    Thread.sleep(3000)
}

fun setWedding(driver: ChromeDriver) {
    driver.findElement(By.className("icon-wedding")).click()
    Thread.sleep(3000)
}

fun setLocation(driver: ChromeDriver, location: String) {
    driver.findElement(By.cssSelector("input[placeholder=\"Region or city\"]")).sendKeys(location)
    Thread.sleep(3000)
}