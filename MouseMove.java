import java.awt.Robot;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadLocalRandom;
import java.time.Duration;
import java.time.Instant;

public class MouseMove
{
    public static void main(String[] args) throws AWTException, InterruptedException, IOException
    {
        Robot robot = new Robot();
        Point pt;
        double x, y;
        File folder;
        File[] listOfFiles;
        String config;
        java.lang.Runtime rt;
        java.lang.Process p;
        java.io.InputStream is;
        java.io.BufferedReader reader;

        TimeUnit.SECONDS.sleep(5);

        while (true)
        {
            // vm reload = 1339 37
            // vm checkbox = 28 162
            // vm pc settings
            //      open = 497 211
            //      settings = 19 12
            //      close = 66 145
            // ff reload = 977 81
            // ff checkbox = 376 186
            /**
            pt = MouseInfo.getPointerInfo().getLocation();
            x = pt.getX();
            y = pt.getY();
            System.out.println(x + " " + y);
            **/

            updateCAPTCHACount();

            //TimeUnit.SECONDS.sleep(5);

            java.awt.Toolkit.getDefaultToolkit().beep();
            TimeUnit.SECONDS.sleep(5);

            robot.keyPress(KeyEvent.VK_ENTER);
            robot.delay(200);
            robot.keyRelease(KeyEvent.VK_ENTER);

            closeLicenseWindow(robot);
            
            //moveMouseToPosition(1339, 37, robot);
            robot.delay(500);
            robot.keyPress(KeyEvent.VK_F5);
            robot.delay(200);
            robot.keyRelease(KeyEvent.VK_F5);
            //robot.keyPress(KeyEvent.VK_SHIFT);
            //leftClick(robot);
            //robot.keyRelease(KeyEvent.VK_SHIFT);
            for (int i = 0; i < 5; i++)
            {
                TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(0,3));
                moveMouseToPosition(ThreadLocalRandom.current().nextInt(0,1300), ThreadLocalRandom.current().nextInt(20,800), robot);
            }

            moveMouseToPosition(28, 162, robot);
            leftClick(robot);
            //TimeUnit.SECONDS.sleep(ThreadLocalRandom.current().nextInt(60, 240));
            //TimeUnit.SECONDS.sleep(5);
        }
    }

    private static void updateCAPTCHACount()
    {
        File folder = new File("mp3/");
        System.out.println("CAPTCHAs harvested: " + folder.listFiles().length);
    }

    private static void leftClick(Robot robot)
    {
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(200);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(200);
    }

    private static void closeLicenseWindow(Robot robot) throws InterruptedException
    {
        moveMouseToPosition(497, 211, robot);
        leftClick(robot);
        TimeUnit.SECONDS.sleep(3);
        moveMouseToPosition(40, 0, robot);
        TimeUnit.SECONDS.sleep(1);
        moveMouseToPosition(23, 11, robot);
        leftClick(robot);
        TimeUnit.SECONDS.sleep(1);
        moveMouseToPosition(66, 145, robot);
        leftClick(robot);
    }
    
    private static void moveMouseToPosition(int targetX, int targetY, Robot robot)
    {
        float normalizationFactor;
        int currX = (int) MouseInfo.getPointerInfo().getLocation().getX();
        int currY = (int) MouseInfo.getPointerInfo().getLocation().getY();
        int maxDelta = Math.max(Math.abs(targetX - currX), Math.abs(targetY - currY));
        boolean normalizeX = false;
        boolean normalizeY = false;

        if (Math.abs(targetX - currX) > Math.abs(targetY - currY))
        {
            normalizeY = true;
            normalizationFactor = (float) Math.abs(targetX - currX) / (float) Math.abs(targetY - currY);
        }
        else
        {
            normalizeX = true;
            normalizationFactor = (float) Math.abs(targetY - currY) / (float) Math.abs(targetX - currX);
        }
        
        int newX, newY;
        for (int i = 0; i < maxDelta+1; i++)
        {
            if (normalizeY)
            {
                if ((targetX - currX) > 0)
                    newX = currX + i;
                else
                    newX = currX - i;

                if ((targetY - currY) > 0)
                    newY = currY + Math.round(i / normalizationFactor);
                else
                    newY = currY - Math.round(i / normalizationFactor);
            }
            else
            {
                if ((targetX - currX) > 0)
                    newX = currX + Math.round(i / normalizationFactor);
                else
                    newX = currX - Math.round(i / normalizationFactor);
                
                if ((targetY - currY) > 0)
                    newY = currY + i;
                else
                    newY = currY - i;
            }

            robot.mouseMove(newX, newY);
            robot.delay(2);
        }
    }

    /**private static void rotateVPN(config) throws IOException, InterruptedException
    {
        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
        java.lang.Process p = rt.exec("/usr/local/sbin/openvpn " + config);
        // You can or maybe should wait for the process to complete
        //p.waitFor();
        //System.out.println("Process exited with code = " + rt.exitValue());
        // Get process' output: its InputStream
        java.io.InputStream is = p.getInputStream();
        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is));
        // And print each line
        String s = null;
        while ((s = reader.readLine()).indexOf("Initialization Sequence Completed") < 0) {
            System.out.println(s);
        }
        while (true)
        {
            TimeUnit.SECONDS.sleep(5);
        }
        //is.close();
    }**/
}