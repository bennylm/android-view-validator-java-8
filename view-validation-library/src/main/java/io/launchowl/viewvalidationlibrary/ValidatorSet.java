package io.launchowl.viewvalidationlibrary;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class can be used to evaluate multiple {@link Validator} objects.
 * <p>
 * The best case scenario to use a ValidatorSet is when there are multiple
 * views or validations that need to be evaluated before continuing.
 * <p>
 * For example,
 * a registration {@link android.app.Activity} might contain multiple views for
 * capturing information about an individual. Instead of evaluating each
 * view after text is changed, all views could be evaluated when a
 * <i>continue</i> button is pressed.
 */
public class ValidatorSet implements Validation {
    private Set<Validator> validators;

    /**
     * Class constructor.
     */
    public ValidatorSet() {
        this.validators = new HashSet<>();
    }

    /**
     * Class constructor that accepts a {@link Validator} object.
     * <p>
     * Multiple {@link Validator} objects can be supplied as a comma-delimited list.
     *
     * @param validators one or more {@link Validator} objects
     */
    public ValidatorSet(Validator ...validators) {
        this.validators = new HashSet<>(Arrays.asList(validators));
    }

    /**
     * Adds a {@link Validator}.
     *
     * @param validator a {@link Validator}
     *
     * @return true if the {@link Validator} was added, otherwise false
     */
    public boolean add(Validator validator) {
        return this.validators.add(validator);
    }

    /**
     * Adds multiple {@link Validator} objects.
     * <p>
     * Multiple {@link Validator} objects can be supplied as a comma-delimited list.
     *
     * @param validators one or more {@link Validator} objects
     *
     * @return true if all the {@link Validator} was added, otherwise false
     */
    public boolean add(Validator ...validators) {
        return this.validators.addAll(Arrays.asList(validators));
    }

    /**
     * Removes a {@link Validator} object.
     *
     * @param validator the {@link Validator} to be removed
     *
     * @return true if the {@link Validator} was found and removed, otherwise false
     */
    public boolean remove(Validator validator) {
        return this.validators.remove(validator);
    }

    /**
     * Returns a collection of {@link Validator} objects associated with this instance.
     * <p>
     * For testing.
     *
     * @return a collection of {@link Validator} objects associated with this instance
     */
    Set<Validator> getValidators() {
        return Collections.unmodifiableSet(this.validators);
    }

    /**
     * Requests each {@link Validator} associated with this instance to validate istelf
     * by calling the {@link Validator#validate()} method.
     */
    @Override
    public void validate() {
        for (Validator validator : validators) {
            validator.validate();
        }
    }

    public void cancelValidation() {
        for (Validator validator : validators) {
            validator.cancelValidation();
        }
    }
}
