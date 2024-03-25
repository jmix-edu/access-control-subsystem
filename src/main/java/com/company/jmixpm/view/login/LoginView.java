package com.company.jmixpm.view.login;

import com.company.jmixpm.view.public_.register.UserRegistrationView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.AbstractLogin.LoginEvent;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.CoreProperties;
import io.jmix.core.MessageTools;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.component.loginform.JmixLoginForm;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.loginform.JmixLoginI18n;
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.authentication.AuthDetails;
import io.jmix.securityflowui.authentication.LoginViewSupport;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "login")
@ViewController("LoginView")
@ViewDescriptor("login-view.xml")
public class LoginView extends StandardView implements LocaleChangeObserver {
    private static final Logger log = LoggerFactory.getLogger(LoginView.class);

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    private LoginViewSupport loginViewSupport;

    @Autowired
    private MessageBundle messageBundle;

    @Autowired
    private MessageTools messageTools;

    @Autowired
    private ViewNavigators viewNavigators;

    @ViewComponent
    private JmixLoginForm login;

    @Value("${ui.login.defaultUsername:}")
    private String defaultUsername;

    @Value("${ui.login.defaultPassword:}")
    private String defaultPassword;

    @Subscribe
    public void onInit(final InitEvent event) {
        initLocales();
        initDefaultCredentials();
    }

    protected void initLocales() {
        LinkedHashMap<Locale, String> locales = coreProperties.getAvailableLocales().stream()
                .collect(Collectors.toMap(Function.identity(), messageTools::getLocaleDisplayName, (s1, s2) -> s1,
                        LinkedHashMap::new));

        ComponentUtils.setItemsMap(login, locales);

        login.setSelectedLocale(VaadinSession.getCurrent().getLocale());
    }

    protected void initDefaultCredentials() {
        if (StringUtils.isNotBlank(defaultUsername)) {
            login.setUsername(defaultUsername);
        }

        if (StringUtils.isNotBlank(defaultPassword)) {
            login.setPassword(defaultPassword);
        }
    }

    @Subscribe("login")
    public void onLogin(final LoginEvent event) {
        try {
            loginViewSupport.authenticate(
                    AuthDetails.of(event.getUsername(), event.getPassword())
                            .withLocale(login.getSelectedLocale())
                            .withRememberMe(login.isRememberMe())
            );
        } catch (final BadCredentialsException | DisabledException | LockedException |
                       AccessDeniedException | AccountExpiredException e) {
            if (e instanceof AccountExpiredException) {
                JmixLoginI18n loginI18n = createLoginI18n();
                loginI18n.getErrorMessage().setMessage(e.getMessage());
                login.setI18n(loginI18n);
            } else {
                login.setI18n(createLoginI18n());
            }
            log.warn("Login failed for user '{}': {}", event.getUsername(), e.toString());
            event.getSource().setError(true);
        }
    }

    @Override
    public void localeChange(final LocaleChangeEvent event) {
        UI.getCurrent().getPage().setTitle(messageBundle.getMessage("LoginView.title"));

        login.setI18n(createLoginI18n());
    }

    private JmixLoginI18n createLoginI18n() {
        final JmixLoginI18n loginI18n = JmixLoginI18n.createDefault();

        final JmixLoginI18n.JmixForm form = new JmixLoginI18n.JmixForm();
        form.setTitle(messageBundle.getMessage("loginForm.headerTitle"));
        form.setUsername(messageBundle.getMessage("loginForm.username"));
        form.setPassword(messageBundle.getMessage("loginForm.password"));
        form.setSubmit(messageBundle.getMessage("loginForm.submit"));
        form.setForgotPassword(messageBundle.getMessage("loginForm.forgotPassword"));
        form.setRememberMe(messageBundle.getMessage("loginForm.rememberMe"));
        loginI18n.setForm(form);

        final LoginI18n.ErrorMessage errorMessage = new LoginI18n.ErrorMessage();
        errorMessage.setTitle(messageBundle.getMessage("loginForm.errorTitle"));
        errorMessage.setMessage(messageBundle.getMessage("loginForm.badCredentials"));
        errorMessage.setUsername(messageBundle.getMessage("loginForm.errorUsername"));
        errorMessage.setPassword(messageBundle.getMessage("loginForm.errorPassword"));
        loginI18n.setErrorMessage(errorMessage);

        return loginI18n;
    }

    @Subscribe("login")
    public void onLoginForgotPassword(final AbstractLogin.ForgotPasswordEvent event) {
        viewNavigators.view(UserRegistrationView.class)
                .navigate();
    }
}
