# lemon
The micro service gateway framework.

基于Netty实现微服务网关（Micro Service Gateway）。
同时支持Dubbo泛化调用和HTTP调用，并支持自定义实现微服务网关请求的代理转发功能。

**Support Protocol**
- HTTP proxy Dubbo
- HTTP proxy HTTP

**TODO Protocol**
- HTTP proxy Motan
- HTTP proxy SpringCloud
- TCP proxy other

## 1 Config Introduce
Config Path: `src/main/resources/lemon.yml`

```
port: 8080
application: lemon
# Netty IO/Work thread number
ioThread: 0
workThread: 0
# Body max content length, 64 * 1024 * 1024 = 64 MB
maxContentLength: 67108864
# Max client connection channel number
maxChannel: 100000
# 
bizCoreThread: 20
bizMaxThread: 200
bizQueueCapacity: 800
bizKeepAliveTime: 60000
# Custom Fixed Response Header Parameter Configuration
resHeaders:
  Connection: keep-alive
  Accept-Encoding: gzip,deflate
  Content-Type: application/json;charset=UTF-8
originalHeaders: [Connection, Content-Type, Set-Cookie, Call-Code, Call-Message]
# Dubbo lemon config
dubbo:
  registryAddress: zookeeper://127.0.0.1:2181
  metadataAddress: zookeeper://127.0.0.1:2181
# Direct forwarding HTTP rule
services:
  - category: jsoup
    service: /baidu/**
    url: https://www.baidu.com
  - category: jsoup
    service: /oschina/**
    url: https://www.oschina.net
```

## 2 Dubbo Lemon
The support apache dubbo 2.7.2 generic service proxy.

**Format:**
```
URL:
http://[host]:[port]/lemon/[application]/[service]/[method]?group=[group]&version=[version]

Body:
List[Map{...}, Map{...}, ...]
```

**Example:**
```
http://localhost:8080/lemon/micro-dubbo-provider/cn.micro.biz.dubbo.provider.DemoService/test

[{"name":"lemon", "age":23}, {"10001"}]
```

## 3 HTTP Lemon
```
http://[host]:[port]/lemon/[service]/**?group=[group]&version=[version]
```

## 4 Motan Lemon
TODO

## 5 Spring Cloud Lemon
TODO
