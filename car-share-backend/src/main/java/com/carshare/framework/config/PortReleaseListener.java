package com.carshare.framework.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PortReleaseListener implements ApplicationListener<ApplicationEvent>
{
    private static final Logger log = LoggerFactory.getLogger(PortReleaseListener.class);
    private static boolean portChecked = false;

    @Override
    public void onApplicationEvent(ApplicationEvent event)
    {
        if (event instanceof ApplicationPreparedEvent && !portChecked)
        {
            portChecked = true;
            Environment env = ((ApplicationPreparedEvent) event).getApplicationContext().getEnvironment();
            Integer port = env.getProperty("server.port", Integer.class, 8081);
            releasePort(port);
        }
    }

    private void releasePort(int port)
    {
        try
        {
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("win"))
            {
                return;
            }
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "netstat -ano | findstr :" + port + " | findstr LISTENING");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"));
            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null)
            {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 5)
                {
                    String pid = parts[parts.length - 1];
                    if (pid.matches("\\d+"))
                    {
                        found = true;
                        log.warn("检测到端口 {} 被进程 PID:{} 占用，正在自动终止该进程...", port, pid);
                        ProcessBuilder killPb = new ProcessBuilder("cmd.exe", "/c", "taskkill /PID " + pid + " /F");
                        killPb.start();
                        Thread.sleep(2000);
                        log.info("已终止占用端口 {} 的进程 PID:{}", port, pid);
                    }
                }
            }
            process.waitFor();
            if (!found)
            {
                log.info("端口 {} 可用，无需释放", port);
            }
        }
        catch (Exception e)
        {
            log.warn("自动释放端口时出错: {}", e.getMessage());
        }
    }
}
