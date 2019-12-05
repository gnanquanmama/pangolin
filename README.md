# Pangolin
轻量级内网穿透工具，类似花生壳工具  
基于Java8，netty4.x实现，开箱即用，可转发基于TCP的应用层数据流，例如HTTP/HTTPS,SSH  
通讯协议使用Protocol Buffer

#### Architecture Diagram
![image](https://raw.githubusercontent.com/gnanquanmama/pangolin/develop/server/src/main/resources/static/architecture.png)


#### How To Use

    0. git clone git@github.com:gnanquanmama/pangolin.git
    1. [可选] 配置server模块下的pub_net_conf.json文件对应的公网访问端口
    2. mvn clean package  
    3. java -jar pangolin_server.jar 7500  
    4. java -jar pangolin_client.jar -i_host [xxxx] -i_port 7500 -t_host [yyyy] -t_port [zzzz] -p_key qaz123

#### Argument Description  
    i_host 内网代理服务器主机名称  
    i_port 内网代理服务器端口  
    t_host 被代理机器主机名称  
    t_port 被代理机器端口  
    p_key 客户端分配的私钥


#### Management Tool  
    telnet 127.0.0.1 7600
    password: root#123
    
    WELCOME TO PANGOLIN CONSOLE... 
    
    >>> 0.菜单
    >>> 1.查询所有在线通道信息
    >>> 2.查询公网端口配置信息
    >>> 3.关闭已失活的通道
    >>> 4.查询请求链路信息
    >>> 5.查询用户流量信息
    >>> x.退出请输入exit
    >>> 请输入对应的数字...
    
    
#### BindTempProxyPort  

    curl http://127.0.0.1:7601/dashboard/bindTempProxyPort -d "privateKey=123456&proxyPort=7702&authCode=XXXX"
    YYYY为年，MM为月，DD为日
    authCode规则为 YYYY + MM**2 + DD**3 
