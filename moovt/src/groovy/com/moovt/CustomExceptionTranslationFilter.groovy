package com.moovt;

import grails.converters.JSON
import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.security.web.util.ThrowableCauseExtractor;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * Handles any <code>AccessDeniedException</code> and <code>AuthenticationException</code> thrown within the
 * filter chain.
 * <p>
 * This filter is necessary because it provides the bridge between Java exceptions and HTTP responses.
 * It is solely concerned with maintaining the user interface. This filter does not do any actual security enforcement.
 * <p>
 * If an {@link AuthenticationException} is detected, the filter will launch the <code>authenticationEntryPoint</code>.
 * This allows common handling of authentication failures originating from any subclass of
 * {@link org.springframework.security.access.intercept.AbstractSecurityInterceptor}.
 * <p>
 * If an {@link AccessDeniedException} is detected, the filter will determine whether or not the user is an anonymous
 * user. If they are an anonymous user, the <code>authenticationEntryPoint</code> will be launched. If they are not
 * an anonymous user, the filter will delegate to the {@link org.springframework.security.web.access.AccessDeniedHandler}.
 * By default the filter will use {@link org.springframework.security.web.access.AccessDeniedHandlerImpl}.
 * <p>
 * To use this filter, it is necessary to specify the following properties:
 * <ul>
 * <li><code>authenticationEntryPoint</code> indicates the handler that
 * should commence the authentication process if an
 * <code>AuthenticationException</code> is detected. Note that this may also
 * switch the current protocol from http to https for an SSL login.</li>
 * <li><tt>requestCache</tt> determines the strategy used to save a request during the authentication process in order
 * that it may be retrieved and reused once the user has authenticated. The default implementation is
 * {@link HttpSessionRequestCache}.</li>
 * </ul>
 *
 * @author Ben Alex
 * @author colin sampaleanu
 */
public class CustomExceptionTranslationFilter extends GenericFilterBean {

    //~ Instance fields ================================================================================================

    private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();

    //~ Methods ========================================================================================================

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        try {
            chain.doFilter(request, response);

            if (logger.isDebugEnabled()) {
                logger.debug("Chain processed normally");
            }
        }
        catch (IOException ex) {
            throw ex;
        }
        catch (Exception ex) {
            // Try to extract a SpringSecurityException from the stacktrace
            Throwable[] causeChain = throwableAnalyzer.determineCauseChain(ex);
            def ase = throwableAnalyzer.getFirstThrowableOfType(AuthenticationException.class, causeChain);

            if (ase == null) {
                ase = (AccessDeniedException)throwableAnalyzer.getFirstThrowableOfType(AccessDeniedException.class, causeChain);
            }

            if (ase != null) {
              		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					  def error = ['code':'ERROR','msg':'Not Authorized']
					response.outputStream << "${error as JSON}"

            } else {
                // Rethrow ServletExceptions and RuntimeExceptions as-is
                if (ex instanceof ServletException) {
                    throw (ServletException) ex;
                }
                else if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }

                // Wrap other Exceptions. These are not expected to happen
                throw new RuntimeException(ex);
            }
        }
    }

    public void setThrowableAnalyzer(ThrowableAnalyzer throwableAnalyzer) {
        Assert.notNull(throwableAnalyzer, "throwableAnalyzer must not be null");
        this.throwableAnalyzer = throwableAnalyzer;
    }

    /**
     * The RequestCache implementation used to store the current request before starting authentication.
     * Defaults to an {@link HttpSessionRequestCache}.
     */
    public void setRequestCache(RequestCache requestCache) {
        Assert.notNull(requestCache, "requestCache cannot be null");
        this.requestCache = requestCache;
    }

    /**
     * Default implementation of <code>ThrowableAnalyzer</code> which is capable of also unwrapping
     * <code>ServletException</code>s.
     */
    private static final class DefaultThrowableAnalyzer extends ThrowableAnalyzer {
        /**
         * @see org.springframework.security.web.util.ThrowableAnalyzer#initExtractorMap()
         */
        protected void initExtractorMap() {
            super.initExtractorMap();

            registerExtractor(ServletException.class, new ThrowableCauseExtractor() {
                public Throwable extractCause(Throwable throwable) {
                    ThrowableAnalyzer.verifyThrowableHierarchy(throwable, ServletException.class);
                    return ((ServletException) throwable).getRootCause();
                }
            });
        }

    }

}