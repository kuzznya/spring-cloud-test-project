package ru.intetech.mnemosynesystem.cloud.eureka;

import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl;
import com.netflix.eureka.resources.ServerCodecs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Primary
@Slf4j
class CustomPeerAwareInstanceRegistryImpl extends PeerAwareInstanceRegistryImpl {

    private final EurekaInstanceConfig instanceConfig;

    private final Set<String> ignored = new HashSet<>();

    public CustomPeerAwareInstanceRegistryImpl(EurekaServerConfig serverConfig,
                                               EurekaClientConfig clientConfig,
                                               ServerCodecs codecs,
                                               @Qualifier("eurekaClient") EurekaClient client,
                                               EurekaInstanceConfig instanceConfig) {
        super(serverConfig, clientConfig, codecs, client);
        this.instanceConfig = instanceConfig;
        log.info("Using custom PeerAwareInstanceRegistry");
    }

    @Override
    public void register(InstanceInfo instanceInfo, boolean isReplication) {
        if (!isRegisterable(instanceInfo))
            return;

        super.register(instanceInfo, isReplication);
    }

    @Override
    public boolean renew(String appName, String id, boolean isReplication) {
        if (ignored.contains(id))
            return true;
        return super.renew(appName, id, isReplication);
    }

    @Override
    public boolean isRegisterable(InstanceInfo instanceInfo) {
        Set<String> regions = Arrays.stream(Optional
                .ofNullable(clientConfig.fetchRegistryForRemoteRegions())
                .map(s -> s.split(","))
                .orElse(new String[0]))
                .map(String::strip)
                .filter(region -> Arrays
                        .stream(clientConfig.getAvailabilityZones(region))
                        .anyMatch(zone -> zone.equals(instanceInfo.getMetadata().get("zone"))))
                .collect(Collectors.toSet());

        if (instanceConfig
                .getMetadataMap()
                .containsKey("zone") && instanceConfig
                .getMetadataMap()
                .get("zone")
                .equals(instanceInfo.getMetadata().get("zone"))) {
            regions.add(clientConfig.getRegion());
        }

        boolean isInWhiteList = regions.stream()
                .anyMatch(region -> region.equals(clientConfig.getRegion()) || Optional
                        .ofNullable(serverConfig.getRemoteRegionAppWhitelist(region))
                        .orElse(Collections.emptySet())
                        .contains(instanceInfo.getAppName()));

        if (!isInWhiteList) {
            log.debug("App {} is not in white list, therefore it will be skipped", instanceInfo.getAppName());
            ignored.add(instanceInfo.getInstanceId());
            return false;
        }
        log.debug("App {} is allowed to be registered", instanceInfo.getAppName());

        return super.isRegisterable(instanceInfo);
    }
}
