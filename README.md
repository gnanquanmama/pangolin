# Pangolin
内网穿透工具 基于Java8,netty4.x实现

#### How To Use

    1. 配置server模块下的user.json文件对应的公网访问端口
        [
            {
                "privateKey": "qaz123",
                "publicPort": 9797
            }
        ]
    2. mvn clean package  
    3. java -jar pangolin_server.jar 7500  
    4. java -jar pangolin_client.jar -p_host [xxxx] -p_port 7500 -r_host [yyyy] -r_port [zzzz] -p_key qaz123

#### Argument Description  
    p_host 代理服务器主机名称  
    p_port 代理服务器端口  
    r_host 被代理机器主机名称  
    r_port 被代理机器端口  
    p_key 客户端分配的私钥


#### Management Restful Api  
    查询所有在线通道信息  
    - http://127.0.0.1:7060/channel/online/info  

    查询公网端口配置信息  
    - http://127.0.0.1:7060/public/port/conf  

    关闭已失活的通道  
    - http://127.0.0.1:7060/channel/inactive/close
