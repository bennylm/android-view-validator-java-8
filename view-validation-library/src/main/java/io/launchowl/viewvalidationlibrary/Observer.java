package io.launchowl.viewvalidationlibrary;

import android.view.View;

/**
 * This abstract class is used to observe if a {@link View} is valid.
 * <p>
 * Observers are added to {@link Validator} instances and are notified after
 * all conditions of a {@link Criteria} object are tested via the {@link Validator#validate()}
 * method.
 *
 * @param <T> the type of {@link View} that needs to respond to the state of the observed view
 *
 * @see Validator
 * @see Criteria
 * @see io.launchowl.viewvalidationlibrary.Criteria.AsyncCondition
 * @see io.launchowl.viewvalidationlibrary.Criteria.Condition
 */
public abstract class Observer<T extends View> {
    private final T observerView;

    /**
     * Class constructor that is supplied with the view that will be udpated based on the
     * validity of the observed view.
     *
     * @param observerView the {@link View} that will be updated based on the observed view
     */
    public Observer(T observerView) {
        this.observerView = observerView;
    }

    /**
     * Notifies the observer of the observee view's validity.
     * <p>
     * This method calls {@link #onValidationComplete(View, Validator.ValidationResult)}.
     *
     * @param validationResult the state of the view being validated
     */
    void update(Validator.ValidationResult validationResult) {
        onValidationComplete(getView(), validationResult);
    }

    /**
     * Returns the {@link View} supplied to the {@link #Observer(View)} constructor.
     *
     * @return the {@link View} supplied to the {@link #Observer(View)} constructor
     */
    T getView() {
        return this.observerView;
    }

    /**
     * This abstract method defines what will happen to the observer's view based upon the validity
     * of the view being observed.
     * <p>
     * <pre>
     * // Assuming the <i>view</i> is a {@link android.widget.Button}
     * {@code
     *
     *  protected void onValidationComplete(Button view, Validator.ValidationResult validationResult) {
     *   view.setEnabled(validationResult == Validator.ValidationResult.Valid);
     *  }
     * }
     * </pre>
     *
     * @param view the {@link View} supplied to the {@link #Observer(View)} constructor
     * @param validationResult the state of the view being validated
     */
    protected abstract void onValidationComplete(T view, Validator.ValidationResult validationResult);
}
