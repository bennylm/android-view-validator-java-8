package io.launchowl.viewvalidationlibrary;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class contains a collection conditions that are tested to determine if a view is valid.
 * <p>
 * A Criteria object contains one or more {@link Condition} or {@link AsyncCondition} that are added
 * via {@link #test(Condition)} and {@link #asyncTest(AsyncCondition)}, respectively.
 * <p>
 * All conditions can be evaluated by calling the {@link Criteria#evaluate(EvalCompleteListener)}
 * method. The method will deliver a single result ({@link io.launchowl.viewvalidationlibrary.Validator.ValidationResult})
 * to the supplied {@link EvalCompleteListener}.
 *
 * @param <T> the type of {@link View} being validated
 */
public class Criteria<T extends View> {
    private int asyncConditionsComplete = 0;
    private Validator.ValidationResult validationResult = Validator.ValidationResult.Valid;
    private EvalCompleteListener evalCompleteListener;
    final private T validatedView;
    final private Set<Condition<T>> conditions;
    final private Set<AsyncCondition<T>> asyncConditions;
    final private Criteria<T> criteria;

    /**
     * A condition is a single test that will return true or false.
     * <p>
     * Conditions are intended to test a single scenario. Add additiona conditions to a
     * {@link Criteria} instance to test each unique unique scenario.
     * <p>
     * For example, one condition could test whether a username contain valid characters. A separate
     * condition could test whether the username contains profanity.
     *
     * @param <T> the type of {@link View} being validated
     *
     * @see AsyncCondition
     */
    public interface Condition<T> {

        /**
         * Perform a test using data from the view being validated.
         * <p>
         * <pre>
         *  // Assumes <i>view</i> is a {@link android.widget.TextView}
         *  // Make sure the username contains a fruit
         * {@code String[] fruit = {
         *   "apple",
         *   "banana",
         *   "blueberry",
         *   "kiwi",
         *   "orange",
         *   "strawberry"
         *  };
         *  Pattern pattern = Pattern.compile(TextUtils.join("|", fruit));
         *  Matcher matcher = pattern.matcher(view.getText().toString().toLowerCase());
         *  return matcher.find();
         * }
         * </pre>
         * @param view the {@link View} being validated
         *
         * @return true or false depending on whether the test passed
         */
        boolean evaluate(T view);
    }

    /**
     * An asynchronous condition is a single test that performs an asynchronous operation and
     * then returns a true or false value by invoking {@link #complete(boolean)}.
     * <p>
     * Conditions are intended to test a single scenario. Add additional conditions to a
     * {@link Criteria} instance to test each unique scenario.
     * <p>
     * For example, the asynchronous operation could check if a username is available by
     * querying a web service.
     * <p>
     * The {@link #complete(boolean)} method should be called in the overridden
     * {@link #evaluate(Object)} method to notify the Criteria object that the
     * asynchronous operation is complete.
     *
     * @param <T> the type of {@link View} being validated
     */
    public static abstract class AsyncCondition<T> {
        private boolean cancelled = false;
        private AsyncTask asyncTask;
        private Criteria criteria;
        private final Handler handler;
        private Thread thread;
        private Message message;

        /**
         * Class constructor that creates a new {@link Handler} which will be used for communicating
         * the response to the main UI thread after the asynchronous operation is complete.
         * <p>
         * To learn about communicating with the UI thread, see "Communicating with the UI Thread":
         * https://developer.android.com/training/multiple-threads/communicate-ui.html
         *
         * @see #asyncConditionComplete(boolean)
         */
        public AsyncCondition() {
            this.handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message inputMessage) {
                    if (!cancelled) {
                        criteria.asyncConditionComplete((boolean) inputMessage.obj);
                    }
                }
            };
        }

        /**
         * Notifies the Criteria object that the asynchronous operation is complete.
         * <p>
         * This method should be called be called in the overridden {@link #evaluate(Object)}
         * method.
         *
         * @param result true if the test passed, otherwise false
         */
        protected final void complete(boolean result) {

            // Send the result to the handler which will notify the Criteria object on the main UI thread.
            this.message = makeMessage();
            message.obj = result;
            message.sendToTarget();
        }

        /**
         * Performs an asynchronous test using information from the view being evaluated.
         *
         * @param asyncConditionCompletionListener an {@link AsyncConditionCompletionListener}
         * @param view the {@link View} being evaluated
         */

        /**
         * Perform a test using data from the view being evaluated.
         * <p>
         * Any code included in this method will be executed on a separate thread. After the
         * data has been evaluated call {@link #complete(boolean)} to notify the Criteria
         * object that the asynchronous operation is complete.
         * <p>
         * If {@link #complete(boolean)} is not executed, then
         * {@link EvalCompleteListener#onComplete(Validator.ValidationResult)} will never be invoked.
         *
         * @param view
         */
        protected abstract void evaluate(T view);

        /**
         * This method is called if {@link Criteria#cancelValidation()} ()} is invoked.
         */
        protected abstract void onCancelled();

        /**
         * Cancels the asynchronous operation.
         */
        final void cancel() {
            cancelled = true;
            if (this.thread != null) {
                this.thread.interrupt();
            }
            this.onCancelled();
        }

        /**
         * Executes {@link #evaluate(Object)} inside of a new {@link Thread}.
         *
         * @param criteria the instance of the enclosing {@link Criteria} object
         * @param view the {@link View} being validated
         */
        final void initEvaluate(final Criteria criteria, final T view) {
            cancelled = false;
            this.criteria = criteria;

            // Kill the current thread to prevent a race condition.
            if (this.thread != null) {
                this.thread.interrupt();
            }


            this.thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    evaluate(view);
                }
            });
            thread.start();
        }

        /**
         * Returns a new message from the global message pool.
         * <p>
         * For testing.
         *
         * @return
         *
         * @see Handler#obtainMessage()
         */
        Message makeMessage() {
            return this.handler.obtainMessage();
        }
    }

    /**
     * This interface is supplied to the {@link Criteria#evaluate(EvalCompleteListener)}
     * method and receives the final validation result after all conditions in a criteria object
     * have been completed their tests.
     */
    public interface EvalCompleteListener {

        /**
         * This method is called after all conditions of the criteria
         * have been tested.
         *
         * @param validationResult the result after all conditions have been tested
         */
        void onComplete(Validator.ValidationResult validationResult);
    }

    /**
     * Class constructor specifying the view being validated.
     *
     * @param validatedView the {@link View} being validated
     */
    public Criteria(T validatedView) {
        this.validatedView = validatedView;
        this.conditions = new HashSet<>();
        this.asyncConditions = new HashSet<>();
        this.criteria = this;
    }

    /**
     * Adds an {@link AsyncCondition} to be tested.
     * <p>
     * For example, the asynchronous operation could evaluate whether a
     * username is available.
     *
     * @param asyncCondition a condition that performs an asynchronous operation
     * @return this {@link Criteria} instance
     *
     * @see AsyncCondition
     * @see Condition
     */
    public Criteria<T> asyncTest(AsyncCondition<T> asyncCondition) {
        this.asyncConditions.add(asyncCondition);
        return this;
    }

    /**
     * Adds a {@link Condition} to be tested.
     * <p>
     * For example, the synchronous operation could evaluate whether a username contains valid
     * characters.
     *
     * @param condition a condition that can be tested immediately
     * @return this {@link Criteria} instance
     *
     * @see AsyncCondition
     * @see Condition
     */
    public Criteria<T> test(Condition<T> condition) {
        this.conditions.add(condition);
        return this;
    }


    /**
     * Evaluates all {@link Condition} and {@link AsyncCondition} objects associated with
     * this instance.
     *
     * @param evalCompleteListener an {@link EvalCompleteListener} that will handle the final result
     */
    void evaluate(EvalCompleteListener evalCompleteListener) {
        this.evalCompleteListener = evalCompleteListener;

        // Initiate all asynchronous evaluations.
        evaluateAsyncConditions();

        // Perform all synchronous evaluations.
        evaluateConditions();

        // Only complete if there aren't any AsyncCondition objects still running.
        if (this.asyncConditions.size() == 0) {
            complete();
        }
    }

    /**
     * Evaluate all synchronous conditions.
     */
    void evaluateConditions() {
        for (Condition<T> condition : this.conditions) {
            setValidationResult(condition.evaluate(this.validatedView));
        }
    }

    /**
     * Initiate all asynchronous conditions.
     */
    void evaluateAsyncConditions() {
        for (AsyncCondition<T> asyncCondition : this.asyncConditions) {
            asyncCondition.initEvaluate(this, this.validatedView);
        }
    }

    void cancelValidation() {
        for (AsyncCondition<T> asyncCondition : this.asyncConditions) {
            asyncCondition.cancel();
        }
    }

    /**
     * Returns all {@link Condition} objects added to this instance.
     * <p>
     * For testing.
     * @return all {@link Condition} objects added to this instance
     */
    Set<Condition<T>> getConditions() {
        return Collections.unmodifiableSet(this.conditions);
    }

    /**
     * Returns all {@link AsyncCondition} objects added to this instance.
     * <p>
     * For testing.
     * @return all {@link AsyncCondition} objects added to this instance
     */
    Set<AsyncCondition<T>> getAsyncConditions() {
        return Collections.unmodifiableSet(this.asyncConditions);
    }

    /**
     * This method is called when {@link AsyncCondition#complete(boolean)} is invoked.
     *
     * @param result the result of testing the condition
     *
     * @see AsyncCondition
     */
    void asyncConditionComplete(boolean result) {
        this.asyncConditionsComplete++;
        setValidationResult(result);

        complete();
    }

    /**
     * This method is called by {@link #evaluate(EvalCompleteListener)} and/or
     * {@link #asyncConditionComplete(boolean)} after all synchronous and/or asynchronous conditions
     * have completed their tests.
     */
    private void complete() {
        if (this.asyncConditions.size() == asyncConditionsComplete) {
            this.evalCompleteListener.onComplete(this.validationResult);
            reset();
        }
    }

    /**
     * This method is called by {@link #evaluate(EvalCompleteListener)} or
     * {@link #asyncConditionComplete(boolean)} to set the {
     * @link io.launchowl.viewvalidationlibrary.Validator.ValidationResult} value supplied to
     * the {@link EvalCompleteListener}.
     *
     * @param result the result of testing the condition
     */
    private void setValidationResult(boolean result) {

        // Only set to Invalid if the test didn't pass since the default value is Valid.
        if (!result) {
            this.validationResult = Validator.ValidationResult.Invalid;
        }
    }

    /**
     * Resets default values.
     */
    private void reset() {
        this.asyncConditionsComplete = 0;
        this.validationResult = Validator.ValidationResult.Valid;
    }
}
