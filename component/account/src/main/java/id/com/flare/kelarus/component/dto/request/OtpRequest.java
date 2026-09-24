package id.com.flare.kelarus.component.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import id.com.flare.common.utilities.network.http.HttpRequestDetails;
import id.com.flare.kelarus.component.constant.DeliveryTypeEnum;
import id.com.flare.kelarus.component.constant.LoginTypeEnum;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequest implements Serializable {

    @NotBlank
    String identifier;

    @NotNull
    LoginTypeEnum loginTypeEnum;

    @NotNull
    DeliveryTypeEnum deliveryType;

    @JsonIgnore
    @Transient
    private HttpRequestDetails httpDetail;

}
