# Pangolin
轻量级内网穿透工具，类似花生壳工具  
基于Java8，netty4.x实现，开箱即用，可转发基于TCP的应用层数据流，例如HTTP/HTTPS,SSH。


#### How To Use

    1. [可选] 配置server模块下的pub_net_conf.json文件对应的公网访问端口
        [
            {
                "pub_net_port": 7700,
                "user_private_key": "qaz123"
            }
        ]
    2. mvn clean package  
    3. java -jar pangolin_server.jar 7500  
    4. java -jar pangolin_client.jar -i_host [xxxx] -i_port 7500 -t_host [yyyy] -t_port [zzzz] -p_key qaz123

#### Argument Description  
    i_host 内网代理服务器主机名称  
    i_port 内网代理服务器端口  
    t_host 被代理机器主机名称  
    t_port 被代理机器端口  
    p_key 客户端分配的私钥


#### Management Restful Api  
    查询所有在线通道信息  
    - http://127.0.0.1:7600/channel/online/info  

    查询公网端口配置信息  
    - http://127.0.0.1:7600/public/port/conf  

    关闭已失活的通道  
    - http://127.0.0.1:7600/channel/inactive/close
    
    查询请求链路信息 
    - http://127.0.0.1:7600/public/trace/info
    
    查询用户流量信息 
    - http://127.0.0.1:7600/public/flow/info
