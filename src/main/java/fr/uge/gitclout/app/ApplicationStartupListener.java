package fr.uge.gitclout.app;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String serverUrl = "http://localhost:" + event.getApplicationContext().getEnvironment().getProperty("server.port");
        System.out.println("WEB server is up! " + serverUrl);
        System.out.println("OPEN API swagger " + serverUrl + "/swagger-ui");
    }
}