package mBankingUtilityCenter;

import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.WebDriver;

import mBankingBasePageObject.BaseObject;
import mBankingPageObjectModel.Configuration;
import mBankingPageObjectModel.StaticStore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
 
/**
 * 
 * @author brantansp
 *
 */
public class ExtentManager{
	
	public static ExtentReports extent;
	public static ExtentTest extentLogger;
	public static String response="";
	public static String transactionID = "";
	static HttpConnect obj=new HttpConnect();
	private static String dbResult[];
	public static WebDriver driver;
	//protected static Log log = LogFactory.getLog(ExtentManager.class);
	protected static Log log = LogFactory.getLog(MethodHandles.lookup().lookupClass().getSimpleName());
	public static Properties prop=getProperty();
	static String reportPath;

	@BeforeSuite
	public void setUp()throws FileNotFoundException{
      	    log.info("Running Mobile banking API Automation testing on mPAY 4.0"+"\r\n");
    		SimpleDateFormat dateFormatter = new SimpleDateFormat("ddMMyyyy"); 
    		SimpleDateFormat timeFormatter = new SimpleDateFormat("HHmmss"); 
    		Date date = new Date();  
        	File dir = new File(System.getProperty("user.dir")+"\\output\\ExtentReport\\"+dateFormatter.format(date));
        	if (!dir.exists())
        	{
        		dir.mkdirs();
        	}
        	reportPath= dir+"\\ExtentReport_"+timeFormatter.format(date)+".html";
			extent = new ExtentReports (reportPath, true);
			extent.loadConfig(new File(System.getProperty("user.dir")+"\\extent-config.xml"));
			prop =getProperty();
	}
	
	@BeforeMethod
	public void beforeMethod(Method method)
	{
		extentLogger = extent.startTest(this.getClass().getSimpleName()+ " ::  " +method.getName(), method.getName()); 
		extentLogger.assignAuthor("Brantan sp");
		extentLogger.assignCategory("Automation Testing");
		extentLogger.log( LogStatus.PASS, "Test started successfully");
	}
	
	
	@AfterMethod
	public void getResult(ITestResult result){
		if(result.getStatus() == ITestResult.FAILURE){
			extentLogger.log(LogStatus.FAIL, "Test Case Failed is "+result.getName());
			extentLogger.log(LogStatus.FAIL, "Test Case Failed is "+result.getThrowable());
		}else if(result.getStatus() == ITestResult.SKIP){
			extentLogger.log(LogStatus.SKIP, "Test Case Skipped is "+result.getName());
		}extent.endTest(extentLogger);
	}
	
	@AfterSuite
	public void endReport(){ 
                extent.flush();
                if(prop.getProperty("openReportInBrowser").equals("Y"))
                {
                	 launchReport();
                }
    } 
	
	public static Properties getProperty()
	{
		prop=ExcelReader.getPropertyFromExcel("Data","InputData");
		return prop;
	}
	
	public static void main(String[] args) {
		launchReport();
	}
	
	public static void launchReport()
	{
		System.out.println("*******************");
		System.out.println("launching Report in browser");
		System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir")+"\\driver\\chromedriver2.33.exe");
		driver = new ChromeDriver();
		System.setProperty("java.net.preferIPv4Stack", "true");
/*		System.setProperty("webdriver.ie.driver", System.getProperty("user.dir")+"\\driver\\IEDriverServer.exe");
		driver = new InternetExplorerDriver();*/
		driver.manage().window().maximize();
		driver.get(reportPath);
		System.out.println("*******************");
	}
	
	public static void assertResponse(String response)
	{
		//log.info(response.substring(2, 4));
		assertTrue(response.substring(2,4).contains("00"));		
	}
	
	public static String sendReq (String Request, String txnType) throws IOException, SQLException
	{
		log.info("******************************START******************************");
	    log.info("Request : " + txnType);
	    BigInteger uniNum = RandomNumGenerator.generate();
	  	if (prop.getProperty("HMAC").equals("Y"))
		{
		  try {
			Request=Hmac.Hmacing(Request+uniNum, Request, uniNum);
			log.info("Hmaced Request : "+Request);
		        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			log.error(e);
		   }
		}
		else {
			Request = Request +";"+uniNum;
			log.info("Non-Hmac request : "+Request);
			log.info(" Request");
		}

			 HttpConnect obj=new HttpConnect();
			response = obj.Post(Request);
			log.info("Response received from Server : "+response);
     	if (response.contains("TXNID"))
			{
				transactionID= response.substring(response.lastIndexOf("TXNID:")+6, response.lastIndexOf("TXNID:")+18);
				log.info("Transaction ID : "+transactionID);
				if(prop.getProperty("dbReport")=="Y")
				{
					dbResult = dbTransactionlog.fetchRecord(transactionID);
					WriteToCSVFile.reportGeneration( dbResult);
				}		
			}
	log.info("******************************END********************************\r\n");
	return response;
	}
	
	public static String sendReqAppLogin (String Request, String req2,String txnType) throws IOException, SQLException
	{
		log.info("******************************START******************************");
	    log.info("Request : " + txnType);
	    BigInteger uniNum = RandomNumGenerator.generate();
	  	if (prop.getProperty("HMAC").equals("Y"))
		{
		  try {
			Request=Hmac.Hmacing(req2+uniNum, Request, uniNum);
			log.info("Hmaced Request : "+Request);
		        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			log.error(e);
		   }
		}
		else {
			Request = Request +";"+uniNum;
			log.info("Non-Hmac request : "+Request);
			log.info(" Request");
		}

			 HttpConnect obj=new HttpConnect();
			response = obj.Post(Request);
			log.info("Response received from Server : "+response);
     	if (response.contains("TXNID"))
			{
				transactionID= response.substring(response.lastIndexOf("TXNID:")+6, response.lastIndexOf("TXNID:")+18);
				log.info("Transaction ID : "+transactionID);
				if(prop.getProperty("dbReport")=="Y")
				{
					dbResult = dbTransactionlog.fetchRecord(transactionID);
					WriteToCSVFile.reportGeneration( dbResult);
				}		
			}
	log.info("******************************END********************************\r\n");
	return response;
	}
}



