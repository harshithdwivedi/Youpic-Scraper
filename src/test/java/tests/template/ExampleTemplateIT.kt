package tests.template

import Photographer
import com.opencsv.CSVWriter
import org.junit.jupiter.api.Test
import org.openqa.selenium.By
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import java.io.File
import java.util.concurrent.TimeUnit
import java.io.FileWriter
import java.util.*


class ExampleTemplateIT {

/*
*  See https://docs.skrape.it for further information on how to use Skrape{it}.
*  It's faster than Selenium and doesn't need a browser installation.
*  Testing of User-Journeys is not supported currently but it's the better choice over selenium
*  if you want to check values or appearance of elements or extract data.
*/

    val CSV_FILE_PATH = "C:\\Users\\harsh\\Desktop\\test.csv"

    @Test
    fun `open website and log in`() {

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

        scroll(driver, 300)

        val photographers = getPhotographers(driver)

        System.out.println("Size of array : ${photographers.size}")

        photographers.forEach {

            driver.get(it.link)
            Thread.sleep(3000)

            //Click Follow
            driver.findElement(By.cssSelector(".layout-item > a:nth-child(1)")).click()
            Thread.sleep(1000)

            //Click Message
            Thread.sleep(1000)
            driver.findElement(By.cssSelector(".layout-item > a:nth-child(2)")).click()

            Thread.sleep(3000)

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

            Thread.sleep(2000)

            driver.findElement(By.cssSelector(".btn-primary-outline")).click()
        }

        driver.close()
    }

    @Test
    private fun createCsv() {
        val file = File(CSV_FILE_PATH)
        val outputfile = FileWriter(file)
        val writer = CSVWriter(
            outputfile, ';',
            CSVWriter.NO_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER,
            CSVWriter.DEFAULT_LINE_END
        )
        val header = arrayOf("Name", "Url")
        writer.writeNext(header)

        val data1 = arrayOf("Harshit", "www.google.com")
        writer.writeNext(data1)

        writer.close()
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

        if (photographers.size < 15) return photographersWithUrl

        //Recursive call to scroll more
        scroll(driver, 300)
        getPhotographers(driver)
        return photographersWithUrl
    }

    private fun scroll(driver: ChromeDriver, amount: Int) {
        driver.executeScript("window.scrollBy(0,$amount);")
        Thread.sleep(1000)
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
}

private fun ChromeDriver.wait(i: Long) {
    this.manage().timeouts().implicitlyWait(i, TimeUnit.SECONDS)
}
