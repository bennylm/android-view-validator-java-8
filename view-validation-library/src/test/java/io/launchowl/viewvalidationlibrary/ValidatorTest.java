package io.launchowl.viewvalidationlibrary;

import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;

import io.launchowl.viewvalidationlibrary.Criteria;
import io.launchowl.viewvalidationlibrary.Observer;
import io.launchowl.viewvalidationlibrary.Validator;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class ValidatorTest {
    @Mock
    private TextView mockTextView;

    @Mock
    private Button mockButton1;

    @Mock
    private Button mockButton2;

    @Mock
    private Button mockButton3;

    @Mock
    private EditText mockEditText;

    @Mock
    private Editable mockEditable;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void observe_ThreeObservers_AddThreeObservers() throws Exception {
        Validator validator = new Validator<EditText>(new Criteria<EditText>(mockEditText));
        validator.observe(mockButton1Observer, mockButton2Observer, mockButton3Observer);

        assertEquals(3, validator.getObservers().size());
    }


    @Test
    public void validate_EditTextHasValidText_MockEditText() {
        final String validText = "Hello";

        when(mockEditText.getText()).thenReturn(mockEditable);
        when(mockEditable.toString()).thenReturn(validText);

       Validator validator = new Validator<EditText>(new Criteria<EditText>(mockEditText)
        .test(new Criteria.Condition<EditText>() {
            @Override
            public boolean evaluate(EditText view) {
                assertEquals(validText, view.getText().toString());

                return false;
            }
        })
       );

        validator.validate();
    }

    private Observer mockButton1Observer = new Observer<Button>(mockButton1) {
        @Override
        public void onValidationComplete(Button button, Validator.ValidationResult validationResult) {

        }
    };

    private Observer mockButton2Observer = new Observer<Button>(mockButton2) {
        @Override
        public void onValidationComplete(Button button, Validator.ValidationResult validationResult) {
        }
    };

    private Observer mockButton3Observer = new Observer<Button>(mockButton3) {
        @Override
        public void onValidationComplete(Button button, Validator.ValidationResult validationResult) {
        }
    };
}