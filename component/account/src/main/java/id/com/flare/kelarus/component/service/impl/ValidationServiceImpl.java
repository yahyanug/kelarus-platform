package id.com.flare.kelarus.component.service.impl;


import id.com.flare.kelarus.component.constant.OtpTypeEnum;
import id.com.flare.kelarus.component.service.ValidationService;
import id.com.flare.kelarus.utilities.general.constant.ErrorCodeGlobalEnum;
import id.com.flare.kelarus.utilities.general.constant.GlobalSystemConfigurationEnum;
import id.com.flare.kelarus.utilities.general.exception.GlobalBusinessException;
import id.com.flare.kelarus.utilities.general.outbound.FeatureFlagOutbound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {

    private final FeatureFlagOutbound featureFlagOutbound;

    @Override
    public void validateOTPRequestKey(OtpTypeEnum otpTypeEnum) {
        String allowKeysStr = getGlobalSysConfigByCode(GlobalSystemConfigurationEnum.ALLOWED_REQUEST_KEY_TYPES_FOR_OTP);
        Set<OtpTypeEnum> allowKeys = Arrays.stream(allowKeysStr.split(",")).map(String::trim)
                .map(OtpTypeEnum::valueOf).collect(Collectors.toSet());
        if (!allowKeys.contains(otpTypeEnum)) {
            throw new GlobalBusinessException(ErrorCodeGlobalEnum.INVALID_PASSCODE_VERIFICATION_REQUEST_TYPE, allowKeys.toString());
        }
    }

    private String getGlobalSysConfigByCode(GlobalSystemConfigurationEnum configurationEnum) {
        try {
            return featureFlagOutbound.getStringValue(configurationEnum);
        }
        catch (Exception e) {
            log.error("Error fetching global system configuration for code: {}", configurationEnum.getCode(), e);
            throw new IllegalStateException("Unable to read global configuration.", e);
        }
    }
}
