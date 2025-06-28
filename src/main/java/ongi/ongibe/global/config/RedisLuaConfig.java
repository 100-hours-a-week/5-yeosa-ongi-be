package ongi.ongibe.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

@Configuration
public class RedisLuaConfig {

    @Bean(name = "likeScript")
    public DefaultRedisScript<Long> likeScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/like_album.lua")));
        script.setResultType(Long.class);
        return script;
    }

    @Bean(name = "unlikeScript")
    public DefaultRedisScript<Long> unlikeScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/unlike_album.lua")));
        script.setResultType(Long.class);
        return script;
    }
}

