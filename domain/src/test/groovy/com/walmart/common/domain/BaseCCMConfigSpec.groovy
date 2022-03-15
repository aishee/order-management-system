package com.walmart.common.domain;

import com.walmart.common.config.BaseCCMConfig
import io.strati.configuration.context.ConfigurationContext
import io.strati.configuration.listener.ChangeLog;
import spock.lang.Specification;
import io.strati.configuration.listener.ChangeType;

class BaseCCMConfigSpec extends Specification {

    BaseCCMConfig baseCCMConfig
    ConfigurationContext configurationContext = Mock()

    def setup() {
        baseCCMConfig = {-> return "ccm-name" }
    }

    def "configChanged executed successfully"() {
        given:
        String configName = "ccm-name";

        List<ChangeLog> changeLogs = new ArrayList<>();
        changeLogs.add(new ChangeLog(ChangeType.UPDATED, "key", "oldValue", "newValue"));
        changeLogs.add(new ChangeLog(ChangeType.ADDED, "key", "oldValue", "newValue"));

       when :
        baseCCMConfig.configChanged(configName, changeLogs, configurationContext);

        then :

        baseCCMConfig.getConfigName()!=null
        baseCCMConfig.getConfigName()==configName
    }

    def "configChanged called but configname does not match"() {
        given:
        String configName = "other-ccm-name";

        List<ChangeLog> changeLogs = new ArrayList<>();
        changeLogs.add(new ChangeLog(ChangeType.UPDATED, "key", "oldValue", "newValue"));
        changeLogs.add(new ChangeLog(ChangeType.ADDED, "key", "oldValue", "newValue"));

        when :
        baseCCMConfig.configChanged(configName, changeLogs, configurationContext);

        then :

        baseCCMConfig.getConfigName()!=null
        baseCCMConfig.getConfigName()!=configName
    }

}
