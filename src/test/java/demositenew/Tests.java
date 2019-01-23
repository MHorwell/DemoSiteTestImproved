package demositenew;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class Tests {

	WebElement element;
	WebDriver driver;
	Actions action;
	static ExtentTest test;
	static ExtentReports report;
	WebDriverWait wait;
	static FileInputStream file = null;
	static XSSFWorkbook workbook;
	static List<User> userList = new ArrayList<User>();
	static XSSFSheet sheet;
	
	

	@BeforeClass
	public static void findFiles() {
		report = new ExtentReports(Constant.REPORTPATH + Constant.REPORTFILE, true);
		try {
			file = new FileInputStream(Constant.EXCELPATH + Constant.EXCELFILE);
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
		try {
			workbook = new XSSFWorkbook(file);
		} catch (IOException e) {
			System.out.println(e);
		}

		sheet = workbook.getSheetAt(0);
		for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
			User user = new User(sheet.getRow(i).getCell(0).getStringCellValue(),
					sheet.getRow(i).getCell(1).getStringCellValue());
			sheet.getRow(i).createCell(2).setCellValue(i);;
			userList.add(user);
		}

	}

	@AfterClass
	public static void endReport() {
		report.flush();
	}

	@Before
	public void setup() {
		System.setProperty("webdriver.gecko.driver", Constant.FIREFOXDRIVERPATH + Constant.FIREFOXDRIVER);
		driver = new FirefoxDriver();
		action = new Actions(driver);
		wait = new WebDriverWait(driver, 10);

	}

	@After
	public void teardown() {
		driver.quit();
	}

	@Test
	public void testingUsers() {

		for (User user : userList) {
			String username = user.getUsername();
			String password = user.getPassword();
			test = report.startTest("Checking " + username + " login");

			driver.manage().window().maximize();
			driver.get("http://thedemosite.co.uk/");

			LandingPage landingPage = PageFactory.initElements(driver, LandingPage.class);
			test.log(LogStatus.INFO, "Landing page loaded.");
			landingPage.goToAddUser();

			AddAUser userPage = PageFactory.initElements(driver, AddAUser.class);
			test.log(LogStatus.INFO, "Add a user page loaded.");
			userPage.createUser(username, password);
			test.log(LogStatus.INFO, "User \"" + username +  "\" created.");
			userPage.goToLogin();

			LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
			loginPage.inputUser(username, password);
			loginPage.validateLogin();
			
			
			XSSFCell resultCell = sheet.getRow(userList.indexOf(user)).getCell(2);
			
			if (loginPage.checkLogin().equals("**Successful Login**")){
				test.log(LogStatus.PASS, username + " logged in successfully");
				resultCell.setCellValue("PASS");
			} else {
				test.log(LogStatus.FAIL, username + " didn't log in successfully");
				resultCell.setCellValue("FAIL");
			}

			assertEquals("**Successful Login**", loginPage.checkLogin());

			report.endTest(test);

		}

	}

}
