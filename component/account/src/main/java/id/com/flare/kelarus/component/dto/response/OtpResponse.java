package id.com.flare.kelarus.component.dto.response;

import id.com.flare.kelarus.component.constant.DeliveryTypeEnum;
import id.com.flare.kelarus.component.constant.OtpTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Instant;

@Slf4j
@Data
public class OtpResponse implements Serializable {

	private String key;

	private String destination;

	private DeliveryTypeEnum deliveryType;

	private OtpTypeEnum otpType;

	private Integer expiresIn;

	private Instant expiresAt;

	private Integer resendAfter;

}
