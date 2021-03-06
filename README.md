# Android View Validator + Observer Views + Java 8


This project is identical to the [Java 7 version](https://github.com/bennylm/android-view-validator), except where Java 8 features can be used. The differences are included below, separated by class.

> **Note:** Several [Java 8 language features](https://developer.android.com/studio/write/java8-support.html) are included in [Android Studio Preview (3.0)](https://developer.android.com/studio/preview/index.html). You can install a preview version of Android Studio [alongside the stable version]((https://developer.android.com/studio/preview/install-preview.html)) to explore new features. Any module that includes Java 8 features will require additional configuration in the module's **build.gradle** file.

```
android {
  ...
  // Configure only for each module that uses Java 8
  // language features (either in its source code or
  // through dependencies).
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}

```

### [Criteria](view-validation-library/src/main/java/io/launchowl/viewvalidationlibrary/Criteria.java)

#### Before
```java
final void initEvaluate(final Criteria criteria, final T view) {
    ...
    this.thread = new Thread(new Runnable() {
        @Override
        public void run() {
            evaluate(view);
        }
    });
    thread.start();
}
```

#### After
[Lambda Expression](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) 
```java
final void initEvaluate(final Criteria criteria, final T view) {
    ...
    this.thread = new Thread(() -> evaluate(view));
    this.thread.start();
}
```

---

#### Before
```java
void evaluateConditions() {
    for (Condition<T> condition : this.conditions) {
        setValidationResult(condition.evaluate(this.validatedView));
    }
}
```

#### After
[Iterable.forEach](https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html#forEach-java.util.function.Consumer-)
```java
void evaluateConditions() {
    this.conditions.forEach(condition -> setValidationResult(condition.evaluate(this.validatedView)));
}
```

---

#### Before
```java
void cancelValidation() {
    for (AsyncCondition<T> asyncCondition : this.asyncConditions) {
        asyncCondition.cancel();
    }
}
```

#### After
[Iterable.forEach](https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html#forEach-java.util.function.Consumer-), [Method Reference](https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html)
```java
void cancelValidation() {
    this.asyncConditions.forEach(AsyncCondition::cancel);
}
```

### [Validator](view-validation-library/src/main/java/io/launchowl/viewvalidationlibrary/Validator.java)

#### Before
```java
public void validate() {
    this.criteria.evaluate(new Criteria.EvalCompleteListener() {
        @Override
        public void onComplete(ValidationResult validationResult) {
            Notifier.notify(observers, validationResult);
        }
    });
}
```

#### After
[Lambda Expression](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html) 
```java
public void validate() {
    this.criteria.evaluate(validationResult -> Notifier.notify(observers, validationResult));
}
```

---

#### Before
```java
public void validate() {
    this.criteria.evaluate(new Criteria.EvalCompleteListener() {
        @Override
        public void onComplete(ValidationResult validationResult) {
            Notifier.notify(observers, validationResult);
        }
    });
}
```

#### After
[Iterable.forEach](https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html#forEach-java.util.function.Consumer-)
```java
private static class Notifier {
     static void notify(Set<Observer> observers, ValidationResult validationResult) {
         observers.forEach(observer -> observer.update(validationResult));
    }
}
```

---

### [ValidatorSet](view-validation-library/src/main/java/io/launchowl/viewvalidationlibrary/ValidatorSet.java)

#### Before
```java
public void validate() {
    for (Validator validator : validators) {
        validator.validate();
    }
}
```

#### After
[Iterable.forEach](https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html#forEach-java.util.function.Consumer-), [Method Reference](https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html)
```java
public void validate() {
    this.validators.forEach(Validator::validate);
}
```

---

#### Before
```java
public void cancelValidation() {
    for (Validator validator : validators) {
        validator.cancelValidation();
    }
}
```

#### After
[Iterable.forEach](https://docs.oracle.com/javase/8/docs/api/java/lang/Iterable.html#forEach-java.util.function.Consumer-), [Method Reference](https://docs.oracle.com/javase/tutorial/java/javaOO/methodreferences.html)
```java
public void cancelValidation() {
    this.validators.forEach(Validator::cancelValidation);
}
```