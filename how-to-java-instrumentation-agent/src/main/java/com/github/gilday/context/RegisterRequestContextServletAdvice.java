package com.github.gilday.context;

import com.github.gilday.bootstrap.ServiceLocator;
import com.github.gilday.bootstrap.context.RequestContext;
import net.bytebuddy.asm.Advice.OnMethodEnter;
import net.bytebuddy.asm.Advice.OnMethodExit;

/**
 * Servlet decorator which creates a new {@link RequestContext} before a request is serviced, and destroys that context
 * at the end of a request
 */
public class RegisterRequestContextServletAdvice {

    private RegisterRequestContextServletAdvice() { }

    @OnMethodEnter static void enter() {
        ServiceLocator.requestContextManager.create();
    }

    @OnMethodExit static void exit() {
        ServiceLocator.requestContextManager.close();
    }
}
