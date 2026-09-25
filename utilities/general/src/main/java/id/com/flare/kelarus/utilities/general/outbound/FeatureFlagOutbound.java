package id.com.flare.kelarus.utilities.general.outbound;

import id.com.flare.kelarus.utilities.general.constant.GlobalSystemConfigurationEnum;

public interface FeatureFlagOutbound {

	boolean isFeatureEnabled(GlobalSystemConfigurationEnum globalSystemConfigurationEnum);

	int getIntegerValue(GlobalSystemConfigurationEnum globalSystemConfigurationEnum);

	String getStringValue(GlobalSystemConfigurationEnum globalSystemConfigurationEnum);

}
