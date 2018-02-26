package org.bahmni.analytics;

import java.lang.reflect.Field;

public class CommonTestHelper {

    public static void setValuesForMemberFields(Object classInstance, String fieldName, Object valueForMemberField) throws NoSuchFieldException, IllegalAccessException {
        Field field = classInstance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(classInstance, valueForMemberField);
    }
}
