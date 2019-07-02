# pangolin
内网穿透工具
基于JDK8,netty4.x实现

#使用方法

1) mvn clean package

2) java -jar pangolin_server.jar proxy_port

3) java -jar pangolin_client.jar -proxy_server_host 127.0.0.1 -proxy_server_port proxy_port -real_server_host 127.0.0.1 -real_server_port 9999 -private_key qaz123

#说明
proxy_port为内网穿透服务内网可访问端口，默认为7500
proxy_server_host为代理服务器主机名称
proxy_server_port为代理服务器端口
real_server_host为被代理机器主机名称
real_server_port为被代理机器端口
private_key为客户端分配的私钥
