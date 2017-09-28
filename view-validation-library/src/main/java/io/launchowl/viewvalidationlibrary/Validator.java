package io.launchowl.viewvalidationlibrary;

import android.view.View;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is responsible for evaluating whether a {@link View} is valid.
 * <p>
 * The validator acts as a mediator between the <i>view</i> being evaluated
 * and the <i>views</i> that need to be updated. This separation of responsibility
 * alleviates the {@link android.app.Activity} from changing state of multiple views
 * and hands it off the {@link Observer} objects.
 * <p>
 * A {@link Criteria} object supplied to a validator object contains all the conditions that must
 * be tested in order to determine if a view is valid.
 * <p>
 * The state of the view under test is communicated to all {@link Observer} instances listening
 * as a {@link ValidationResult} value.
 *
 * @param <T> the {@link View} being evaluated for a valid state
 */
public class Validator<T extends View> implements Validation {
    Set<Observer> observers;
    Criteria<T> criteria;
    public enum ValidationResult {
        Valid,
        Invalid
    }

    /**
     * Class constructor that is supplied a {@link Criteria} object.
     *
     * @param criteria a {@link Criteria} object that contains all conditions to be tested
     */
    public Validator(Criteria<T> criteria) {
        this.observers = new HashSet<>();
        this.criteria = criteria;
    }

    /**
     * Add an {@link Observer}.
     * <p>
     * Multiple observers can be supplied as a comma-delimited list.
     *
     * @param observers one or more {@link Observer} objects
     */
    public void observe(Observer... observers) {
        Collections.addAll(this.observers, observers);
    }

    /**
     * Evaluate all conditions that belong to the {@link Criteria} object.
     */
    @Override
    public void validate() {

        /* Java 8
         *  => Lambda Expression
         */
        this.criteria.evaluate(validationResult -> Notifier.notify(observers, validationResult));
    }

    public void cancelValidation() {
        this.criteria.cancelValidation();
    }

    /**
     * Returns a collection of {@link Observer} objects that have been added via {@link #observe(Observer[])}.
     * <p>
     * For testing.
     * @return a collection of {@link Observer} objects
     */
    Set<Observer> getObservers() {
        return this.observers;
    }

    /**
     * A class that notifies a collection of {@link Observer} objects.
     */
    private static class Notifier {
        /**
         * Notifies a collection of {@link Observer} objects with the state of the
         * view that's being evaluated.
         *
         * @param observers a collection of {@link Observer} objects
         * @param validationResult the state of the view being evaluated
         */
         static void notify(Set<Observer> observers, ValidationResult validationResult) {

             /* Java 8
              *  => collection.forEach(Consumer<? super T> action)
              */
             observers.forEach(observer -> observer.update(validationResult));
        }
    }
}
