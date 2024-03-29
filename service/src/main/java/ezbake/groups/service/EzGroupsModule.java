/*   Copyright (C) 2013-2014 Computer Sciences Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package ezbake.groups.service;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.thinkaurelius.titan.core.TitanGraph;
import ezbake.common.properties.EzProperties;
import ezbake.groups.graph.api.GraphProvider;
import ezbake.groups.graph.api.GroupIDProvider;
import ezbake.groups.graph.api.GroupIDPublisher;
import ezbake.groups.graph.impl.RedisIDProvider;
import ezbake.groups.graph.impl.TitanGraphIDPublisher;
import ezbake.groups.graph.impl.TitanGraphProvider;
import ezbakehelpers.ezconfigurationhelpers.redis.RedisConfigurationHelper;
import ezbakehelpers.ezconfigurationhelpers.zookeeper.ZookeeperConfigurationHelper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * User: jhastings
 * Date: 6/16/14
 * Time: 11:17 AM
 */
public class EzGroupsModule extends AbstractModule {


    private EzProperties ezProperties;

    public EzGroupsModule(EzProperties ezProperties) {
        this.ezProperties = ezProperties;
    }

    @Override
    protected void configure() {
        bind(new TypeLiteral<GraphProvider<TitanGraph>>(){}).to(TitanGraphProvider.class);
        bind(TitanGraph.class).toProvider(TitanGraphProvider.class);
        bind(GroupIDProvider.class).to(RedisIDProvider.class);
        bind(GroupIDPublisher.class).to(TitanGraphIDPublisher.class);
    }

    @Provides
    public Properties provideEzProperties() {
        return ezProperties;
    }

    @Provides
    public JedisPool provideJedis() {
        RedisConfigurationHelper rc = new RedisConfigurationHelper(ezProperties);
        return new JedisPool(new JedisPoolConfig(), rc.getRedisHost(), rc.getRedisPort());
    }

    @Provides
    public CuratorFramework provideCurator() {
        ZookeeperConfigurationHelper zc = new ZookeeperConfigurationHelper(ezProperties);
        return CuratorFrameworkFactory.builder()
                .connectString(zc.getZookeeperConnectionString())
                .retryPolicy(new RetryNTimes(5, 1000))
                .build();
    }
}
