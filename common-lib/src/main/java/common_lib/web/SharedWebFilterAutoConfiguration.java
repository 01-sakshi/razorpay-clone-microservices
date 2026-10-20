package common_lib.web;

import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.RequestContextFilter;

@AutoConfiguration
public class SharedWebFilterAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.security.trust-inbound-headers", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<Filter> merchantContextRegistration(MerchantContextFilter merchantContextFilter) {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>(merchantContextFilter);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registrationBean;
    }

    //For merchantContext bean having request scope, this FilterRegistrationBean
    //will help to propagate the scope from api-gateway service to downstream service(common-lib)
    @Bean
    public FilterRegistrationBean<Filter> requestContextFilterRegistration() {
        FilterRegistrationBean<Filter> registration =
                new FilterRegistrationBean<>(new RequestContextFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
