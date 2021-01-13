package com.csii.pe.gateway.common;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.RoundRobinRule;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.Server.MetaInfo;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
 
/**
 * 
 * @ClassName: GameCenterBalanceRule
 * @Description: 根据userId对服务进行负载均衡。同一个用户id的请求，都转发到同一个服务实例上面。
 * @author: wgs
 * @date: 2019年3月15日 下午2:17:06
 */
public class CustomBalanceRule extends RoundRobinRule {
	@Autowired
	private ConfigFields configFields;
    @Override
    public Server choose(Object key) {//这里的key就是过滤器中传过来的userId
    	boolean grayFlag = configFields.getGrayFlag();
    	String grayValue = configFields.getGrayValue();
        List<Server> servers = this.getLoadBalancer().getReachableServers();
        if (grayFlag&&grayValue!=null&&key!=null&&grayValue.contains(key.toString())&&servers!=null&&servers.size()>0) {
            Server server = chooseServer(servers);
            if(server!=null)
            	return server;
            else
            	return super.choose(key);
        }
        return super.choose(key);
    }
    private Server chooseServer(List<Server> servers){
    	String grayVersion = configFields.getGrayVersion();
    	Server server = null;
    	for(int i = 0;i<servers.size();i++){
    		Server tempServer = servers.get(i);
    		Map<String, String> metadata = ((DiscoveryEnabledServer) tempServer).getInstanceInfo().getMetadata();
    		String version = metadata.get("version");
    		if(version!=null&&version.equals(grayVersion)){
    			server = tempServer;
    			break;
    		}
    	}
    	return server;
    }
	@Override
	public void initWithNiwsConfig(IClientConfig arg0) {
		// TODO Auto-generated method stub
		
	}
}