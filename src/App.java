import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.openqa.selenium.remote.DesiredCapabilities;

import io.appium.java_client.touch.offset.PointOption;
import io.appium.java_client.*;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.clipboard.ClipboardContentType;
import io.appium.java_client.remote.MobileCapabilityType;


public class App {
    public static void main(String[] args) throws Exception {

        DesiredCapabilities dc = new DesiredCapabilities();

        dc.setCapability(MobileCapabilityType.DEVICE_NAME, "emulator-5554");
        dc.setCapability("platformName", "android");
        dc.setCapability("appPackage", "com.samesystem.checkin.debug");
        dc.setCapability("appActivity", "leakcanary.internal.activity.LeakLauncherActivity");
        dc.setCapability("noReset", true);

        AndroidDriver<AndroidElement> driver = new AndroidDriver<AndroidElement>(new URL("http://127.0.0.1:4723/wd/hub"),dc);
        
        driver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
        

        MobileElement distinctHeaderText = (MobileElement) driver.findElementByXPath("/hierarchy/android.widget.FrameLayout/android.view.ViewGroup/android.widget.FrameLayout[1]/android.view.ViewGroup/android.widget.TextView");
        String distinctHeaderTextVlue = distinctHeaderText.getText();
        String distinctsCountTextValue = distinctHeaderTextVlue.replace(" Distinct Leaks", "");
        Integer distinctCount = Integer.parseInt(distinctsCountTextValue);
        System.out.println(distinctCount);

        for(int mainLeakCount = 1;mainLeakCount<distinctCount+1;mainLeakCount++){
            
            String mainLeakNameXpath =       String.format("/hierarchy/android.widget.FrameLayout/android.view.ViewGroup/android.widget.FrameLayout[2]/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.ListView/android.widget.FrameLayout[%s]/android.widget.RelativeLayout/android.widget.TextView[2]", mainLeakCount) ;
            String mainLeakOcurrencesCount = String.format("/hierarchy/android.widget.FrameLayout/android.view.ViewGroup/android.widget.FrameLayout[2]/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.ListView/android.widget.FrameLayout[%s]/android.widget.RelativeLayout/android.widget.TextView[1]", mainLeakCount);
            
            MobileElement mainLeakNameTextView = (MobileElement) driver.findElementByXPath(mainLeakNameXpath);
            MobileElement mainLeakOcurrencesTextView = (MobileElement) driver.findElementByXPath(mainLeakOcurrencesCount);
            
            JsonObject mainLeakJsonObject = new JsonObject();
            mainLeakJsonObject.addProperty("LeakName", mainLeakNameTextView.getText().toString());
            mainLeakJsonObject.addProperty("count", mainLeakOcurrencesTextView.getText().toString());
            
            Integer mainLeakOcurrencesCountValue = Integer.parseInt(mainLeakOcurrencesTextView.getText()); 

            mainLeakNameTextView.click();
            
            ArrayList<JsonObject> ocurrencesList = new ArrayList<>();
            if(mainLeakOcurrencesCountValue>1){

                for(int leaksOcurrencesCount = 1;leaksOcurrencesCount<=mainLeakOcurrencesCountValue;leaksOcurrencesCount++){
                    
                    MobileElement headerSpinner = (MobileElement) driver.findElementByXPath("/hierarchy/android.widget.FrameLayout/android.view.ViewGroup/android.widget.FrameLayout[2]/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.Spinner");
                    headerSpinner.click();
                    
                    String leakOcurrenceTextViewXpath = String.format("/hierarchy/android.widget.FrameLayout/android.widget.FrameLayout/android.widget.ListView/android.widget.LinearLayout[%s]/android.widget.TextView[1]", leaksOcurrencesCount);
                    MobileElement leakOcurrenceTextViewItem = (MobileElement) driver.findElementByXPath(leakOcurrenceTextViewXpath);
                    String leakOccurenciesTextValue = leakOcurrenceTextViewItem.getText().toString();
                    leakOcurrenceTextViewItem.click();

                    driver.performTouchAction(new io.appium.java_client.TouchAction<>(driver).press(PointOption.point(411, 548)).release());
    
                    MobileElement copyImageView = (MobileElement) driver.findElementByXPath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.ScrollView/android.widget.ListView/android.widget.LinearLayout[1]/android.widget.RelativeLayout/android.widget.LinearLayout/android.widget.ImageView");
                    copyImageView.click();    
                    
                    driver.getClipboard(ClipboardContentType.PLAINTEXT); 

                    JsonObject occurencyJsonObect = new JsonObject();
                    occurencyJsonObect.addProperty("className",leakOccurenciesTextValue);
                    occurencyJsonObect.addProperty("description",driver.getClipboardText());

                    ocurrencesList.add(occurencyJsonObect);
                }
    
            }
            else {
                Thread.sleep(200);
                driver.performTouchAction(new io.appium.java_client.TouchAction<>(driver).press(PointOption.point(411, 548)).release());
    
                MobileElement leakOcurrenceTextViewItem = (MobileElement) driver.findElementByXPath("/hierarchy/android.widget.FrameLayout/android.view.ViewGroup/android.widget.FrameLayout[2]/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.Spinner/android.widget.LinearLayout/android.widget.TextView[1]");

                MobileElement copyImageView = (MobileElement) driver.findElementByXPath("/hierarchy/android.widget.FrameLayout/android.widget.LinearLayout/android.widget.FrameLayout/android.widget.ScrollView/android.widget.ListView/android.widget.LinearLayout[1]/android.widget.RelativeLayout/android.widget.LinearLayout/android.widget.ImageView");
                copyImageView.click();  
                
                driver.getClipboard(ClipboardContentType.PLAINTEXT); 

                JsonObject occurencyJsonObect = new JsonObject();
                occurencyJsonObect.addProperty("className",leakOcurrenceTextViewItem.getText().toString());
                occurencyJsonObect.addProperty("description",driver.getClipboardText().toString());
                
                
                ocurrencesList.add(occurencyJsonObect);

            }
            
            Gson gson = new Gson();
            JsonElement element = gson.toJsonTree(ocurrencesList, new TypeToken<ArrayList<JsonObject>>() {}.getType());
            

            mainLeakJsonObject.add("occurencies", element);

            System.out.print(mainLeakJsonObject);

            MobileElement navigationUpButton = (MobileElement) driver.findElementByAccessibilityId("Navigate up");
            navigationUpButton.click();
        }

    }
}
