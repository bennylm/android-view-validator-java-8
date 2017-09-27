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
import io.launchowl.viewvalidationlibrary.Validator;
import io.launchowl.viewvalidationlibrary.ValidatorSet;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
public class ValidatorSetTest {
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
    public void add_AddThreeValidators_ThreeValidators_OnePerCall() throws Exception {
        ValidatorSet validatorSet = new ValidatorSet();
        validatorSet.add(new Validator<Button>(new Criteria<Button>(mockButton1)));
        validatorSet.add(new Validator<Button>(new Criteria<Button>(mockButton2)));
        validatorSet.add(new Validator<Button>(new Criteria<Button>(mockButton3)));

        assertEquals(3, validatorSet.getValidators().size());
    }

    @Test
    public void add_AddThreeValidators_ThreeValidators_ThreeAtOnce() throws Exception {
        ValidatorSet validatorSet = new ValidatorSet();
        validatorSet.add(
                new Validator<Button>(new Criteria<Button>(mockButton1)),
                new Validator<Button>(new Criteria<Button>(mockButton2)),
                new Validator<Button>(new Criteria<Button>(mockButton3))
        );

        assertEquals(3, validatorSet.getValidators().size());
    }

    @Test
    public void remove_OneValidator_AddTwoRemoveOne() throws Exception {
        ValidatorSet validatorSet = new ValidatorSet();
        Validator<Button> buttonValidator = new Validator<Button>(new Criteria<Button>(mockButton1));
        validatorSet.add(buttonValidator);
        validatorSet.add(new Validator<Button>(new Criteria<Button>(mockButton2)));

        validatorSet.remove(buttonValidator);

        assertEquals(1, validatorSet.getValidators().size());
    }

    int validatorsValidated = 0;
    @Test
    public void validate_TwoValidatorsValidated_TwoValidators() throws Exception {
        final String validText = "Hello";


        when(mockEditText.getText()).thenReturn(mockEditable);
        when(mockEditable.toString()).thenReturn(validText);

        Validator validator1 = new Validator<EditText>(new Criteria<EditText>(mockEditText)
                .test(new Criteria.Condition<EditText>() {
                    @Override
                    public boolean evaluate(EditText view) {
                        validatorsValidated++;

                        return false;
                    }
                })
        );

        Validator validator2 = new Validator<EditText>(new Criteria<EditText>(mockEditText)
                .test(new Criteria.Condition<EditText>() {
                    @Override
                    public boolean evaluate(EditText view) {
                        validatorsValidated++;

                        return false;
                    }
                })
        );

        ValidatorSet validatorSet = new ValidatorSet(validator1, validator2);
        validatorSet.validate();

        assertEquals(2, validatorsValidated);
    }

}