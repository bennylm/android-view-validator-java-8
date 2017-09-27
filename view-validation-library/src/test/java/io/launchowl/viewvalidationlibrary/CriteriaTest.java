package io.launchowl.viewvalidationlibrary;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.EditText;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static java.lang.Thread.sleep;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.expectNew;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replay;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verify;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Looper.class, Criteria.class })
public class CriteriaTest {
    @Mock
    private TextView mockTextView;

    @Mock
    private EditText mockEditText;

    @Mock
    private Looper mockMainLooper;

    @Mock
    private Handler mockHandler;

    @Mock
    private Message mockMessage;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    /*@Test
    public void test_ValidResult_ValidState() throws Exception {
        Criteria<TextView> textViewAsyncCondition = new Criteria<TextView>() {
            @Override
            public void test(TextView textView, final TestCompleteListener testCompleteListener) {
                SomeAsyncTask.doAsync(new SomeAsyncTask.SomeAsyncTaskCompleteListener() {
                    @Override
                    public void onAsyncTaskComplete() {
                        testCompleteListener.onComplete(Validator.ValidationResult.Valid);

                    }
                });
            }
        };


        textViewAsyncCondition.test(mockTextView, new TestCompleteListener() {
           @Override
            public void onComplete(Validator.ValidationResult validationResult) {
               assertEquals(Validator.ValidationResult.Valid, validationResult);
           }
        });
    }

    @Test
    public void test_ViewsToBeEqual_TextView() throws Exception {
        Criteria<TextView> textViewCriteria = new Criteria<TextView>() {
            @Override
            public void test(TextView textView, final TestCompleteListener asyncConditionListener) {
                assertEquals(mockTextView, textView);
            }
        };

        textViewCriteria.test(mockTextView, null);
    }*/

    @Test
    public void test_ThreeConditionsAdded_AddThreeConditions() {
        Criteria<EditText> criteria = new Criteria<EditText>(mockEditText);
        criteria.test(new Criteria.Condition<EditText>() {
            @Override
            public boolean evaluate(EditText view) {
                return true;
            }
        }).test(new Criteria.Condition<EditText>() {
            @Override
            public boolean evaluate(EditText view) {
                return true;
            }
        }).test(new Criteria.Condition<EditText>() {
            @Override
            public boolean evaluate(EditText view) {
                return true;
            }
        });

        assertEquals(3, criteria.getConditions().size());
    }

    @Test
    public void test_TwoAsyncConditionsAdded_AddTwoAsyncConditions() {
        mockStatic(Looper.class);
        expect(Looper.getMainLooper()).andReturn(mockMainLooper).times(2);
        replayAll();

        Criteria.AsyncCondition<EditText> asyncCondition1 = spy(new Criteria.AsyncCondition<EditText>() {
            @Override
            public void evaluate(EditText view) {

            }

            @Override
            public void onCancelled() {

            }
        });


        Criteria.AsyncCondition<EditText> asyncCondition2 = new Criteria.AsyncCondition<EditText>() {
            @Override
            public void evaluate(EditText view) {

            }

            @Override
            public void onCancelled() {

            }
        };

        Criteria<EditText> criteria = new Criteria<EditText>(mockEditText);
        criteria
                .asyncTest(asyncCondition1)
                .asyncTest(asyncCondition2);

        assertEquals(2, criteria.getAsyncConditions().size());
    }

    @Test
    public void test_MixedTestsCompletesAsInvalid_AsyncConditionReturnsFalse() throws Exception {
        mockStatic(Looper.class);
        expect(Looper.getMainLooper()).andReturn(mockMainLooper);

        final Criteria.AsyncCondition<EditText> asyncCondition = spy(new Criteria.AsyncCondition<EditText>() {
            @Override
            public void evaluate(EditText view) {

            }

            @Override
            public void onCancelled() {

            }
        });

        doReturn(mockMessage).when(asyncCondition).makeMessage();


        Criteria.Condition<EditText> condition = new Criteria.Condition<EditText>() {
            @Override
            public boolean evaluate(EditText view) {
                return true;
            }
        };

        final Criteria<EditText> criteria = spy(new Criteria<EditText>(mockEditText));
        criteria
                .asyncTest(asyncCondition)
                .test(condition);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                criteria.asyncConditionComplete(false);

                return null;
            }
        }).when(criteria).evaluateAsyncConditions();

        criteria.evaluate(new Criteria.EvalCompleteListener() {
            @Override
            public void onComplete(Validator.ValidationResult validationResult) {
                assertEquals(Validator.ValidationResult.Invalid, validationResult);
            }
        });
    }

    @Test
    public void evaluate_AsyncTestsCompletesAsValid_TwoAsyncConditionsReturnTrue() {
        mockStatic(Looper.class);
        expect(Looper.getMainLooper()).andReturn(mockMainLooper);

        final Criteria.AsyncCondition<EditText> asyncCondition = spy(new Criteria.AsyncCondition<EditText>() {
            @Override
            public void evaluate(EditText view) {

            }

            @Override
            public void onCancelled() {

            }
        });

        doReturn(mockMessage).when(asyncCondition).makeMessage();


        Criteria.Condition<EditText> condition = new Criteria.Condition<EditText>() {
            @Override
            public boolean evaluate(EditText view) {
                return true;
            }
        };

        final Criteria<EditText> criteria = spy(new Criteria<EditText>(mockEditText));
        criteria
                .asyncTest(asyncCondition)
                .asyncTest(asyncCondition);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                criteria.asyncConditionComplete(true);

                return null;
            }
        }).when(criteria).evaluateAsyncConditions();

        criteria.evaluate(new Criteria.EvalCompleteListener() {
            @Override
            public void onComplete(Validator.ValidationResult validationResult) {
                assertEquals(Validator.ValidationResult.Valid, validationResult);
            }
        });
    }

    int cancelCount = 0;
    @Test
    public void cancel_AsyncConditionCancelled_CancelInvoked() {

        mockStatic(Looper.class);
        expect(Looper.getMainLooper()).andReturn(mockMainLooper).times(2);
        replayAll();

        Criteria<EditText> criteria = new Criteria<EditText>(mockEditText);
        criteria.asyncTest(new Criteria.AsyncCondition<EditText>() {
            @Override
            public void evaluate(EditText view) {

            }

            @Override
            public void onCancelled() {
                cancelCount++;
            }
        }).asyncTest(new Criteria.AsyncCondition<EditText>() {
            @Override
            public void evaluate(EditText view) {

            }

            @Override
            public void onCancelled() {
                cancelCount++;
            }
        });

        criteria.evaluate(new Criteria.EvalCompleteListener() {
            @Override
            public void onComplete(Validator.ValidationResult validationResult) {

            }
        });

        criteria.cancelValidation();

        assertEquals(2, cancelCount);
    }
}