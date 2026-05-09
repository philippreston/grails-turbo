import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeOptions
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions



def webDriverVersions = [
        'chromedriver': (System.getenv("TESTING_WEBDRIVER_VERSION_CHROME") ?: 'stable'),
        'edgedriver'  : (System.getenv("TESTING_WEBDRIVER_VERSION_EDGE") ?: 'stable'),
        'geckodriver' : (System.getenv("TESTING_WEBDRIVER_VERSION_GECKO") ?: 'stable')
]

waiting {
    timeout = 120
    retryInterval = 1
}

// Override the URL.  Without being set this will default to the local spun up instance of the Application
def overrideUrl = System.getenv('TESTING_BASEURL')
if(overrideUrl) {
    println "Overriding base url: ${overrideUrl}"
    baseUrl = overrideUrl
}

atCheckWaiting = true

// Default when geb.env is unset (Chrome headless works without Edge on macOS/Linux CI)
driver = {
    ChromeOptions o = new ChromeOptions()
    o.browserVersion = webDriverVersions.chromedriver
    o.addArguments('--headless=new')
    o.addArguments('--window-size=1600,900')
    new ChromeDriver(o)
}

environments {

    // run via “./gradlew -Dgeb.env=chrome iT”
    chrome {
        driver = {
            ChromeOptions o = new ChromeOptions()
            o.browserVersion = webDriverVersions.chromedriver
            new ChromeDriver(o)
        }
    }

    // run via “./gradlew -Dgeb.env=chromeHeadless iT”
    chromeHeadless {
        driver = {
            ChromeOptions o = new ChromeOptions()
            o.browserVersion = webDriverVersions.chromedriver
            o.addArguments('--headless=new')
            o.addArguments('--window-size=1600,900')
            new ChromeDriver(o)
        }
    }

    // run via “./gradlew -Dgeb.env=chromeHeadful iT”
    chromeHeadful {
        driver = {
            ChromeOptions o = new ChromeOptions()
            o.browserVersion = webDriverVersions.chromedriver
            new ChromeDriver(o)
        }
    }

    // run via “./gradlew -Dgeb.env=firefox iT”
    firefox {
        driver = {
            FirefoxOptions o = new FirefoxOptions()
            o.browserVersion = webDriverVersions.geckodriver
            new FirefoxDriver(o)
        }
    }

    // run via “./gradlew -Dgeb.env=firefoxHeadless iT”
    firefoxHeadless {
        driver = {
            FirefoxOptions o = new FirefoxOptions()
            o.browserVersion = webDriverVersions.geckodriver
            o.addArguments("-headless")
            new FirefoxDriver(o)
        }
    }

    // run via "./gradlew -Dgeb.env=edge iT"
    edge {
        driver = {
            EdgeOptions o = new EdgeOptions()
            o.browserVersion = webDriverVersions.edgedriver
            o.setAcceptInsecureCerts(true)
            o.addArguments("--disable-web-security")
            new EdgeDriver(o)
        }
    }
}

// Where to save Geb reports (screenshots, HTML dumps)
reportsDir = new File("build/reports/geb")

