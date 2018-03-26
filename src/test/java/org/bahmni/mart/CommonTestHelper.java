package org.bahmni.mart;

import java.lang.reflect.Field;

public class CommonTestHelper {

    public static void setValuesForMemberFields(Object classInstance, String fieldName, Object valueForMemberField)
            throws NoSuchFieldException, IllegalAccessException {
        setField(classInstance, valueForMemberField, classInstance.getClass().getDeclaredField(fieldName));
    }

    public static void setValuesForSuperClassMemberFields(Object classInstance, String fieldName,
                  Object valueForMemberField) throws NoSuchFieldException, IllegalAccessException {
        Field field = classInstance.getClass().getSuperclass().getDeclaredField(fieldName);
        setField(classInstance, valueForMemberField, field);
    }

    private static void setField(Object classInstance, Object valueForMemberField, Field field)
            throws IllegalAccessException {
        field.setAccessible(true);
        field.set(classInstance, valueForMemberField);
    }
}
