# Android View Validator + Observer Views

The Android View Validator enables a developer to validate the content of a [View](https://developer.android.com/reference/android/view/View.html) and notify observing views so they can update their own state accordingly. 

> **Note:** This project was created using [Android Studio](https://developer.android.com/studio/install.html). There are two separate Android Studio modules:
>- **android-view-validation** is an [Android Library](https://developer.android.com/studio/projects/android-library.html) that contains all the logic for validating and observing views. This [module can be added](https://developer.android.com/studio/projects/android-library.html#AddDependency) to any existing Android Studio project.
>- **app** is a *Phone & Tablet* module that contains a sample app that uses the *android-view-validation* module.

> **Note:** A **Java 8 version** of this project is available here. You will need to open the project with [Android Studio Preview](https://developer.android.com/studio/preview/index.html).

## Project Goal
At times you might need to update the state of a View (ex: provide a helpful user message) based on the state of a different View (ex: user input in an ```EditText```) in the same activity. The goal of this project is to isolate the task of validating views from the task of updating any dependent views. It also alleviates the Activity from the responsibility of performing validation by allowing more appropriate classes to handle the work.

The mediator pattern is the shining star in this project. You might point out that the Activity is already behaving as a mediator between the views. However, this approach allows a more expressive way of communicating the events that are occurring. 

## Intro

When you're creating forms in an Android Activity you might find yourself needing to check whether the View is valid. For example, suppose you have a signup form where a user must enter a unique username. The form will more than likely contain an [EditText](https://developer.android.com/reference/android/widget/EditText.html) view (or a [TextInputEditText](https://developer.android.com/reference/android/support/design/widget/TextInputEditText.html) wrapped inside of [TextInputLayout](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html)).

Based on the input provided in the EditText view you might want to display a message informing the user that the username is available or not available by querying a web service. You might also want to display an error message ([TextInputLayout.setError(CharSequence error)](https://developer.android.com/reference/android/support/design/widget/TextInputLayout.html#setError(java.lang.CharSequence))) if the input contains unacceptable characters (*!@#$%^&*).

![Sample App](/images/view-validation-app-original.gif)

## Getting Started

#### Option 1
***
1. Clone this repository.
```sh
$ git clone https://github.com/bennylm/Android-View-Validator.git
```
2. Open the project in Android Studio.
3. Run app on a device or AVD.
4. Type different usernames to see messages appear.

| Must Contain One of These Words | Existing Usernames |
| ------------ | -------------
| apple | coolblueberry |
| banana | happyorange |
| blueberry | iceapple |
| kiwi  | realkiwi |
| orange | |
| strawberry | |

#### Option 2
***
1. Clone this repository.
```sh
$ git clone https://github.com/bennylm/Android-View-Validator.git
```
2. Open the project in Android Studio.
3. Click the project via **Build > Make Project** (this will generate an Android Archive/AAR file).
```
.\android-view-validation\view-validation-library\build\outputs\aar\view-validation-library-debug.aar
```
4. Create/open your own project in Android Studio.
5. Add a new module via **File > New > New Module...**.
6. Select **Import .JAR/.AAR Package**.
5. Update settings.gradle to look similar to this:
```
include ':app', ':view-validation-library-debug'
```

## A Validation Attempt

Let's demonstrate the scenario mentioned above before using basic control flow -- without any separation of responsibility.

##### Monitor an `````EditText````` view as the user inputs characters (*don't do this...it ends up getting ugly*)
```java
EditText userNameEditText = (EditText) findViewById(R.id.user_name);
userNameEditText.addTextChangedListener(new TextWatcher() {
     @Override
     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
     }

     @Override
     public void onTextChanged(CharSequence s, int start, int before, int count) {

     }

     @Override
     public void afterTextChanged(Editable s) {
         String input = s.toString();
         if (input.length() > 3) {
         
            // Get through the synchronous tests first.
            if (hasFruitInName() && hasValidChars()) {
                usernameTextInputLayout.setError(null);
                
                // Okay, the username contains valid text...now we have to query a web service 
                // to see if the username is available
                getUser(userName, new OnUserRetrievedListener() {
                  @Override
                  protected void onUserRetrievedListener(User user) {
                  
                    // User is null, so username must be available.
                    if (user == null) {
                    
                        // Let the user know the username is available.
                        usernameStatusTextView.setText(getString(R.string.success_available));
                        usernameStatusTextView.setTextColor(getColor(R.color.success_color));
                    } else {
       
                        // Let the user know the username is not available.
                        usernameStatusTextView.setText(getString(R.string.error_not_available));
                        usernameStatusTextView.setTextColor(getColor(R.color.error_color));
                    }
                  }
                });
            } else {
                usernameTextInputLayout.setError(getString(R.string.error_invalid_username));
                
                // Oh no! We have to add code to query the web service again.
                
                /***** STOP! *****/
            }
         }
     }
     
 });
 
boolean hasFruitInName(String username) {
 String[] fruit = {
      "apple",
      "banana",
      "blueberry",
      "kiwi",
      "orange",
      "strawberry"
 };
 Pattern pattern = Pattern.compile(TextUtils.join("|", fruit));
 Matcher matcher = pattern.matcher(view.getText().toString().toLowerCase());
      
 return matcher.find();
}

boolean hasValidChars(String username) {
 return Pattern.matches("^[a-zA-Z0-9]*$", userName)
}
```

Let's see if we can improve this at all...

##### Monitor an `````EditText````` and perform the asynchronous test first (*not as ugly, but room for improvement*)

```java
...
    @Override
     public void afterTextChanged(Editable s) {
         String input = s.toString();
         if (input.length() > 3) {
         
            getUser(userName, new OnUserRetrievedListener() {
               @Override
               protected void onUserRetrievedListener(User user) {
               
                 // User is null, so username must be available.
                 if (user == null) {
                    usernameStatusTextView.setText(getString(R.string.success_available));
                    usernameStatusTextView.setTextColor(getColor(R.color.success_color));
                    
                    if (hasFruitInName() && hasValidChars()) {
                        usernameTextInputLayout.setError(null);
                    } else {
                    
                        // Hid the username status TextView to not overwhelm user with messages.
                        usernameStatusTextView.setVisibility(View.INVISIBLE);
                        
                        usernameTextInputLayout.setError(getString(R.string.error_invalid_username));
                    }               
                 } else {
                     // Let the user know the username is not available.
                     usernameStatusTextView.setText(getString(R.string.error_not_available));
                     usernameStatusTextView.setTextColor(getColor(R.color.error_color));
                     
                     // Here we go again duplicating code...
                     
                     /***** STOP! *****/
                     
                     if (hasFruitInName() && hasValidChars()) {
                        ...
                     } else {
                        ...
                     }
                 }
               }
             });
           }
         }  
...
```

As you can see above, we're doing quite a bit of logical testing that has to be performed in a certain order. In the second code sample, we decided to perform our asynchronous user lookup first, and then check to see if the input meets the username requirements - ```hasFruitInName()``` and ```hasValidChars()```.

With a bit more encapsulation you could probably clean up the code a bit. Still, you're depending on sequential order of execution in order to provide the user with information they need to know (does their username contain valid characters *and* is it available).

Also, your Activity ends up doing a lot more work then it needs to be doing. It's already handling the [Activity Lifecycle](https://developer.android.com/guide/components/activities/activity-lifecycle.html)...why put the pressure on it to also validate data and update a bunch of views willy nilly?

## Separation of Responsibility

The goal of this project is to separate the responsibility of validation *from* the task of updating other views (or the view itself). It also alleviates the Activity from the responsibility of performing validation by allowing other more appropriate classes to handle the task.

Let's break down what's included in [view-validation-library](/view-validation-library)...

#### Condition
A condition is the most important piece of the view validation library. The ```Condition``` and ```AsyncCondition``` classes are responsible for evaluating individual tests. 

A ```Condition``` performs a single evaluation and returns the result (```true``` or ```false```) immediately. It is a synchronous operation.

```java
Criteria.Condition<EditText> criteria new Criteria.Condition<EditText>() {
    @Override
    public boolean evaluate(EditText view) {
    
        // Only accept alphanumeric characters.
        return Pattern.matches("^[a-zA-Z0-9]*$", view.getText().toString());
    }
};
```

In the above snippet, ```boolean evaluate(EditText view)``` is provided with an ```EditText view```. This is the ```View``` that's being validated. We don't specify where that's coming from yet, but you'll learn about it when the ```Criteria``` class is discussed. ```EditText``` is provided as a type argument to ensure we're receiving the appropriate ```View``` subclass.

> **Note:** **Condition** objects can be reused with different **Criteria** objects.

#### AsyncCondition
An ```AsynCondition``` has the same responsibility as a ```Criteria```, however it executes the supplied test on a separate ```Thread```. You would use an ```AsyncCondition``` if you needed to perform an asynchronous operation such as querying a web service. Performing a task like querying a web service could take "x" amount of time resulting in a poor user experience if executed on the main UI thread. You [don't want to do that](https://developer.android.com/guide/components/processes-and-threads.html).

```java
Criteria.AsyncCondition<EditText> usernameAvailAsyncCondition = new Criteria.AsyncCondition<EditText>() {

    @Override
    protected void evaluate(EditText view) {
    
        // Assume UserRepository queries a web service.
        UserRepository userRepository = new UserRepository();
        userRepository.getUser(view.getText().toString(), new UserRepository.OnuserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                // The username is available (returns true) if no user is found.
                complete(user == null);
            }
        });
    }

    @Override
    protected void onCancelled() {
        resetViews();
    }
};
```

You'll notice that ```evaluate(EditText view)``` function returns ```void``` instead of ```boolean```. That's because an asynchronous operation has nobody waiting to receive a value back. Instead, we call ```complete(user == null)```. The ```complete(boolean result)``` method notifies the ```Criteria``` object that the asynchronous operation completed. 

> **Note:** **AsyncCondition** objects can be reused with different **Criteria** objects.

#### Criteria
The ```Criteria``` class is responsible for managing a collection of ```Condition``` & ```AsyncCondition``` objects. When called upon, it also evaluates all of those objects and returns a single final result to the observers as a ```Validator.ValidationResult```.

```java
public enum ValidationResult {
    Valid,
    Invalid
}
```

```Condition``` objects can be added to the list of criteria via ```criteria.test(condition)```. ```AsyncContion``` objects can be added via ```criteria.asyncTest(asyncCondition)```

> **Note:** Both ```test(...)``` and ```asyncTest(...)``` return the instance of the ```Criteria``` object you are working with. As a result, you can chain as many tests as you need together.
```java
criteria.asyncTest(new Criteria.AsyncCondition<EditText>() { ... })
    .asyncTest(new Criteria.AsyncCondition<EditText>() { ... })
    .test(new Criteria.Condition<EditText>() { ... });
```

Using the ```AsyncCondition``` above, a criteria can be created like this...

```java
Criteria<EditText> criteria = new Criteria<EditText>(userNameEditText)
            .asyncTest(usernameAvailAsyncCondition);
```

Circling back around, the ```userNameEditText``` object supplied above is the view that's being validated. This is the same view supplied to ```boolean evaluate(EditText view)``` and ```void evaluate(EditText view)``` that must be overridden in ```Condition``` and ```AsyncCondition```, respectively.

#### Validator
A ```Validator``` is the creature you'll be interacting with the most. Which isn't all the much, considering there are only two methods - ```validate()``` and ```cancelValidation()```.

To create a ```Validator``` you supply it with a ```Criteria``` object. 

```java
Validator<EditText> userNameAvailableValidator = new Validator<>(criteria);
```

Validators act as a mediator between the *view being validated* and the observers...

#### Observer
Here's where the true *separation of responsibility* comes in to play. ```Observer``` objects contain a single ```View```. The views they contain are ones that want to be updated based upon whether the *view being validated* is valid...or invalid. 

Continuing with our example, suppose the ```usernameAvailAsyncCondition``` reports back that the username isn't taken by an existing user. Meaning, the ```EditText``` view where we're capturing user input, is in a *valid* state. In this case, we could display a message in a ```TextView``` informing the user that the username is available.

On the flip side, a message could display stating the username isn't available.

```java
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
```

Observers are added to the validator object like so:
```java
userNameAvailableValidator.observe(userNameStatusObserver, ..., ...);
```

> **Note:** **Observer** objects can be reused with different **Validator** objects.

#### Break it Down
***
The ```validate()``` method kicks everything off...
1. It requests the ```Criteria``` object to ask each of its conditions to test themselves. 
2. Each condition object reports back whether they passed (true or false).
3. After all conditions have been evaluated, the final result ```Validator.ValidationResult.Valid``` or ```Validator.ValidationResult.Invalid``` is reported to all the observers.
4. Each observer does what it needs to with the view associated with it based on the result.

Here's what everything combines could look like...

```java
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        
        // Views will be available in the onCreate(...) method. For cleanliness, move to another method.
        initFormValidation();
        ...
    }
    
    ...
    
    private void initFormValidation() {
        EditText userNameEditText = (EditText) findViewById(R.id.user_name);
        final Validator<EditText> userNameAvailableValidator = new Validator<EditText>(
                new Criteria<EditText>(userNameEditText)
                        .asyncTest(new Criteria.AsyncCondition<EditText>() {
        
                            @Override
                            protected void evaluate(EditText view) {
        
                                // Assume UserRepository queries a web service.
                                UserRepository userRepository = new UserRepository();
                                userRepository.getUser(view.getText().toString(), new UserRepository.OnuserRetrievedListener() {
                                    @Override
                                    public void onUserRetrieved(User user) {
                                        // The username is available (returns true) if no user is found.
                                        complete(user == null);
                                    }
                                });
                            }
        
                            @Override
                            protected void onCancelled() {
                                resetViews();
                            }
                        }) 
        );
        
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
        
        // Add the observer
        userNameAvailableValidator.observe(userNameStatusObserver);
        
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
                    userNameAvailableValidator.validate();
                } else {
                    userNameAvailableValidator.cancelValidation();
                    
                    // Reset the views if the input is not at least 4 characters.
                    resetViews();
                }
            }
        });
}
...
````

The message ```userNameAvailableValidator.cancelValidation()``` will cancel any currently running asynchronous operations. This is called to prevent a valid/invalid response back *after* more recent request has already returned.

## Summary
The end result allows for an *expressive* way to perform validation. The task of validating conditions is isolated and granular. The task of observers updating their state is kept separate. 

Adding and removing an element from any part of the process is straightforward:
>- Add/remove an observer
>- Add/remove/change a condition
>- Add a new validator

The other point I look about this approach is that we don't extend any of Android's ```View``` subclasses. We can handle any ```View``` as a type argument supplied to the validator and observer classes. 

On the downside...it becomes a bit verbose. If needed, a separate class could be created.