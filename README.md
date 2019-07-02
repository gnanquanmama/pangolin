# pangolin
内网穿透工具
基于JDK8,netty4.x实现

### 使用方法

1) mvn clean package  

2) java -jar pangolin_server.jar 7500  

3) java -jar pangolin_client.jar -p_host 127.0.0.1 -p_port 7500 -r_host 127.0.0.1 -r_port 9999 -private_key qaz123

### 说明  
p_host 代理服务器主机名称  
p_port 代理服务器端口  
r_host 被代理机器主机名称  
r_port 被代理机器端口  
private_key 客户端分配的私钥  
