package burp;

import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.UUID;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.*; 

public class BurpExtender implements IBurpExtender, IHttpListener, IExtensionStateListener
{
    private IBurpExtenderCallbacks callbacks;
    private PrintWriter stdout;
    private IExtensionHelpers helpers;
    private Random random = new Random();
    private AWSCredentials credentials;
    private AmazonEC2 ec2;
    private Region usWest2;

    private String currUserAgent = "";
    private String[] userAgentHeaderList = {
        "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:40.0) Gecko/20100101 Firefox/40.1",
        "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10; rv:33.0) Gecko/20100101 Firefox/33.0",
        "Mozilla/5.0 (X11; Linux x86_64; rv:17.0) Gecko/20121202 Firefox/17.0 Iceweasel/17.0.1",
        "Mozilla/5.0 (X11; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0.1 Iceweasel/15.0.1",
        "Mozilla/5.0 (X11; Linux i686; rv:15.0) Gecko/20100101 Firefox/15.0.1 Iceweasel/15.0.1",
        "Mozilla/5.0 (X11; Linux x86_64; rv:15.0) Gecko/20120724 Debian Iceweasel/15.0",
        "Mozilla/5.0 (X11; Linux x86_64; rv:15.0) Gecko/20100101 Firefox/15.0 Iceweasel/15.0",
        "Mozilla/5.0 (X11; Linux x86_64; rv:12.0) Gecko/20120721 Debian Iceweasel/15.0",
        "Mozilla/5.0 (X11; Linux i686; rv:15.0) Gecko/20100101 Firefox/15.0 Iceweasel/15.0",
        "Mozilla/5.0 (X11; debian; Linux x86_64; rv:15.0) Gecko/20100101 Iceweasel/15.0",
        "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko",
        "Mozilla/5.0 (compatible, MSIE 11, Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko",
        "Mozilla/5.0 (compatible; MSIE 10.6; Windows NT 6.1; Trident/5.0; InfoPath.2; SLCC1; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729; .NET CLR 2.0.50727) 3gpp-gba UNTRUSTED/1.0",
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 7.0; InfoPath.3; .NET CLR 3.1.40767; Trident/6.0; en-IN)",
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)",
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/6.0)",
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)",
        "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/4.0; InfoPath.2; SV1; .NET CLR 2.0.50727; WOW64)",
        "Mozilla/5.0 (compatible; MSIE 10.0; Macintosh; Intel Mac OS X 10_7_3; Trident/6.0)",
        "Mozilla/4.0 (Compatible; MSIE 8.0; Windows NT 5.2; Trident/6.0)",
        "Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)",
        "Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)",
        "Opera/9.80 (X11; Linux i686; Ubuntu/14.10) Presto/2.12.388 Version/12.16",
        "Opera/9.80 (Windows NT 6.0) Presto/2.12.388 Version/12.14",
        "Mozilla/5.0 (Windows NT 6.0; rv:2.0) Gecko/20100101 Firefox/4.0 Opera 12.14",
        "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.0) Opera 12.14",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A",
        "Mozilla/5.0 (iPad; CPU OS 6_0 like Mac OS X) AppleWebKit/536.26 (KHTML, like Gecko) Version/6.0 Mobile/10A5355d Safari/8536.25",
        "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36",
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36"
    };

    private int currProxy = -1;
    private String[] instanceIDList = {
        "i-024028e4c7b7c2f77",
        "i-0389ac51ce9a5e7e1",
        "i-04ffd6175d8e33400",
        "i-0534782e98920fef0",
        "i-070eab3b75ac8f037",
        "i-072ca25cd846a47cd",
        "i-0c804cd8f319e1989"
    };
    
    //
    // implement IBurpExtender
    //
    
    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
    {
        // keep a reference to our callbacks object
        this.callbacks = callbacks;
        
        // set our extension name
        callbacks.setExtensionName("CAPTCHA Scraper");
        
        // obtain our output stream
        stdout = new PrintWriter(callbacks.getStdout(), true);
        
        // register ourselves as an HTTP listener
        callbacks.registerHttpListener(this);
        
        // register ourselves as an extension state listener
        callbacks.registerExtensionStateListener(this);
        helpers = callbacks.getHelpers();

        credentials = new ProfileCredentialsProvider().getCredentials();
        ec2 = new AmazonEC2Client(credentials);
        usWest2 = Region.getRegion(Regions.US_EAST_2);
        ec2.setRegion(usWest2);

        /**
        try
        {
            PrintWriter out = new PrintWriter("/Users/jcallahan/Desktop/captcha/burpconfig.json");
            String config = callbacks.saveConfigAsJson();
            out.println(config);
            out.flush();
            out.close();
        }
        catch (java.io.FileNotFoundException fnfe) {}
        **/
    }

    //
    // implement IHttpListener
    //

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
    {
        if (!messageIsRequest)
        {
            byte[] rsp = messageInfo.getResponse();
            IResponseInfo rspInfo = helpers.analyzeResponse(rsp);
            List<String> headers = rspInfo.getHeaders();
            String contentType = "";

            for (String s : headers) 
            {
                if (s.indexOf("Content-Type") == 0 && s.indexOf("audio/mp3") > 0)
                {    
                    int offset = rspInfo.getBodyOffset();
                    File file;
                    String id;
                    do
                    {
                        id = UUID.randomUUID().toString();
                        file = new File("/Users/jcallahan/Desktop/captcha/mp3/" + id + ".mp3");
                    } while (file.exists());
                    
                    try 
                    {
                        FileOutputStream stream = new FileOutputStream("/Users/jcallahan/Desktop/captcha/mp3/" + id + ".mp3");
                        stream.write(Arrays.copyOfRange(rsp, offset, rsp.length));
                        stream.close();
                        stdout.println("CAPTCHA harvested");
                    } 
                    catch (Exception e) 
                    {
                        stdout.println("something fucked up");
                    }

                    restartEC2(instanceIDList[currProxy]);
                    updateUpstreamProxy();
                }
            }
        }
        else
        {
            byte[] req = messageInfo.getRequest();
            //IRequestInfo reqInfo = helpers.analyzeRequest(req);
            IRequestInfo reqInfo = helpers.analyzeRequest(messageInfo);
            List<String> headers = reqInfo.getHeaders();
            String contentType = "";

            if (reqInfo.getUrl().toString().indexOf("192.168.115.1:5000") >= 0)
            {
                int i = random.nextInt(userAgentHeaderList.length);
                currUserAgent = userAgentHeaderList[i];
                stdout.println("switching to user-agent: " + currUserAgent);
            }

            int i = 0;
            for (String s : headers)
            {
                if (s.indexOf("User-Agent") >= 0)
                {
                    headers.set(i, "User-Agent: " + currUserAgent);
                }
                i++;
            }

            int offset = reqInfo.getBodyOffset();
            byte[] msgBody = Arrays.copyOfRange(req, offset, req.length);
            byte[] newRequest = helpers.buildHttpMessage(headers, msgBody);
            messageInfo.setRequest(newRequest);
        }
    }

    private void updateUpstreamProxy()
    {
        if (currProxy == instanceIDList.length)
            currProxy = 0;
        else
            currProxy++;
        
        String proxy = getPublicIp(instanceIDList[currProxy]);
        String config = "{\"project_options\" : {\"connections\" : {\"upstream_proxy\" : {\"servers\" : [{\"destination_host\" : \"*.com\", \"enabled\" : true, \"proxy_host\" : \"" + proxy + "\", \"proxy_port\" : 8080}]}}}}";
        stdout.println("switching to instance: " + instanceIDList[currProxy]);
        stdout.println("public IP: " + proxy);
        this.callbacks.loadConfigFromJson(config);
    }

    private void restartEC2(String instanceID)
    {
        stdout.println("restarting instance: " + instanceID);
        StopInstancesRequest stopReq = new StopInstancesRequest();
        stopReq.withInstanceIds(instanceID);
         ec2.stopInstances(stopReq);

        DescribeInstancesRequest statusReq;
        DescribeInstancesResult statusRes;
        InstanceState state;
        int status;

        do
        {
            statusReq = new DescribeInstancesRequest();
            statusReq.withInstanceIds(instanceID);
            statusRes = ec2.describeInstances(statusReq);
            status = statusRes.getReservations().get(0).getInstances().get(0).getState().getCode();
            
            try
            {
                java.lang.Thread.sleep(2000);
            }
            catch (InterruptedException ie) {}
        } while (status != 80);

        StartInstancesRequest startReq = new StartInstancesRequest();
        startReq.withInstanceIds(instanceID);
        ec2.startInstances(startReq);
    }

    private String getPublicIp(String instanceID)
    {
        String[] instanceIDs = {instanceID};
        return getPublicIPs(instanceIDs).get(0);
    }
    
    private List<String> getPublicIPs(String[] instanceIDs){
        List<String> publicIpsList = new LinkedList<String>();
        List<String> instanceList = new ArrayList<String>();
        for (String instanceID : instanceIDs)
            instanceList.add(instanceID);

        DescribeInstancesRequest request =  new DescribeInstancesRequest();
        request.setInstanceIds(instanceList);

        DescribeInstancesResult result = ec2.describeInstances(request);
        List<Reservation> reservations = result.getReservations();

        List<Instance> instances;
        for(Reservation res : reservations){
            instances = res.getInstances();
            for(Instance ins : instances){
                publicIpsList.add(ins.getPublicIpAddress());
            }
        }

        return publicIpsList;
    }

    //
    // implement IExtensionStateListener
    //

    @Override
    public void extensionUnloaded()
    {
        stdout.println("Extension was unloaded");
    }
}