package com.ljh.infrastructure.dao;

import com.ljh.infrastructure.dao.po.SshConnectionConfigPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * SSH连接高级配置DAO
 */
@Mapper
public interface ISshConnectionConfigDAO {

    void insertOrUpdate(SshConnectionConfigPO po);

    SshConnectionConfigPO queryByConnectionId(String connectionId);

}
