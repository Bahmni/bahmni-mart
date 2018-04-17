package org.bahmni.mart;

import org.springframework.test.context.ActiveProfilesResolver;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;

public class SystemPropertyActiveProfileResolver implements ActiveProfilesResolver {

    private final DefaultActiveProfilesResolver defaultActiveProfilesResolver = new DefaultActiveProfilesResolver();

    @Override
    public String[] resolve(Class<?> testClass) {

        return System.getProperties().containsKey("spring.profiles.active") ?
                System.getProperty("spring.profiles.active").split("\\s*,\\s*") :
                defaultActiveProfilesResolver.resolve(testClass);
    }
}