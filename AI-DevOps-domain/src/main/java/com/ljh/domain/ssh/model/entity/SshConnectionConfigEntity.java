package com.ljh.domain.ssh.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSH连接高级配置实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SshConnectionConfigEntity {

    private Long id;
    private String connectionId;
    private Integer connectTimeout;
    private Integer keepaliveInterval;
    // 连接建立后自动执行的启动命令
    private String startupCommand;
    // 是否启用压缩，默认false
    private Boolean compression;
    // 是否严格校验 hostKey,默认true
    private Boolean strictHostKeyCheck;
    // 已知主机指纹信息
    private String knownHosts;
    private java.time.LocalDateTime updatedAt;

    /**
     * 设置默认值
     */
    public SshConnectionConfigEntity withDefaults() {
        if (connectTimeout == null) connectTimeout = 10;
        if (keepaliveInterval == null) keepaliveInterval = 60;
        if (compression == null) compression = false;
        if (strictHostKeyCheck == null) strictHostKeyCheck = true;
        return this;
    }

}
