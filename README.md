## 平台简介
基于若依开发的SSE AI问答系统是一套全部开源的快速开发平台。
* 前端采用Vue、Element UI。
* 后端采用Spring Boot、Spring Security、Redis & Jwt。
* 权限认证使用Jwt，支持多终端认证系统。
* 支持加载动态权限菜单，多方式轻松权限控制。
* 高效率开发，使用代码生成器可以一键生成前后端代码。

## SSE是什么？
Server-Sent Events (SSE) 是一种服务器推送技术，允许服务器向客户端推送数据。与WebSocket不同，SSE是单向的(只能服务器向客户端推送)，但它的优势在于:
* 1.基于HTTP协议，实现简单
* 2.自动重连机制
* 3.轻量级，资源消耗少
* 4.天然支持文本传输，特别适合流式数据

## SSE 与相关技术对比
<img src="https://luochengbin.com/static/img/sse/contrast.png"/>

## SSE工作流程图：
<img src="https://luochengbin.com/static/img/sse/sse_workflow.png"/>

## SSE协议层工作原理：
<img src="https://luochengbin.com/static/img/sse/sse_protocol_workflow.png">

## 连接建立过程

# 客户端发起请求：
``
http
GET /ai-stream HTTP/1.1
Host: api.example.com
Accept: text/event-stream
Cache-Control: no-cache
``
# 服务端响应：
``
http
HTTP/1.1 200 OK
Content-Type: text/event-stream
Connection: keep-alive
Transfer-Encoding: chunked
Cache-Control: no-cache
``
# 数据流格式：
``
text
event: ready
id: 167890
retry: 5000
data: This is the first line
data: This is the second line
data: Single line message\n\n
event: close
``

## 项目流程图
<img src="https://luochengbin.com/static/img/sse/sse_project_workflow.png"/>

详细流程说明：
1. 前端发起请求：
    - 用户在前端输入问题并发送
    - 前端通过 GET 请求访问 `/sse/stream?question=xxx` 接口
    - 使用 EventSource 建立 SSE 连接
   
2. 后端控制器处理：
    - `SseController` 接收到请求
    - 创建 `SseEmitter` 对象用于流式响应
    - 调用 `ChatService` 的 `streamChat` 方法

3. ChatService 处理：
    - 首先发送 "ready" 事件，告知前端连接已就绪
    - 从 `VolcengineConfig` 获取配置信息（API密钥、基础URL、模型名称等）
    - 构建 `ChatMessage` 和 `ChatCompletionRequest` 对象
    - 使用 `ArkService` 调用 Deepseek API

4. Deepseek API 交互：
    - 发送流式请求到 Deepseek
    - Deepseek 持续返回数据块
    - `ChatService` 将每个数据块包装成事件发送给前端

5. 前端接收处理：
    - 接收 "ready" 事件：显示"思考中..."
    - 接收 "message" 事件：逐字显示返回内容
    - 接收 "close" 事件：完成显示，关闭连接

6. 数据格式：
    - ready 事件：`{event: "ready", data: "连接已就绪"}`
    - message 事件：`{event: "message", data: {"v": "实际内容"}}`
    - close 事件：`{event: "close", data: "传输完成"}`

7. 错误处理：
    - 如果在处理过程中发生错误，会通过 `emitter.completeWithError()` 通知前端
    - 前端会监听错误事件并适当处理
   

## 项目使用说明
1.访问：https://luochengbin.com/sse
2.默认账号密码登录：admin    admin123
3.输入框输入问题即可使用
4.模式说明：
    DeepSeek模式：后台调用火山引擎提供的deepseek-r1-250120，返回结果内容，流式传输到前端解析
    Local模式：本地模式，后台模拟数据，返回结果内容，流式传输到前端解析

## Java 服务端实现细节
<a href="https://github.com/Litaibai-Geek/sse.luochengbin.com/tree/master/ruoyi-admin/src/main/java/com/ruoyi/web/controller/sse">主要代码，统一放在sse包下，方便查看</a>
<a href="https://github.com/Litaibai-Geek/sse.luochengbin.com/blob/master/ruoyi-admin/src/main/java/com/ruoyi/web/controller/sse/SseController.java">SseController</a>
<a href="https://github.com/Litaibai-Geek/sse.luochengbin.com/blob/master/ruoyi-admin/src/main/java/com/ruoyi/web/controller/sse/service/ChatService.java">ChatService</a>

## VUE 客户端实现细节
<a href="https://github.com/Litaibai-Geek/sse.luochengbin.com/blob/master/ruoyi-ui/src/views/front/sse/index.vue">front/sse/index.vue</a>

## 遇到的问题：
# 1.没有返回开始、结束标记，导致一直请求重复问题。
解决方法：参考deepseek返回数据格式，发现它返回了开始标记(event: ready)和结束标记(event: close)。

## deepseek后端参考返回数据格式：

``
event: ready

data: {"v": "用户"}

data: {"v": "问"}

data: {"v": "也很"}

event: close
``
## 项目部署
1.购买域名：https://www.spaceship.com/
2.前后端修改为生产环境：/prod-api/，运行bin/package.bat 打成jar包，上传到服务器
3.npm run build:prod ，生成静态文件，上传到服务器
4.运行后端程序
``
    ps -ef | grep java
    nohup java -jar ruoyi-admin-sse.jar > output.log 2>&1 &
``
5.DNS解析到服务器
6.申请SSL证书
7.修改nginx配置
``
    server {
    listen 443 ssl;
    server_name luochengbin.com;
    
        # SSL 配置
        ssl_certificate /www/server/panel/vhost/cert/luochengbin.com/fullchain.pem;
        ssl_certificate_key /www/server/panel/vhost/cert/luochengbin.com/privkey.pem;
        include /etc/letsencrypt/options-ssl-nginx.conf;
        ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;
    
        # 托管前端静态文件
        root /www/wwwroot/sse-ruoyi-front/dist;
        index index.html index.htm;
    
        location / {
            try_files $uri $uri/ /index.html;  # 处理 Vue 路由
        }
    
        # 代理后端 API
        location /prod-api/ {
            proxy_pass http://localhost:18888/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }
    }
``
8.访问 https://luochengbin.com/sse 默认账号密码登录可用 

## 可优化点：
1.记录用户登录状态，保存用户历史输入数据
2.数据返回后的样式问题
3.火山引擎提供的api不支持流式返回，只能等待数据全部返回， 否则可以直接调用api流式输出，提升速度。


