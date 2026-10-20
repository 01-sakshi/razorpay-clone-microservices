package common_lib.web;

import common_lib.context.MerchantContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SharedMerchantContextFilterAutoConfiguration {

    @Bean
    public MerchantContextFilter merchantContextFilter(MerchantContext merchantContext) {
        return new MerchantContextFilter(merchantContext);
    }
}
