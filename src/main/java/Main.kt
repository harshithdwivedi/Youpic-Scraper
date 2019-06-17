import com.opencsv.CSVWriter
import org.openqa.selenium.By
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File
import java.io.FileWriter
import java.util.HashSet

val CSV_FILE_PATH = "C:\\Users\\harsh\\Desktop\\photographers.csv"
lateinit var writer: CSVWriter
val file = File(CSV_FILE_PATH)
val outputfile = FileWriter(file)

fun main() {
    createCsv()
    openWebsiteAndLogIn()
}

fun openWebsiteAndLogIn() {

    System.setProperty("webdriver.chrome.driver", "C:\\Users\\harsh\\Downloads\\chromedriver.exe")

    val options = ChromeOptions()

    options.addArguments("--disable-extensions")
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
//        setLocation(driver, "India")
//        search(driver)

    scroll(driver, 400)
    Thread.sleep(1000)

    val photographers = getPhotographers(driver)

    System.out.println("Size of array : ${photographers.size}")

    photographers.forEach {

        //Write photographers to csv
        val photographer = arrayOf(it.name, it.link)
        writePhotographer(photographer)

        driver.get(it.link)
        Thread.sleep(1500)

        //Click Follow
        driver.findElement(By.cssSelector(".layout-item > a:nth-child(1)")).click()
        Thread.sleep(1000)

        //Click Message
        Thread.sleep(1000)
        driver.findElement(By.cssSelector(".layout-item > a:nth-child(2)")).click()

        Thread.sleep(1500)

        driver.findElement(By.cssSelector(".input-lg")).sendKeys(
            """
Hi ${it.name}, hope you're doing good!
I am a student at MIT BootCamps and being a hobbyist photographer myself; I was doing a survey to study the time spent by photographers in culling and organizing their photos.
I was wondering if you would be able to devote a few minutes of your time to answer some questions, your experience and expertise will surely help us a lot in understanding and potentially improving every photographer's workflow!

All the questions that we need for the study are in the short google form linked below :

https://forms.gle/EijfGvmcW1tBtV6v8

The form is totally anonymous and won't take more than 5 minutes of your time, I’d appreciate if you could take out some time to fill it out.
Thanks!
        """.trimIndent()
        )

        Thread.sleep(1500)

        driver.findElement(By.cssSelector(".btn-primary-outline")).click()
    }

    writer.close()
    driver.close()
}

private fun createCsv() {
    writer = CSVWriter(
        outputfile, ';',
        CSVWriter.NO_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
        CSVWriter.DEFAULT_LINE_END
    )
    val header = arrayOf("Name", "Url")
    writer.writeNext(header)
//    writer.close()
}

fun writePhotographer(photographer: Array<String>) {
    writer = CSVWriter(
        outputfile, ';',
        CSVWriter.NO_QUOTE_CHARACTER,
        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
        CSVWriter.DEFAULT_LINE_END
    )

    writer.writeNext(photographer)
//    writer.close()
}

private fun signIn(driver: ChromeDriver) {
    val baseUrl = "https://youpic.com/signin"
    driver.get(baseUrl)

    Thread.sleep(800)

    val userName = "harshithdwivedi@gmail.com"
    val password = "youpic@123"

    driver.findElement(By.name("user")).sendKeys(userName)
    driver.findElement(By.name("password")).sendKeys(password)

    driver.findElement(By.cssSelector("button[type=\"submit\"]")).click()
}

private fun deleteCookiesAndLogin(driver: ChromeDriver) {
    driver.manage().deleteAllCookies()

    driver.findElement(By.cssSelector(".layout-item > a:nth-child(1)")).click()
    Thread.sleep(1000)
    driver.findElement(By.linkText("Sign in")).click()
    val userName = "harshithdwivedi@gmail.com"
    val password = "youpic@123"

    driver.findElement(By.name("user")).sendKeys(userName)
    driver.findElement(By.name("password")).sendKeys(password)

    driver.findElement(By.cssSelector("button[type=\"submit\"]")).click()

    Thread.sleep(2000)
}

private val photographersWithUrl = hashSetOf<Photographer>()

private fun getPhotographers(driver: ChromeDriver): HashSet<Photographer> {
    val photographers = driver.findElements(By.cssSelector(".link-underline"))

    photographers.forEach { webElement ->
        photographersWithUrl.add(Photographer(webElement.text, webElement.getAttribute("href")))
    }

    if (photographers.size < 10 || photographersWithUrl.size > 3000) return photographersWithUrl

    //Recursive call to scroll more
    scroll(driver, 300)
    getPhotographers(driver)
    return photographersWithUrl
}

private fun scroll(driver: ChromeDriver, amount: Int) {
    driver.executeScript("window.scrollBy(0,$amount);")
    Thread.sleep(300)
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