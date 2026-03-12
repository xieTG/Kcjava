package com.xietg.kc.testsupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.lang.reflect.Method;
import java.util.Optional;

public class ConsoleTestReporter implements TestWatcher {

    private String description(ExtensionContext context) {
        Optional<Method> testMethod = context.getTestMethod();
        if (testMethod.isPresent()) {
            DisplayName dn = testMethod.get().getAnnotation(DisplayName.class);
            if (dn != null && !dn.value().isBlank()) {
                return dn.value();
            }
        }
        return context.getDisplayName();
    }

    private void print(ExtensionContext context, String result, String reason) {
        System.out.println("--------------------------------------------------");
        System.out.println("Test: " + context.getRequiredTestClass().getSimpleName() + "." + context.getRequiredTestMethod().getName());
        System.out.println("Description: " + description(context));
        System.out.println("Result: " + result);
        System.out.println("Reason: " + reason);
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        print(context, "OK", "-");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        String reason = cause == null ? "Unknown failure" : cause.getClass().getSimpleName() + ": " + cause.getMessage();
        print(context, "KO", reason);
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        String reason = cause == null ? "Test aborted" : cause.getClass().getSimpleName() + ": " + cause.getMessage();
        print(context, "ABORTED", reason);
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
        print(context, "DISABLED", reason.orElse("-"));
    }
}
