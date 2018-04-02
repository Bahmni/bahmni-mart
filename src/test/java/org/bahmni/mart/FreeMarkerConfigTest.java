package org.bahmni.mart;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FreeMarkerConfig.class)
public class FreeMarkerConfigTest {

    @Mock
    private Configuration configuration;

    @Test
    public void shouldAddFreeMarkerConfiguration() throws Exception {
        FreeMarkerConfig freeMarkerConfig = new FreeMarkerConfig();
        whenNew(Configuration.class).withArguments(any()).thenReturn(configuration);

        Configuration actualConfig = freeMarkerConfig.freeMarkerConfiguration();
        assertEquals(configuration, actualConfig);
        verify(configuration, times(1)).setDefaultEncoding("UTF-8");
        verify(configuration, times(1)).setClassForTemplateLoading(any(), eq("/templates"));
        verify(configuration, times(1)).setTemplateExceptionHandler(any(TemplateExceptionHandler.class));
    }

}