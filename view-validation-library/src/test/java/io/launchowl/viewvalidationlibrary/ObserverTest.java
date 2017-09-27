package io.launchowl.viewvalidationlibrary;

import android.widget.Button;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.launchowl.viewvalidationlibrary.Observer;
import io.launchowl.viewvalidationlibrary.Validator;

import static org.junit.Assert.assertEquals;

public class ObserverTest {
    @Mock
    private Button mockButton;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void update_ViewsToBeEqual_Button() throws Exception {
        Observer<Button> buttonObserver = new Observer<Button>(mockButton) {
            @Override
            protected void onValidationComplete(Button view, Validator.ValidationResult validationResult) {
                assertEquals(mockButton, view);
            }
        };

        buttonObserver.update(Validator.ValidationResult.Valid);
    }

    @Test
    public void update_ValidResult_ValidState() throws Exception {
        Observer<Button> buttonObserver = new Observer<Button>(mockButton) {
            @Override
            protected void onValidationComplete(Button view, Validator.ValidationResult validationResult) {
                assertEquals(Validator.ValidationResult.Valid, validationResult);
            }
        };

        buttonObserver.update(Validator.ValidationResult.Valid);
    }
}