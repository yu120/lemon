# lemon
The micro service gateway framework.

## Dubbo Lemon
The support apache dubbo 2.7.2 generic service proxy.

```
http://[host]:[port]/lemon/[application]/[service]/[method]?group=[group]&version=[version]

List[Map{...}, Map{...}, ...]
```

```
http://localhost:8080/lemon/micro-dubbo-provider/cn.micro.biz.dubbo.provider.DemoService/test

[{"name":"lemon", "age":23}, {"10001"}]
```

## Motan Lemon

## HTTP Lemon
```
http://[host]:[port]/lemon/[service]/**?group=[group]&version=[version]
```