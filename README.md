# pangolin
内网穿透工具

基于JDK8,netty4.x实现

1) mvn clean package

2) java -jar server-1.0-SNAPSHOT.jar proxy_port

3) java -jar client-1.0-SNAPSHOT.jar -proxy_server_host 127.0.01 -proxy_server_port 5600 -real_server_host 127.0.0.1 -real_server_port 9999 -user_id 1
