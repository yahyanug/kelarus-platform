package id.com.flare.kelarus.component.service;

import id.com.flare.kelarus.component.constant.OtpTypeEnum;

public interface ValidationService {

    void validateOTPRequestKey(OtpTypeEnum otpTypeEnum);

}
