# pangolin
内网穿透工具
基于JDK8,netty4.x实现


### 使用方法

   1. 配置server模块下的user.json文件  
配置外网访问端口还有对应的私钥privateKey

   2. mvn clean package  

   3. java -jar pangolin_server.jar 7500  

   4. java -jar pangolin_client.jar -p_host 127.0.0.1 -p_port 7500 -r_host 192.168.126.124 -r_port 22 -private_key privateKey


### 说明  
   p_host 代理服务器主机名称  
   p_port 代理服务器端口  
   r_host 被代理机器主机名称  
   r_port 被代理机器端口  
   private_key 客户端分配的私钥  


### 管理接口
   查询所有在线通道信息 http://127.0.0.1:7060/channel/online/info