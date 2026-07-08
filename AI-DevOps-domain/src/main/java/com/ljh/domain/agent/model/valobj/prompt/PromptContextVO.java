package com.ljh.domain.agent.model.valobj.prompt;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PromptContextVO {
    /** 系统环境信息（实时状态） */
    private String serverInfo;        // 服务器标识
    private String osInfo;            // 操作系统信息
    private String currentUser;       // 当前登录用户
    private String currentDirectory;  // 当前工作目录

    /** 工程信息（项目上下文） */
    private String projectName;       // 当前工程名称（如 "ai-mcp-gateway"）
    private String projectRootPath;   // 当前工程根路径（如 "/Users/xxx/coding/ai-mcp-gateway"）

    /**
     *  recentCommands = [
     *     "ls -la src/main/java",
     *     "git status",
     *     "mvn compile"
     * ]
     */
    private List<String> recentCommands;    // 历史命令

    /**
     * - [TASK_COMPLETE] 成功部署v2.3.0到生产环境
     * - [ERROR_OCCURRED] 数据库连接超时
     * - [TASK_START] 开始配置Nginx
     */
    private List<MilestoneVO> milestoneVOS;   // 关键事件里程碑

    /**
     * 原始工具输出（可能很长）
     * ls -la /var/log
     * total 2048
     * -rw-r--r-- 1 root root 1048576 Jan 15 10:23 app.log
     * -rw-r--r-- 1 root root 1048576 Jan 14 08:45 app.log.1
     *  ... 还有50行
     * // 摘要后
     * toolResultSummary = "在/var/log目录下找到2个日志文件，总大小约2MB"
     */
    private String toolResultSummary;  //将上一步工具执行的结果压缩成摘要，传递给AI作为上下文
    
    private String taskDescription; // 记录用户最初的任务意图

    // 核心记忆（由 CoreMemoryProvider 注入，XML 格式）
    private String coreMemories;

    // SSH 连接信息（由 TerminalStateProvider 注入）
    /** SSH 连接名称 */
    private String sshConnectionName;
    /** SSH 主机地址 */
    private String sshHost;
    /** SSH 端口 */
    private Integer sshPort;
    /** SSH 用户名 */
    private String sshUsername;
    /** SSH 连接 ID */
    private String sshConnectionId;
}
