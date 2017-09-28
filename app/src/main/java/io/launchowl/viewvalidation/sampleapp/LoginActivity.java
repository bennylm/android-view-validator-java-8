package io.launchowl.viewvalidation.sampleapp;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.launchowl.viewvalidationlibrary.Criteria;
import io.launchowl.viewvalidationlibrary.Observer;
import io.launchowl.viewvalidationlibrary.Validator;
import io.launchowl.viewvalidationlibrary.ValidatorSet;

/**
 * This Activity demonstrates validating views using the
 * {@link Validator} class.
 */
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /* Calling TextInputLayout.setErrorEnabled(boolean enabled) prevents the
         * layout from changing size when an error is displayed.
         */
        ((TextInputLayout) findViewById(R.id.user_name_layout)).setErrorEnabled(true);

        /* The views will be available in the onCreate method.
         * Offload creating the Validator and Observer
         * objects in a separate method.
         */
        initFormValidation();
    }

    /*
     * Setup the form validation
     */
    void initFormValidation() {
        /*
         * Create a Validator for the username field that will be used to
         * check if a username is available.
         *
         * A single AsyncCondition will is added to the validator. The UserRepository
         * class contains a collection of existing usernames to search. It returns
         * a response in <=1500ms to simulate querying a remote service.
         */
        EditText userNameEditText = (EditText) findViewById(R.id.user_name);
        final Validator<EditText> userNameAvailableValidator = new Validator<>(new Criteria<>(userNameEditText)
            .asyncTest(new Criteria.AsyncCondition<EditText>() {

                @Override
                protected void evaluate(EditText view) {
                    UserRepository userRepository = new UserRepository();

                    /* Java 8
                     *  => Lambda Expression
                     */
                    userRepository.getUser(view.getText().toString(), user -> complete(user == null));
                }

                @Override
                protected void onCancelled() {
                    resetViews();
                }
            })
        );

        /*
         * Create an observer for the username status message.
         */
        Observer<TextView> userNameStatusObserver = new Observer<TextView>((TextView) findViewById(R.id.username_status)) {
            @Override
            protected void onValidationComplete(TextView view, Validator.ValidationResult validationResult) {
                // Display whether the username is "Available" or "Not available".
                view.setText(
                        validationResult == Validator.ValidationResult.Valid
                                ? getString(R.string.success_available)
                                : getString(R.string.error_not_available)
                );

                // Change the color of the text.
                view.setTextColor(
                        validationResult == Validator.ValidationResult.Valid
                                ? getColor(R.color.success_color)
                                : getColor(R.color.error_color)
                );

            }
        };

        // Add the observers
        userNameAvailableValidator.observe(userNameStatusObserver);

        /*
         * Create a Validator for the username field that will
         *  be used to check if it contains valid characters
        */
        final Validator<EditText> userNameCompliesValidator = new Validator<>(new Criteria<>(userNameEditText)
                // Make sure it doesn't contain special characters
                /* Java 8
                 *  => Lambda Expression
                 */
                .test(editText -> Pattern.matches("^[a-zA-Z0-9]*$", editText.getText().toString()))

                // Make sure it contains the name of a popular fruit
                /* Java 8
                 *  => Lambda Expression
                 */
                .test(editText -> {
                    String[] fruit = {
                            "apple",
                            "banana",
                            "blueberry",
                            "kiwi",
                            "orange",
                            "strawberry"
                    };
                    Pattern pattern = Pattern.compile(TextUtils.join("|", fruit));
                    Matcher matcher = pattern.matcher(editText.getText().toString().toLowerCase());
                    return matcher.find();
                })
        );

        userNameCompliesValidator.observe(
                /*
                 * This is a new observer for the username status TextView. If the username doesn't meet
                 * the expected criteria then we want to hide the status completely, so we're not
                 * overcrowding the space below the EditText view.
                 */
                new Observer<TextView>((TextView) findViewById(R.id.username_status)) {
                    @Override
                    protected void onValidationComplete(TextView view, Validator.ValidationResult validationResult) {
                        view.setVisibility(validationResult == Validator.ValidationResult.Valid
                                ? View.VISIBLE
                                : View.GONE);
                    }
                },

                /*
                 * Create an observer that will call TextInputLayout.setError(CharSequence error)
                 * if the username contains invalid characters. This method will display the message
                 * below the EditText wrapped inside the TextInputLayout.
                 *
                 */
                new Observer<TextInputLayout>((TextInputLayout) findViewById(R.id.user_name_layout)) {
                    @Override
                    protected void onValidationComplete(TextInputLayout view, Validator.ValidationResult validationResult) {
                        view.setError(validationResult == Validator.ValidationResult.Valid
                                ? null
                                : getString(R.string.error_invalid_username));
                    }
                });

        // Add the validators to a ValidatorSet so they can both be validated via a single request.
        final ValidatorSet validatorSet = new ValidatorSet(userNameAvailableValidator, userNameCompliesValidator);

        // Listen for text being modified in the user name view.
        userNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                // If at least 4 characters have been entered then validate the input.
                if (s.toString().length() > 3) {

                    /*
                     * Calling validate() on the validatorSet object will evaluate both
                     * userNameAvailableValidator and userNameCompliesValidator.
                     */
                    validatorSet.validate();
                } else {
                    validatorSet.cancelValidation();
                    // Reset the views if the input is not at least 4 characters.
                    resetViews();
                }
            }
        });
    }

    /*
     * Reset the views to their default state.
     */
    void resetViews() {
        ((TextInputLayout) findViewById(R.id.user_name_layout)).setError(null);
        findViewById(R.id.username_status).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.username_status)).setText(getString(R.string.empty_string));
    }
}

