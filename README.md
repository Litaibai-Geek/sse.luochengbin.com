## 平台简介
基于若依开发的SSE AI问答系统是一套全部开源的快速开发平台，毫无保留给个人及企业免费使用。
* 前端采用Vue、Element UI。
* 后端采用Spring Boot、Spring Security、Redis & Jwt。
* 权限认证使用Jwt，支持多终端认证系统。
* 支持加载动态权限菜单，多方式轻松权限控制。
* 高效率开发，使用代码生成器可以一键生成前后端代码。

## SSE是什么？
Server-Sent Events (SSE) 是一种服务器推送技术，允许服务器向客户端推送数据。与WebSocket不同，SSE是单向的(只能服务器向客户端推送)，但它的优势在于:
1.基于HTTP协议，实现简单
2.自动重连机制
3.轻量级，资源消耗少
4.天然支持文本传输，特别适合流式数据

SSE工作流程图：




## 遇到的问题：
1.没有返回开始、结束标记，导致一直请求重复问题。
解决方法：参考deepseek返回数据格式，发现它返回了开始标记(event: ready)和结束标记(event: close)。

## deepseek后端参考返回数据格式：

event: ready

data: {"v": "用户"}

data: {"v": "问"}

data: {"v": "也很"}

event: close
