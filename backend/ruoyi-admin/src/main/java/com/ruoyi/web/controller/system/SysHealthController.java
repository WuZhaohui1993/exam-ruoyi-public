package com.ruoyi.web.controller.system;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * 轻量健康检查。
 */
@RestController
@RequestMapping("/health")
public class SysHealthController
{
    private final JdbcTemplate jdbcTemplate;
    private final RedisTemplate<Object, Object> redisTemplate;

    @Value("${ruoyi.version}")
    private String version;

    public SysHealthController(JdbcTemplate jdbcTemplate, RedisTemplate<Object, Object> redisTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping
    public AjaxResult health()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("status", "UP");
        data.put("version", version);
        data.put("database", checkDatabase());
        data.put("redis", checkRedis());
        return AjaxResult.success(data);
    }

    private String checkDatabase()
    {
        try
        {
            Integer result = jdbcTemplate.queryForObject("select 1", Integer.class);
            return result != null && result == 1 ? "UP" : "DOWN";
        }
        catch (Exception e)
        {
            return "DOWN";
        }
    }

    private String checkRedis()
    {
        try
        {
            redisTemplate.hasKey("__health_check__");
            return "UP";
        }
        catch (Exception e)
        {
            return "DOWN";
        }
    }
}
